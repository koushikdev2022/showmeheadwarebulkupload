package alison.customeheadware.service.balkupload;

// import java.util.ArrayList;
// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.springframework.stereotype.Service;

// import alison.customeheadware.dto.CapInventoryDTO;
// import alison.customeheadware.dto.CapInventoryItemDTO;
// import alison.customeheadware.dto.CapInventoryResponseDTO;
// import alison.customeheadware.dto.GroupedCapInventoryDTO;

// @Service
// public class CreateParseDataService {
//     public CapInventoryResponseDTO  upload(List<CapInventoryDTO> capInventoryList){
//       Map<String, List<CapInventoryDTO>> groupedMap = capInventoryList.stream()
//             .collect(Collectors.groupingBy(
//                 dto -> dto.getHatName().toUpperCase(), 
//                 LinkedHashMap::new, 
//                 Collectors.toList()
//             ));
        
      
//         List<GroupedCapInventoryDTO> groupedData = new ArrayList<>();
        
//         for (Map.Entry<String, List<CapInventoryDTO>> entry : groupedMap.entrySet()) {
//             String hatName = entry.getKey();
//             List<CapInventoryDTO> items = entry.getValue();
            
//             GroupedCapInventoryDTO groupedDTO = new GroupedCapInventoryDTO();
//             groupedDTO.setHatName(hatName);
            
//             // Convert each item to CapInventoryItemDTO (without hatName)
//             List<CapInventoryItemDTO> itemDTOs = items.stream()
//                 .map(this::convertToItemDTO)
//                 .collect(Collectors.toList());
            
//             groupedDTO.setItems(itemDTOs);
//             groupedData.add(groupedDTO);
//         }
        
//         // Create response
//         CapInventoryResponseDTO response = new CapInventoryResponseDTO();
//         response.setSuccess(true);
//         response.setTotalGroups(groupedData.size());
//         response.setTotalItems(capInventoryList.size());
//         response.setData(groupedData);
        
//         return response;
//     }

//     private CapInventoryItemDTO convertToItemDTO(CapInventoryDTO dto) {
//         CapInventoryItemDTO itemDTO = new CapInventoryItemDTO();
//         itemDTO.setHatColor(dto.getHatColor());
//         itemDTO.setHatSize(dto.getHatSize());
//         itemDTO.setHatDescription(dto.getHatDescription());
//         itemDTO.setAvailableQuantity(dto.getAvailableQuantity());
//         return itemDTO;
//     }
// }


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import alison.customeheadware.dto.CapInventoryDTO;
import alison.customeheadware.dto.CapInventoryResponseDTO;
import alison.customeheadware.dto.CapColorItemDTO;
import alison.customeheadware.dto.CapSizeDTO;
import alison.customeheadware.dto.DecorationPriceTierDTO;
import alison.customeheadware.dto.GroupedCapInventoryDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CreateParseDataService {
    
    private final ExecutorService executorService;
    
    public CreateParseDataService() {
        int processors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(processors);
        log.info("Initialized thread pool with {} threads", processors);
    }
    
    public CapInventoryResponseDTO upload(List<CapInventoryDTO> capInventoryList) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Group by hatName (case-insensitive)
            Map<String, List<CapInventoryDTO>> groupedByName = capInventoryList.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                    dto -> dto.getHatName().toUpperCase(),
                    ConcurrentHashMap::new,
                    Collectors.toList()
                ));
            
            log.info("Grouped {} items into {} hat names in {}ms", 
                capInventoryList.size(), groupedByName.size(), 
                System.currentTimeMillis() - startTime);
            
            // Step 2: Process each hat name group in parallel
            List<CompletableFuture<GroupedCapInventoryDTO>> futures = groupedByName.entrySet()
                .stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> 
                    processHatNameGroup(entry.getKey(), entry.getValue()), executorService))
                .collect(Collectors.toList());
            
            // Wait for all futures to complete
            List<GroupedCapInventoryDTO> groupedData = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Total processing completed in {}ms", processingTime);
            
            // Create response
            CapInventoryResponseDTO response = new CapInventoryResponseDTO();
            response.setSuccess(true);
            response.setTotalGroups(groupedData.size());
            response.setTotalItems(capInventoryList.size());
            response.setData(groupedData);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error during parallel processing", e);
            throw new RuntimeException("Failed to process inventory data", e);
        }
    }
    
    private GroupedCapInventoryDTO processHatNameGroup(String hatName, List<CapInventoryDTO> items) {
        GroupedCapInventoryDTO groupedDTO = new GroupedCapInventoryDTO();
        groupedDTO.setHatName(hatName);
        
        // Get the first description (assuming same hatName has same description)
        if (!items.isEmpty()) {
            CapInventoryDTO firstItem = items.get(0);
            List<DecorationPriceTierDTO> decorationList = new ArrayList<>();
            // Set description
            groupedDTO.setHatDescription(firstItem.getHatDescription());

            Map<String, BigDecimal> priceFields = new LinkedHashMap<>();

            priceFields.put("embroidery_24", firstItem.getEmbroidery24());
            priceFields.put("embroidery_48", firstItem.getEmbroidery48());
            priceFields.put("embroidery_96", firstItem.getEmbroidery96());
            priceFields.put("embroidery_144", firstItem.getEmbroidery144());
            priceFields.put("embroidery_576", firstItem.getEmbroidery576());
            priceFields.put("embroidery_2500_plus", firstItem.getEmbroidery2500Plus());
        
            priceFields.put("leatherPatch_24", firstItem.getLeatherPatch24());
            priceFields.put("leatherPatch_48", firstItem.getLeatherPatch48());
            priceFields.put("leatherPatch_96", firstItem.getLeatherPatch96());
            priceFields.put("leatherPatch_144", firstItem.getLeatherPatch144());
            priceFields.put("leatherPatch_576", firstItem.getLeatherPatch576());
            priceFields.put("leatherPatch_2500_plus", firstItem.getLeatherPatch2500Plus());
         
            priceFields.forEach((fieldName, price) -> {
                if (price != null) {
                    // Parse fieldName to extract type and quantity
                    String[] parts = fieldName.split("_");
                    String decorationType = parts[0]; // embroidery or leatherPatch
                    
                    // Extract quantity (handling "_plus" suffix)
                    int minQty;
                    if (parts.length == 3 && parts[2].equals("plus")) {
                        minQty = Integer.parseInt(parts[1]); // e.g., 2500 from "embroidery_2500_plus"
                    } else {
                        minQty = Integer.parseInt(parts[1]); // e.g., 24 from "embroidery_24"
                    }
                    
                    DecorationPriceTierDTO tier = new DecorationPriceTierDTO();
                    tier.setName(decorationType);
                    tier.setMinQty(minQty);
                    tier.setPrice(price);
                    
                    log.debug("Field: {}, Type: {}, MinQty: {}, Price: {}", 
                        fieldName, decorationType, minQty, price);
                    
                    decorationList.add(tier);
                }
            });
            groupedDTO.setDecoration(decorationList);
        } else {
            groupedDTO.setHatDescription("");
        }

        // Group by hatColor
        Map<String, List<CapInventoryDTO>> groupedByColor = items.stream()
            .collect(Collectors.groupingBy(
                dto -> dto.getHatColor() != null ? dto.getHatColor() : "Unknown",
                LinkedHashMap::new,
                Collectors.toList()
            ));
        
        // Process each color group
        List<CapColorItemDTO> colorItems = groupedByColor.entrySet().parallelStream()
            .map(entry -> processColorGroup(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
        
        groupedDTO.setItems(colorItems);
        return groupedDTO;
    }
    
    private CapColorItemDTO processColorGroup(String color, List<CapInventoryDTO> items) {
        CapColorItemDTO colorItem = new CapColorItemDTO();
        colorItem.setHatColor(color);
        
        // Convert to size DTOs
        List<CapSizeDTO> sizes = items.stream()
            .map(this::convertToSizeDTO)
            .collect(Collectors.toList());
        
        colorItem.setSizes(sizes);
        return colorItem;
    }
    
    private CapSizeDTO convertToSizeDTO(CapInventoryDTO dto) {
        CapSizeDTO sizeDTO = new CapSizeDTO();
        sizeDTO.setHatSize(dto.getHatSize());
        sizeDTO.setAvailableQuantity(dto.getAvailableQuantity());
        return sizeDTO;
    }

   
}
