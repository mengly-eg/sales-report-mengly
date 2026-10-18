package edu.itc.salesreport.ingest;

/**
 * Checked exception thrown when a row from the input POS export cannot be parsed or violates domain rules.
 */
public class InvalidRowException extends Exception {
    public InvalidRowException(String message) {
        super(message);
    }

    public InvalidRowException(String message, Throwable cause) {
        super(message, cause);
    }
}
