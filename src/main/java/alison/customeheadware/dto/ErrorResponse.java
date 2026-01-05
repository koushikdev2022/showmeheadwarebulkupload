package alison.customeheadware.dto;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    @JsonProperty("success")
    private boolean success = false;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("errorType")
    private String errorType;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("details")
    private List<String> details;
    
    public ErrorResponse(String message, String errorType) {
        this.message = message;
        this.errorType = errorType;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String message, String errorType, List<String> details) {
        this.message = message;
        this.errorType = errorType;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}

