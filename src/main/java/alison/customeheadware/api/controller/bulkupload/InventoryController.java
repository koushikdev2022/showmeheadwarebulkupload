package alison.customeheadware.api.controller.bulkupload;




import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import alison.customeheadware.dto.CapInventoryDTO;
import alison.customeheadware.dto.CapInventoryResponseDTO;
import alison.customeheadware.service.balkupload.CreateParseDataService;
import alison.customeheadware.service.balkupload.CsvParserService;
import alison.customeheadware.service.balkupload.InsertDatabaseBulkService;

import java.io.IOException;
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
            CapInventoryResponseDTO data = insertDatabaseBulkService.uploadIntoDatabase(insertData);
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("Total upload processing completed in {}ms", totalTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processingTimeMs", totalTime);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                createErrorResponse(e.getMessage())
            );
        } catch (IOException e) {
            log.error("IO error during CSV processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("Error parsing CSV: " + e.getMessage())
            );
        } catch (Exception e) {
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
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}
