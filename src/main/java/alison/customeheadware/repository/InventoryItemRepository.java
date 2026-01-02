package alison.customeheadware.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import alison.customeheadware.entity.InventoryItem;
import alison.customeheadware.enums.InventoryStatus;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    
    Optional<InventoryItem> findTop1ByHatSizeVariantAndWarehouse(Long id,Long wareid);
}

