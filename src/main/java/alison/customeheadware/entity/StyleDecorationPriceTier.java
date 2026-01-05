package alison.customeheadware.entity;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "style_decoration_price_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StyleDecorationPriceTier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "hat_id", nullable = false)
    private Long hatId;
    
    @Column(name = "decoration_type_id", nullable = false)
    private Long decorationTypeId;
    
    @Column(name = "min_qty", nullable = false)
    private Integer minQty;
    
    @Column(name = "max_qty")
    private Integer maxQty;
    
    @Column(name = "display_label")
    private String displayLabel;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "is_active")
    private Integer isActive = 1;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
 
}

