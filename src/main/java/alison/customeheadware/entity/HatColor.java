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
@Table(name = "hat_colors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HatColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

 
    @Column(name = "hat_style_id", nullable = false)
    private Long hat;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "primary_image_url")
    private String primaryImageUrl;

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
