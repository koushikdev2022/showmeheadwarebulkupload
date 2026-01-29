package alison.customeheadware.service.balkupload;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import alison.customeheadware.dto.CapColorItemDTO;
import alison.customeheadware.dto.CapInventoryResponseDTO;
import alison.customeheadware.dto.CapSizeDTO;
import alison.customeheadware.dto.DecorationPriceTierDTO;
import alison.customeheadware.dto.GroupedCapInventoryDTO;
import alison.customeheadware.dto.ImageDTO;
import alison.customeheadware.entity.Hat;
import alison.customeheadware.entity.HatColor;
import alison.customeheadware.entity.HatImage;
import alison.customeheadware.entity.HatSizeVariant;
import alison.customeheadware.entity.InventoryItem;
import alison.customeheadware.entity.StyleDecorationPriceTier;
import alison.customeheadware.enums.InventorySource;
import alison.customeheadware.enums.InventoryStatus;
import alison.customeheadware.repository.HatColorRepository;
import alison.customeheadware.repository.HatImageRepository;
import alison.customeheadware.repository.HatRepository;
import alison.customeheadware.repository.HatSizeVariantRepository;
import alison.customeheadware.repository.InventoryItemRepository;
import alison.customeheadware.repository.StyleDecorationPriceTierRepository;
import jakarta.persistence.Column;
import jakarta.transaction.Transactional;

@Service
public class InsertDatabaseBulkService {
    @Autowired
    private HatRepository hatRepository;
    @Autowired
    private HatColorRepository hatColorRepository;
    @Autowired
    private HatSizeVariantRepository hatSizeVariantRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private StyleDecorationPriceTierRepository styleDecorationPriceTierRepository;
    @Autowired
    private HatImageRepository hatImageRepository;
    @Transactional
    public CapInventoryResponseDTO uploadIntoDatabase(CapInventoryResponseDTO capInventoryResponseDTO) {
        
        if (capInventoryResponseDTO == null || capInventoryResponseDTO.getData() == null) {
        
            return capInventoryResponseDTO;
        }
        
        for (GroupedCapInventoryDTO hatData : capInventoryResponseDTO.getData()) {
            if (hatData == null) {
                continue; 
            }
            
            Long hatId = findOrCreateHat(hatData.getHatName(), hatData.getHatDescription());
            HatImage imHat =  HatImage.builder()
                                    .imageUrl(hatData.getHatImage())
                                    .imageType("hat")
                                    .isActive(1)
                                    .isPrimary(0)
                                    .hat(hatId)
                                    .hatColor(0L)
                                    .build();
                                    hatImageRepository.save(imHat);
            // if (hatData.getItems() == null) {
            //     continue;  
            // }
            for(DecorationPriceTierDTO dec: hatData.getDecoration()){
                System.out.println(dec+"leatherPatchdetails");
                if("leatherPatch".equals(dec.getName()) ){
                    Long decorId= findOrCreateDecorationPrice(hatId,2L,dec);
                }else{
                    Long decorId= findOrCreateDecorationPrice(hatId,1L,dec);
                }
                
            }
            
            for (CapColorItemDTO colorItemDTO : hatData.getItems()) {
                if (colorItemDTO == null || colorItemDTO.getHatColor() == null) {
                    continue; 
                }
                
                Long colorId = findOrCreateColor(colorItemDTO.getHatColor(), hatId);
                
                if (colorItemDTO.getSizes() == null) {
                    continue;  
                }
                for(ImageDTO image: colorItemDTO.getImages()){
                    HatImage im =  HatImage.builder()
                                    .imageUrl(image.getImage())
                                    .imageType("color")
                                    .isActive(1)
                                    .isPrimary(0)
                                    .hat(hatId)
                                    .hatColor(colorId)
                                    .build();
                                    hatImageRepository.save(im);
                        
                }
                for (CapSizeDTO capSize : colorItemDTO.getSizes()) {
                    if (capSize == null || capSize.getHatSize() == null) {
                        continue;  
                    }
                    
                    Long sizeId = findOrCreateStyle(capSize.getHatSize(), colorId);
                    Long inventoryId = findOrInventoryStyle(sizeId, capSize);

                }
            }
        }
        
        return capInventoryResponseDTO;
    }

     private Long findOrCreateHat(String name, String description) {
        Optional<Hat> existingHat = hatRepository.findTop1ByNameIgnoreCase(name);
        
        Long hatId;
        
        if (existingHat.isPresent()) {
            // Hat exists, get ID
            hatId = existingHat.get().getId();
           
        } else {
            // Hat doesn't exist, create new
            Hat newHat = new Hat();
            newHat.setName(name);
            newHat.setBrand(1L);
            newHat.setIsActive(1);
            newHat.setMinQty(00);
            newHat.setInternalStyleCode(name);
            newHat.setDescription(description);
            Hat savedHat = hatRepository.save(newHat);
            hatId = savedHat.getId();
          
        }
        
        return hatId;
    }

    private Long findOrCreateColor(String name, Long id) {
        
        Optional<HatColor> existingHatColor = hatColorRepository.findTop1ByHatAndNameIgnoreCase(id,name);
        
        Long hatColorId;
        
        if (existingHatColor.isPresent()) {
            System.out.println(existingHatColor.isPresent()+"present");
            hatColorId = existingHatColor.get().getId();
           
        } else {
             System.out.println("false"+"present");
            // Hat doesn't exist, create new
            HatColor newHatColoHat = new HatColor();
            newHatColoHat.setHat(id);
            newHatColoHat.setColorCode(name);
            newHatColoHat.setName(name);
            newHatColoHat.setIsActive(1);
            HatColor savedHatColor = hatColorRepository.save(newHatColoHat);
            hatColorId = savedHatColor.getId();
          
        }
        
        return hatColorId;
    }
   private Long findOrCreateDecorationPrice(Long hatId, Long decorationTypeId, DecorationPriceTierDTO dec) {
    
        // Find existing record by hatId, decorationTypeId, and minQty
        Optional<StyleDecorationPriceTier> existingDecoration = styleDecorationPriceTierRepository
            .findTop1ByHatIdAndDecorationTypeIdAndMinQty(hatId, decorationTypeId,dec.getMinQty());
        
        StyleDecorationPriceTier styleDecorationPriceTier;
        
        if (existingDecoration.isPresent()) {
            // UPDATE existing record
         
            
            styleDecorationPriceTier = existingDecoration.get();
       
            styleDecorationPriceTier.setUnitPrice(dec.getPrice());
            styleDecorationPriceTier.setMinQty(dec.getMinQty());
            styleDecorationPriceTier.setIsActive(1);
            
        } else {
            // CREATE new record
     
            
            styleDecorationPriceTier = new StyleDecorationPriceTier();
            styleDecorationPriceTier.setHatId(hatId);
            styleDecorationPriceTier.setDecorationTypeId(decorationTypeId);
            styleDecorationPriceTier.setMinQty(dec.getMinQty());
            styleDecorationPriceTier.setMaxQty(dec.getMaxQty());
            styleDecorationPriceTier.setUnitPrice(dec.getPrice());
            styleDecorationPriceTier.setDisplayLabel(dec.getMinQty() + (dec.getMaxQty() != null ? "-" + dec.getMaxQty() : "+"));
            styleDecorationPriceTier.setIsActive(1);
        }
        
        // Save (works for both create and update)
        StyleDecorationPriceTier savedData = styleDecorationPriceTierRepository.save(styleDecorationPriceTier);
        
        return savedData.getId();
    }

    private Long findOrCreateStyle(String name, Long id) {
        System.out.println(name+id+"namesssssss");
        Optional<HatSizeVariant> existingHatColorSize = hatSizeVariantRepository.findTop1ByHatColorAndVariantNameIgnoreCase(id,name);
        
        Long hatColorSizeId;
        
        if (existingHatColorSize.isPresent()) {
            // Hat exists, get ID
            hatColorSizeId = existingHatColorSize.get().getId();
           
        } else {
            // Hat doesn't exist, create new
            HatSizeVariant newHatColoSize = new HatSizeVariant();
            newHatColoSize.setIsActive(1);
            newHatColoSize.setHatColor(id);
            newHatColoSize.setSupplierSku("#hhhh");
            newHatColoSize.setVariantName(name);
            newHatColoSize.setSizeLabel(name);
            HatSizeVariant savedHatColorSize = hatSizeVariantRepository.save(newHatColoSize);
            hatColorSizeId = savedHatColorSize.getId();
          
        }
        
        return hatColorSizeId;
    }

  private Long findOrInventoryStyle(Long variantId,CapSizeDTO capSize) {
                 // Debug logging
    System.out.println("=== findOrInventoryStyle DEBUG ===");
    System.out.println("variantId: " + variantId);
    System.out.println("capSize: " + capSize);
            Long warehouse = 1L;
            Optional<InventoryItem> existingItem = inventoryItemRepository
                .findTop1ByHatSizeVariantAndWarehouse(variantId, warehouse);
            
            if (existingItem.isPresent()) {
                InventoryItem item = existingItem.get();
                System.out.println(item+"itemitemitem");
                
                item.setQtyAvailable( capSize.getAvailableQuantity());  
                item.setQtyOnHand( capSize.getAvailableQuantity());
                InventoryItem updatedItem = inventoryItemRepository.save(item);
               
                return updatedItem.getId();
            }
            
            InventoryItem newItem = InventoryItem.builder()
                .hatSizeVariant(variantId)
                .warehouse(warehouse)  // hardcoded 1
                .supplierSku("#INV-" + variantId)  // generate SKU
                .qtyOnHand(capSize.getAvailableQuantity())
                .qtyReserved(0)
                .qtyAvailable(capSize.getAvailableQuantity())
                .status(InventoryStatus.IN_STOCK)
                .source(InventorySource.IMPORT)
                .isActive(1)
                .build();
            
            InventoryItem savedItem = inventoryItemRepository.save(newItem);
       
            
            return savedItem.getId();
        }

}
