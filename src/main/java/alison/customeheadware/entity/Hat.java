package alison.customeheadware.entity;



import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "hats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // just a scalar column, no @ManyToOne
    @Column(name = "brand_id", nullable = false)
    private Long brand;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "internal_style_code")
    private String internalStyleCode;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "size_chart_json", columnDefinition = "json")
    private JsonNode sizeChartJson;

    @Column(name = "min_qty")
    private Integer minQty;

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

