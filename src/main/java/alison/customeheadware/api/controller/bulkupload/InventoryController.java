package alison.customeheadware.api.controller.bulkupload;




import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.exceptions.CsvException;

import alison.customeheadware.dto.CapInventoryDTO;
import alison.customeheadware.dto.CapInventoryResponseDTO;
import alison.customeheadware.exception.CsvValidationException;
import alison.customeheadware.service.balkupload.CreateParseDataService;
import alison.customeheadware.service.balkupload.CsvParserService;
import alison.customeheadware.service.balkupload.InsertDatabaseBulkService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/bulkupload/")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    
    private final CsvParserService csvParserService;
    private final CreateParseDataService createParseDataService;
    @Autowired
    private InsertDatabaseBulkService insertDatabaseBulkService;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCapInventory(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                createErrorResponse("File is empty")
            );
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(
                createErrorResponse("Only CSV files are allowed")
            );
        }
        
        try {
            log.info("Starting CSV upload processing for file: {}", filename);
            
            // Parse CSV with parallel processing
            List<CapInventoryDTO> inventoryList = csvParserService.parseCsv(file);
            
            // Group and transform with parallel processing
            CapInventoryResponseDTO insertData = createParseDataService.upload(inventoryList);
            // CapInventoryResponseDTO data = insertDatabaseBulkService.uploadIntoDatabase(insertData);
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("Total upload processing completed in {}ms", totalTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processingTimeMs", totalTime);
            response.put("data", insertData);
            
            return ResponseEntity.ok(response);
            
        } catch (CsvValidationException e) {
            // Custom CSV validation errors with specific error types
            log.error("CSV Validation Error [{}]: {}", e.getErrorType(), e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createValidationErrorResponse(e.getMessage(), e.getErrorType()));
                
        } catch (IllegalArgumentException e) {
            // General validation errors (legacy support)
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
                
        } catch (CsvException e) {
            // OpenCSV specific parsing errors
            log.error("CSV parsing error: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(
                    "CSV file format is invalid: " + e.getMessage(), 
                    "CSV_PARSE_ERROR"
                ));
                
        } catch (IOException e) {
            // File reading/IO errors
            log.error("IO error during CSV processing", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(
                    "Error reading CSV file: " + e.getMessage(), 
                    "FILE_READ_ERROR"
                ));
                
        } catch (MaxUploadSizeExceededException e) {
            // File size exceeded error
            log.error("File size exceeded maximum limit", e);
            return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(createErrorResponse(
                    "File size exceeds maximum upload limit", 
                    "FILE_SIZE_EXCEEDED"
                ));
                
        } catch (OutOfMemoryError e) {
            // Memory error for very large files
            log.error("Out of memory while processing CSV", e);
            return ResponseEntity
                .status(HttpStatus.INSUFFICIENT_STORAGE)
                .body(createErrorResponse(
                    "File is too large to process. Please reduce file size.", 
                    "OUT_OF_MEMORY"
                ));
                
        }  catch (Exception e) {
            log.error("Unexpected error during processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("Unexpected error: " + e.getMessage())
            );
        }
    }
    
    // Optional: Async endpoint that returns immediately
    // @PostMapping("/upload-async")
    // public ResponseEntity<?> uploadCapInventoryAsync(@RequestParam("file") MultipartFile file) {
        
    //     if (file.isEmpty()) {
    //         return ResponseEntity.badRequest().body(
    //             createErrorResponse("File is empty")
    //         );
    //     }
        
    //     String filename = file.getOriginalFilename();
    //     if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
    //         return ResponseEntity.badRequest().body(
    //             createErrorResponse("Only CSV files are allowed")
    //         );
    //     }
        
    //     // Process asynchronously
    //     CompletableFuture.runAsync(() -> {
    //         try {
    //             List<CapInventoryDTO> inventoryList = csvParserService.parseCsv(file);
    //             CapInventoryResponseDTO result = createParseDataService.upload(inventoryList);
    //             log.info("Async processing completed: {} groups, {} items", 
    //                 result.getTotalGroups(), result.getTotalItems());
    //         } catch (Exception e) {
    //             log.error("Async processing failed", e);
    //         }
    //     });
        
    //     Map<String, Object> response = new HashMap<>();
    //     response.put("success", true);
    //     response.put("message", "File upload started. Processing in background.");
    //     response.put("filename", filename);
        
    //     return ResponseEntity.accepted().body(response);
    // }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", false);
        error.put("message", message);
        error.put("statusCode", 422);
        return error;
    }
    private Map<String, Object> createValidationErrorResponse(String message, String errorType) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", false);
        error.put("message", message);
        error.put("errorType", errorType);
          error.put("statusCode", 422);
        error.put("timestamp", LocalDateTime.now());
        error.put("suggestion", getErrorSuggestion(errorType));
        return error;
    }
    
    /**
     * Create generic error response
     */
    private Map<String, Object> createErrorResponse(String message, String errorType) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", false);
        error.put("statusCode", 422);
        error.put("message", message);
        error.put("errorType", errorType);
        error.put("timestamp", LocalDateTime.now());
        return error;
    }
    
    /**
     * Provide helpful suggestions based on error type
     */
    private String getErrorSuggestion(String errorType) {
        switch (errorType) {
            case "NO_FILE":
                return "Please select a file to upload";
            case "INVALID_FILE_TYPE":
                return "Please upload a valid CSV file with .csv extension";
            case "EMPTY_FILE":
                return "The uploaded CSV file is empty. Please provide a file with data";
            case "MISSING_HEADERS":
                return "CSV must contain all 17 required columns. Use /debug-headers endpoint to check your file";
            case "NO_DATA_ROWS":
                return "CSV contains headers but no data rows. Please add data to the file";
            case "FILE_TOO_LARGE":
                return "File size exceeds 10MB. Please split into smaller files";
            case "TOO_MANY_ROWS":
                return "CSV contains too many rows. Maximum allowed is 50,000 rows per file";
            case "DUPLICATE_HEADERS":
                return "CSV contains duplicate column names. Each column must have a unique name";
            case "DATA_VALIDATION_ERROR":
                return "Some rows contain invalid data. Please check Hat Name is not empty";
            default:
                return "Please check the CSV file format and try again";
        }
    }

}
