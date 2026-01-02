package alison.customeheadware.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "hat_size_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HatSizeVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    
    @Column(name = "hat_color_id", nullable = false)
    private Long hatColor;

    @Column(name = "size_label", nullable = false)
    private String sizeLabel;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "supplier_sku")
    private String supplierSku;

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

