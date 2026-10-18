package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

/**
 * Strategy interface for parsing a raw text line into a domain SaleTransaction.
 */
public interface TransactionParser {
    /**
     * Parses a single row into a SaleTransaction.
     *
     * @param line raw text line from POS export
     * @return validated domain transaction record
     * @throws InvalidRowException if the row format, fields, or business rules fail validation
     */
    SaleTransaction parse(String line) throws InvalidRowException;
}
