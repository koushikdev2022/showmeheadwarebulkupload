package alison.customeheadware.service.balkupload;




// import com.opencsv.CSVReader;
// import com.opencsv.exceptions.CsvException;

// import alison.customeheadware.dto.CapInventoryDTO;

// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.util.*;

// @Service
// public class CsvParserService {
    
//     private static final Set<String> REQUIRED_HEADERS = Set.of(
//         "Hat Name", "Hat Color", "Hat size", "Hat Description", "Available Quantity"
//     );
    
//     public List<CapInventoryDTO> parseCsv(MultipartFile file) throws IOException, CsvException {
//         List<CapInventoryDTO> inventoryList = new ArrayList<>();
        
//         try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
//             List<String[]> records = reader.readAll();
            
//             if (records.isEmpty()) {
//                 throw new IllegalArgumentException("CSV file is empty");
//             }
            
//             // Validate headers
//             String[] headers = records.get(0);
//             validateHeaders(headers);
            
//             // Create header index map
//             Map<String, Integer> headerMap = new HashMap<>();
//             for (int i = 0; i < headers.length; i++) {
//                 headerMap.put(headers[i].trim(), i);
//             }
            
//             // Parse data rows (skip header)
//             for (int i = 1; i < records.size(); i++) {
//                 String[] row = records.get(i);
                
//                 CapInventoryDTO dto = new CapInventoryDTO();
//                 dto.setHatName(getValueOrNull(row, headerMap.get("Hat Name")));
//                 dto.setHatColor(getValueOrNull(row, headerMap.get("Hat Color")));
//                 dto.setHatSize(getValueOrNull(row, headerMap.get("Hat size")));
//                 dto.setHatDescription(getValueOrNull(row, headerMap.get("Hat Description")));
                
//                 String quantityStr = getValueOrNull(row, headerMap.get("Available Quantity"));
//                 dto.setAvailableQuantity(
//                     quantityStr != null && !quantityStr.trim().isEmpty() 
//                         ? Integer.parseInt(quantityStr.trim()) 
//                         : null
//                 );
                
//                 inventoryList.add(dto);
//             }
//         }
        
//         return inventoryList;
//     }
    
//     private void validateHeaders(String[] headers) {
//         Set<String> actualHeaders = new HashSet<>();
//         for (String header : headers) {
//             actualHeaders.add(header.trim());
//         }
        
//         Set<String> missingHeaders = new HashSet<>(REQUIRED_HEADERS);
//         missingHeaders.removeAll(actualHeaders);
        
//         if (!missingHeaders.isEmpty()) {
//             throw new IllegalArgumentException(
//                 "Missing required headers: " + missingHeaders + 
//                 ". Expected headers: " + REQUIRED_HEADERS
//             );
//         }
//     }
    
//     private String getValueOrNull(String[] row, Integer index) {
//         if (index == null || index >= row.length) {
//             return null;
//         }
//         String value = row[index].trim();
//         return value.isEmpty() ? null : value;
//     }
// }





import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import alison.customeheadware.dto.CapInventoryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CsvParserService {
    
    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "Hat Name", "Hat Color", "Hat size", "Hat Description", "Available Quantity"
    );
    
    private final ExecutorService executorService;
    private static final int BATCH_SIZE = 100;
    
    public CsvParserService() {
        int processors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(processors);
        log.info("CSV Parser initialized with {} threads", processors);
    }
    
    public List<CapInventoryDTO> parseCsv(MultipartFile file) throws IOException, CsvException {
        long startTime = System.currentTimeMillis();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = reader.readAll();
            
            if (records.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }
            
            // Validate headers
            String[] headers = records.get(0);
            validateHeaders(headers);
            
            // Create header index map
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim(), i);
            }
            
            // Get data rows (skip header)
            List<String[]> dataRows = records.subList(1, records.size());
            
            log.info("Loaded {} records from CSV", dataRows.size());
            
            // Split into batches for parallel processing
            List<List<String[]>> batches = partitionList(dataRows, BATCH_SIZE);
            
            // Process batches in parallel using CompletableFuture
            List<CompletableFuture<List<CapInventoryDTO>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> 
                    processBatch(batch, headerMap), executorService))
                .collect(Collectors.toList());
            
            // Combine all results
            List<CapInventoryDTO> inventoryList = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("CSV parsing completed in {}ms for {} records", 
                processingTime, inventoryList.size());
            
            return inventoryList;
        }
    }
    
    private List<CapInventoryDTO> processBatch(List<String[]> batch, Map<String, Integer> headerMap) {
        return batch.stream()
            .map(row -> mapToDTO(row, headerMap))
            .collect(Collectors.toList());
    }
    
    private void validateHeaders(String[] headers) {
        Set<String> actualHeaders = new HashSet<>();
        for (String header : headers) {
            actualHeaders.add(header.trim());
        }
        
        Set<String> missingHeaders = new HashSet<>(REQUIRED_HEADERS);
        missingHeaders.removeAll(actualHeaders);
        
        if (!missingHeaders.isEmpty()) {
            throw new IllegalArgumentException(
                "Missing required headers: " + missingHeaders + 
                ". Expected headers: " + REQUIRED_HEADERS
            );
        }
    }
    
    private CapInventoryDTO mapToDTO(String[] row, Map<String, Integer> headerMap) {
        CapInventoryDTO dto = new CapInventoryDTO();
        
        dto.setHatName(getValueOrNull(row, headerMap.get("Hat Name")));
        dto.setHatColor(getValueOrNull(row, headerMap.get("Hat Color")));
        dto.setHatSize(getValueOrNull(row, headerMap.get("Hat size")));
        dto.setHatDescription(getValueOrNull(row, headerMap.get("Hat Description")));
        
        String quantityStr = getValueOrNull(row, headerMap.get("Available Quantity"));
        dto.setAvailableQuantity(
            quantityStr != null && !quantityStr.trim().isEmpty() 
                ? Integer.parseInt(quantityStr.trim()) 
                : null
        );
        
        return dto;
    }
    
    private String getValueOrNull(String[] row, Integer index) {
        if (index == null || index >= row.length) {
            return null;
        }
        String value = row[index].trim();
        return value.isEmpty() ? null : value;
    }
    
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }
}
