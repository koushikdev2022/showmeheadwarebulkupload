package alison.customeheadware.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "primary_decoration_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryDecorationType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_primary")
    private Integer isPrimary = 1;
    
    @Column(name = "is_active")
    private Integer isActive = 1;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    
}
