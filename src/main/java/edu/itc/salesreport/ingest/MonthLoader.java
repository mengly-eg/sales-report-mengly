package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject: loads monthly CSV files and notifies registered observers of progress.
 */
public final class MonthLoader {

    private final TransactionParser parser;
    private final List<LoadListener> listeners =
            new CopyOnWriteArrayList<>();
    private final List<String> rejectedRows = new ArrayList<>();

    public MonthLoader(TransactionParser parser) {
        this.parser = parser;
    }

    /**
     * Registers a listener; the returned handle unsubscribes it.
     */
    public Runnable subscribe(LoadListener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public List<String> rejectedRows() {
        return List.copyOf(rejectedRows);
    }

    public List<SaleTransaction> load(YearMonth month, Path dataDir) {
        var start = Instant.now();
        var dir = dataDir.resolve(month.toString());
        List<Path> files;
        try (var stream = Files.list(dir)) {
            files = stream.filter(p -> p.getFileName().toString().endsWith(".csv"))
                          .sorted()
                          .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var accepted = new ArrayList<SaleTransaction>();
        int totalAccepted = 0;
        int totalRejected = 0;

        for (int i = 0; i < files.size(); i++) {
            Path file = files.get(i);
            int ok = 0;
            int bad = 0;
            List<String> lines;
            try {
                lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            // Skip line 0 (header row); line numbers are 1-based (list index + 1)
            for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                try {
                    SaleTransaction tx = parser.parse(line);
                    accepted.add(tx);
                    ok++;
                } catch (InvalidRowException e) {
                    bad++;
                    int lineNum = lineIndex + 1;
                    rejectedRows.add(file.getFileName() + ":" + lineNum + " " + e.getMessage());
                }
            }

            totalAccepted += ok;
            totalRejected += bad;
            publish(new LoadEvent.FileLoaded(file, ok, bad, i + 1, files.size()));
        }

        publish(new LoadEvent.LoadFinished(totalAccepted, totalRejected, Duration.between(start, Instant.now())));
        return List.copyOf(accepted);
    }

    private void publish(LoadEvent event) {
        listeners.forEach(l -> l.onEvent(event));
    }
}
