package alison.customeheadware.service.balkupload;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import alison.customeheadware.dto.CapInventoryDTO;
import alison.customeheadware.exception.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CsvParserService {
    
    // ALL 17 headers are REQUIRED
    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "Hat Name", "Hat Color", "Hat size", "Hat Description", "Available Quantity",
        "Embroidery_24", "Embroidery_48", "Embroidery_96", "Embroidery_144", 
        "Embroidery_576", "Embroidery_2500_plus",
        "LeatherPatch_24", "LeatherPatch_48", "LeatherPatch_96", "LeatherPatch_144", 
        "LeatherPatch_576", "LeatherPatch_2500_plus"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_ROWS = 50000;
    private final ExecutorService executorService;
    private static final int BATCH_SIZE = 100;
    
    public CsvParserService() {
        int processors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(processors);
        log.info("CSV Parser initialized with {} threads", processors);
    }
    
    public List<CapInventoryDTO> parseCsv(MultipartFile file) throws IOException, CsvException {
        long startTime = System.currentTimeMillis();
        
        // Validate file
        validateFile(file);
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = reader.readAll();
            
            if (records.isEmpty()) {
                throw new CsvValidationException("CSV file is empty", "EMPTY_FILE");
            }
            
            if (records.size() == 1) {
                throw new CsvValidationException(
                    "CSV file contains only headers, no data rows found", 
                    "NO_DATA_ROWS"
                );
            }
            
            if (records.size() > MAX_ROWS) {
                throw new CsvValidationException(
                    String.format("CSV file contains %d rows, maximum allowed is %d", 
                        records.size() - 1, MAX_ROWS),
                    "TOO_MANY_ROWS"
                );
            }
            
            String[] headers = records.get(0);
            validateHeaders(headers);
            
            Map<String, Integer> headerMap = createHeaderMap(headers);
            List<String[]> dataRows = records.subList(1, records.size());
            
            // Validate that at least one valid data row exists
            validateDataRows(dataRows, headerMap);
            
            log.info("Loaded {} records from CSV", dataRows.size());
            
            List<List<String[]>> batches = partitionList(dataRows, BATCH_SIZE);
            
            List<CompletableFuture<List<CapInventoryDTO>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> 
                    processBatch(batch, headerMap), executorService))
                .collect(Collectors.toList());
            
            List<CapInventoryDTO> inventoryList = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("CSV parsing completed in {}ms for {} records", 
                processingTime, inventoryList.size());
            
            return inventoryList;
            
        } catch (IOException | CsvException e) {
            throw new CsvValidationException(
                "Failed to read CSV file: " + e.getMessage(), 
                "FILE_READ_ERROR"
            );
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CsvValidationException("No file uploaded", "NO_FILE");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new CsvValidationException(
                "Invalid file format. Only CSV files are allowed", 
                "INVALID_FILE_TYPE"
            );
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CsvValidationException(
                String.format("File size exceeds maximum limit of %dMB", MAX_FILE_SIZE / (1024 * 1024)),
                "FILE_TOO_LARGE"
            );
        }
    }
    
    private Map<String, Integer> createHeaderMap(String[] headers) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String trimmedHeader = headers[i].trim();
            if (trimmedHeader.isEmpty()) {
                throw new CsvValidationException(
                    "Empty header found at column " + (i + 1),
                    "EMPTY_HEADER"
                );
            }
            headerMap.put(trimmedHeader, i);
        }
        return headerMap;
    }
    
    private List<CapInventoryDTO> processBatch(List<String[]> batch, Map<String, Integer> headerMap) {
        return batch.stream()
            .map(row -> mapToDTO(row, headerMap))
            .collect(Collectors.toList());
    }
    
    private void validateHeaders(String[] headers) {
        Set<String> actualHeaders = Arrays.stream(headers)
            .map(String::trim)
            .filter(h -> !h.isEmpty())
            .collect(Collectors.toSet());
        
        // Check for missing headers
        Set<String> missingHeaders = new HashSet<>(REQUIRED_HEADERS);
        missingHeaders.removeAll(actualHeaders);
        
        if (!missingHeaders.isEmpty()) {
            throw new CsvValidationException(
                String.format("Missing required columns: %s. Please ensure CSV has all 17 columns: %s",
                    missingHeaders, REQUIRED_HEADERS),
                "MISSING_HEADERS"
            );
        }
        
        // Check for duplicate headers
        Set<String> seenHeaders = new HashSet<>();
        List<String> duplicates = new ArrayList<>();
        
        for (String header : headers) {
            String trimmed = header.trim();
            if (!trimmed.isEmpty() && !seenHeaders.add(trimmed)) {
                duplicates.add(trimmed);
            }
        }
        
        if (!duplicates.isEmpty()) {
            throw new CsvValidationException(
                "Duplicate headers found: " + duplicates,
                "DUPLICATE_HEADERS"
            );
        }
        
        log.info("CSV header validation passed. All 17 required columns present.");
    }
    
    private void validateDataRows(List<String[]> dataRows, Map<String, Integer> headerMap) {
        if (dataRows.isEmpty()) {
            throw new CsvValidationException(
                "No data rows found in CSV", 
                "NO_DATA_ROWS"
            );
        }
        
        // Sample first few rows for validation
        int rowsToCheck = Math.min(5, dataRows.size());
        List<String> validationErrors = new ArrayList<>();
        
        for (int i = 0; i < rowsToCheck; i++) {
            String[] row = dataRows.get(i);
            int rowNumber = i + 2; // +2 because row 1 is header, and 0-indexed
            
            // Check if row is completely empty
            boolean allEmpty = Arrays.stream(row).allMatch(cell -> cell == null || cell.trim().isEmpty());
            if (allEmpty) {
                validationErrors.add("Row " + rowNumber + " is completely empty");
            }
            
            // Validate required fields
            String hatName = getValueOrNull(row, headerMap.get("Hat Name"));
            if (hatName == null || hatName.isEmpty()) {
                validationErrors.add("Row " + rowNumber + ": Hat Name is required");
            }
        }
        
        if (!validationErrors.isEmpty()) {
            throw new CsvValidationException(
                "Data validation errors: " + String.join("; ", validationErrors),
                "DATA_VALIDATION_ERROR"
            );
        }
    }
    
    private CapInventoryDTO mapToDTO(String[] row, Map<String, Integer> headerMap) {
        CapInventoryDTO dto = new CapInventoryDTO();
        
        // Basic fields
        dto.setHatName(getValueOrNull(row, headerMap.get("Hat Name")));
        dto.setHatColor(getValueOrNull(row, headerMap.get("Hat Color")));
        dto.setHatSize(getValueOrNull(row, headerMap.get("Hat size")));
        dto.setHatDescription(getValueOrNull(row, headerMap.get("Hat Description")));
        
        String quantityStr = getValueOrNull(row, headerMap.get("Available Quantity"));
        dto.setAvailableQuantity(parseInteger(quantityStr));
        
        // Embroidery pricing tiers
        dto.setEmbroidery24(parsePriceValue(row, headerMap.get("Embroidery_24")));
        dto.setEmbroidery48(parsePriceValue(row, headerMap.get("Embroidery_48")));
        dto.setEmbroidery96(parsePriceValue(row, headerMap.get("Embroidery_96")));
        dto.setEmbroidery144(parsePriceValue(row, headerMap.get("Embroidery_144")));
        dto.setEmbroidery576(parsePriceValue(row, headerMap.get("Embroidery_576")));
        dto.setEmbroidery2500Plus(parsePriceValue(row, headerMap.get("Embroidery_2500_plus")));
        
        // Leather patch pricing tiers
        dto.setLeatherPatch24(parsePriceValue(row, headerMap.get("LeatherPatch_24")));
        dto.setLeatherPatch48(parsePriceValue(row, headerMap.get("LeatherPatch_48")));
        dto.setLeatherPatch96(parsePriceValue(row, headerMap.get("LeatherPatch_96")));
        dto.setLeatherPatch144(parsePriceValue(row, headerMap.get("LeatherPatch_144")));
        dto.setLeatherPatch576(parsePriceValue(row, headerMap.get("LeatherPatch_576")));
        dto.setLeatherPatch2500Plus(parsePriceValue(row, headerMap.get("LeatherPatch_2500_plus")));
        
        return dto;
    }
    
    private String getValueOrNull(String[] row, Integer index) {
        if (index == null || index >= row.length) {
            return null;
        }
        String value = row[index].trim();
        return value.isEmpty() ? null : value;
    }
    
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer value: {}", value);
            return null;
        }
    }
    
    private BigDecimal parsePriceValue(String[] row, Integer index) {
        String value = getValueOrNull(row, index);
        if (value == null) {
            return null;
        }
        try {
            String cleanValue = value.replace("$", "").replace(",", "").trim();
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse price value: {}", value);
            return null;
        }
    }
    
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
