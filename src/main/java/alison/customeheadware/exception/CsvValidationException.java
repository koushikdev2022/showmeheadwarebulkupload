package alison.customeheadware.exception;



public class CsvValidationException extends RuntimeException {
    
    private final String errorType;
    
    public CsvValidationException(String message) {
        super(message);
        this.errorType = "CSV_VALIDATION_ERROR";
    }
    
    public CsvValidationException(String message, String errorType) {
        super(message);
        this.errorType = errorType;
    }
    
    public CsvValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = "CSV_VALIDATION_ERROR";
    }
    
    public String getErrorType() {
        return errorType;
    }
}

