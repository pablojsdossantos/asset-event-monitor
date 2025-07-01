package pd.santos.asseteventmonitor.exception;

import java.io.IOException;

/**
 * Exception thrown when a row in a CSV file cannot be parsed.
 */
public class CsvRowParseException extends IOException {
    
    private final String[] row;
    
    /**
     * Constructs a new CsvRowParseException with the specified detail message and row data.
     *
     * @param message the detail message
     * @param row the row data that could not be parsed
     * @param cause the cause of the parsing failure
     */
    public CsvRowParseException(String message, String[] row, Throwable cause) {
        super(message, cause);
        this.row = row;
    }
    
    /**
     * Returns the row data that could not be parsed.
     *
     * @return the row data
     */
    public String[] getRow() {
        return row;
    }
}