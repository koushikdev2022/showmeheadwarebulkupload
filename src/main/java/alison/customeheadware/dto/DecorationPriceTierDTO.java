package alison.customeheadware.dto;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecorationPriceTierDTO {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("minQty")
    private Integer minQty;
    
    @JsonProperty("maxQty")
    private Integer maxQty;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    // Constructor without maxQty for convenience
    public DecorationPriceTierDTO(String name, Integer minQty, BigDecimal price) {
        this.name = name;
        this.minQty = minQty;
        this.price = price;
    }
}

