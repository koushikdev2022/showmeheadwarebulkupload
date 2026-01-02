package alison.customeheadware.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import alison.customeheadware.enums.InventorySource;
import alison.customeheadware.enums.InventoryStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    
    @Column(name = "hat_size_variant_id", nullable = false)
    private Long hatSizeVariant;

   
    @Column(name = "warehouse_id")
    private Long warehouse;

    @Column(name = "supplier_sku")
    private String supplierSku;

    @Column(name = "qty_on_hand")
    @Builder.Default
    private Integer qtyOnHand = 0;

    @Column(name = "qty_reserved")
    @Builder.Default
    private Integer qtyReserved = 0;

    @Column(name = "qty_available")
    @Builder.Default
    private Integer qtyAvailable = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private InventoryStatus status = InventoryStatus.IN_STOCK;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    @Builder.Default
    private InventorySource source = InventorySource.MANUAL;

    @Column(name = "is_active")
    @Builder.Default
    private Integer isActive = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

