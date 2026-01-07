package alison.customeheadware.service.balkupload;

import alison.customeheadware.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            // 1. Group by hatName (case-insensitive)
            Map<String, List<CapInventoryDTO>> groupedByName = capInventoryList.parallelStream()
                    .collect(Collectors.groupingByConcurrent(
                            dto -> dto.getHatName().toUpperCase(),
                            ConcurrentHashMap::new,
                            Collectors.toList()
                    ));

            log.info("Grouped {} items into {} hat names in {}ms", 
                    capInventoryList.size(), groupedByName.size(), 
                    System.currentTimeMillis() - startTime);

            // 2. Process each hat name group in parallel
            List<CompletableFuture<GroupedCapInventoryDTO>> futures = groupedByName.entrySet()
                    .stream()
                    .map(entry -> CompletableFuture.supplyAsync(
                            () -> processHatNameGroup(entry.getKey(), entry.getValue()),
                            executorService
                    ))
                    .collect(Collectors.toList());

            // 3. Wait for all futures
            List<GroupedCapInventoryDTO> groupedData = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Total processing completed in {}ms", processingTime);

            // 4. Build response
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

        if (!items.isEmpty()) {
            CapInventoryDTO firstItem = items.get(0);
            groupedDTO.setHatDescription(firstItem.getHatDescription());
            groupedDTO.setHatImage(firstItem.getImage1());
            
            // Extract and set product-level images
            List<ImageDTO> productImages = extractProductImages(firstItem);
            groupedDTO.setImages(productImages);
            log.info("Hat: {}, Set {} product-level images", hatName, productImages.size());
         
            List<DecorationPriceTierDTO> decorationList = extractDecorationPricing(firstItem);
            groupedDTO.setDecoration(decorationList);
        } else {
            // Set empty list if no items
            groupedDTO.setImages(new ArrayList<>());
        }
        
        // Group by hatColor
        Map<String, List<CapInventoryDTO>> groupedByColor = items.stream()
                .collect(Collectors.groupingBy(
                        dto -> dto.getHatColor() != null ? dto.getHatColor() : "Unknown",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<CapColorItemDTO> colorItems = groupedByColor.entrySet()
                .parallelStream()
                .map(entry -> processColorGroup(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        groupedDTO.setItems(colorItems);
        
        // Final verification log
        log.info("Hat: {}, Final DTO has {} images, {} color items", 
                 hatName, 
                 groupedDTO.getImages() != null ? groupedDTO.getImages().size() : "null",
                 colorItems.size());
        
        return groupedDTO;
    }

    private CapColorItemDTO processColorGroup(String color, List<CapInventoryDTO> items) {
        CapColorItemDTO colorItem = new CapColorItemDTO();
        colorItem.setHatColor(color);

        if (!items.isEmpty()) {
            // VARIANT IMAGES (sub_image_1..sub_image_5) → data[x].items[y].images
            List<ImageDTO> subImages = extractSubImages(items.get(0));
            colorItem.setImages(subImages);
        }

        // Sizes for this color
        List<CapSizeDTO> sizes = items.stream()
                .map(this::convertToSizeDTO)
                .collect(Collectors.toList());

        colorItem.setSizes(sizes);
        return colorItem;
    }

    private List<ImageDTO> extractProductImages(CapInventoryDTO dto) {
        List<ImageDTO> images = new ArrayList<>();
        
        log.debug("Extracting product images for hat - Image1: {}, Image2: {}, Image3: {}, Image4: {}, Image5: {}", 
                  dto.getImage1(), dto.getImage2(), dto.getImage3(), dto.getImage4(), dto.getImage5());
        
        addImageIfNotNull(images, dto.getImage1());
        addImageIfNotNull(images, dto.getImage2());
        addImageIfNotNull(images, dto.getImage3());
        addImageIfNotNull(images, dto.getImage4());
        addImageIfNotNull(images, dto.getImage5());
        
        log.info("Extracted {} product images from DTO", images.size());
        return images;
    }

    private List<ImageDTO> extractSubImages(CapInventoryDTO dto) {
        List<ImageDTO> images = new ArrayList<>();
        addImageIfNotNull(images, dto.getSubImage1());
        addImageIfNotNull(images, dto.getSubImage2());
        addImageIfNotNull(images, dto.getSubImage3());
        addImageIfNotNull(images, dto.getSubImage4());
        addImageIfNotNull(images, dto.getSubImage5());
        return images;
    }

    private void addImageIfNotNull(List<ImageDTO> images, String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            images.add(new ImageDTO(imageUrl));
        }
    }

    private List<DecorationPriceTierDTO> extractDecorationPricing(CapInventoryDTO firstItem) {
        List<DecorationPriceTierDTO> decorationList = new ArrayList<>();

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
                String[] parts = fieldName.split("_");
                String decorationType = parts[0];
                int minQty = Integer.parseInt(parts[1]);

                DecorationPriceTierDTO tier = new DecorationPriceTierDTO();
                tier.setName(decorationType);
                tier.setMinQty(minQty);
                tier.setPrice(price);

                decorationList.add(tier);
            }
        });

        return decorationList;
    }

    private CapSizeDTO convertToSizeDTO(CapInventoryDTO dto) {
        CapSizeDTO sizeDTO = new CapSizeDTO();
        sizeDTO.setHatSize(dto.getHatSize());
        sizeDTO.setAvailableQuantity(dto.getAvailableQuantity());
        return sizeDTO;
    }
}