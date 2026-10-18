package edu.itc.salesreport.ingest;

/**
 * Functional interface for observers of data load progress.
 */
@FunctionalInterface
public interface LoadListener {
    void onEvent(LoadEvent event);
}
