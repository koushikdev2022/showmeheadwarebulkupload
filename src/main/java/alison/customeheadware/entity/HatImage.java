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
@Table(name = "hat_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HatImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hat_style_id")
    private Hat hat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hat_color_id")
    private HatColor hatColor;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_type")
    private String imageType;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_primary")
    @Builder.Default
    private Integer isPrimary = 0;

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
