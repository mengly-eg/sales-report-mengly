package edu.itc.salesreport.ingest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonthLoaderTest {

    @Test
    void producesExpectedEventsInOrderAndHandlesUnsubscribe(@TempDir Path tempDir) throws IOException {
        var month = YearMonth.of(2026, 9);
        var monthDir = Files.createDirectories(tempDir.resolve(month.toString()));

        // File 1: 2 valid rows
        Files.write(monthDir.resolve("PNH-2026-09-01.csv"), List.of(
                CsvTransactionParser.HEADER,
                "PNH,2026-09-01,PNH-001,SKU-1001,Rice,Grocery,1,6.50,0.00,CASH",
                "PNH,2026-09-01,PNH-002,SKU-1002,Sauce,Grocery,2,1.80,0.00,CARD"
        ));

        // File 2: 1 valid row, 1 invalid row
        Files.write(monthDir.resolve("REP-2026-09-01.csv"), List.of(
                CsvTransactionParser.HEADER,
                "REP,2026-09-01,REP-001,SKU-2001,Water,Beverages,1,0.45,0.00,KHQR",
                "REP,2026-09-01,REP-002,SKU-2002,Coffee,Beverages,invalid_qty,0.90,0.00,KHQR"
        ));

        var loader = new MonthLoader(new CsvTransactionParser());
        var events = new ArrayList<LoadEvent>();
        loader.subscribe(events::add);

        var unsubscribedEvents = new ArrayList<LoadEvent>();
        var unsubscribe = loader.subscribe(unsubscribedEvents::add);
        unsubscribe.run(); // unsubscribe before load

        var transactions = loader.load(month, tempDir);

        // Check unsubscribe
        assertEquals(0, unsubscribedEvents.size());

        // Check accepted transactions
        assertEquals(3, transactions.size());

        // Check events count and order
        assertEquals(3, events.size());
        assertInstanceOf(LoadEvent.FileLoaded.class, events.get(0));
        assertInstanceOf(LoadEvent.FileLoaded.class, events.get(1));
        assertInstanceOf(LoadEvent.LoadFinished.class, events.get(2));

        var f1 = (LoadEvent.FileLoaded) events.get(0);
        assertEquals(2, f1.accepted());
        assertEquals(0, f1.rejected());
        assertEquals(1, f1.filesDone());
        assertEquals(2, f1.filesTotal());

        var f2 = (LoadEvent.FileLoaded) events.get(1);
        assertEquals(1, f2.accepted());
        assertEquals(1, f2.rejected());
        assertEquals(2, f2.filesDone());
        assertEquals(2, f2.filesTotal());

        var finished = (LoadEvent.LoadFinished) events.get(2);
        assertEquals(3, finished.accepted());
        assertEquals(1, finished.rejected());

        // Check rejected row tracking
        assertEquals(1, loader.rejectedRows().size());
        assertTrue(loader.rejectedRows().get(0).contains("REP-2026-09-01.csv:3"));
    }
}
