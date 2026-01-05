package alison.customeheadware.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapInventoryDTO {
    
    // Basic fields
    @JsonProperty("hatName")
    private String hatName;
    
    @JsonProperty("hatColor")
    private String hatColor;
    
    @JsonProperty("hatSize")
    private String hatSize;  // Can be null/blank
    
    @JsonProperty("hatDescription")
    private String hatDescription;
    
    @JsonProperty("availableQuantity")
    private Integer availableQuantity;
    
    // Embroidery pricing tiers
    @JsonProperty("embroidery_24")
    private BigDecimal embroidery24;
    
    @JsonProperty("embroidery_48")
    private BigDecimal embroidery48;
    
    @JsonProperty("embroidery_96")
    private BigDecimal embroidery96;
    
    @JsonProperty("embroidery_144")
    private BigDecimal embroidery144;
    
    @JsonProperty("embroidery_576")
    private BigDecimal embroidery576;
    
    @JsonProperty("embroidery_2500_plus")
    private BigDecimal embroidery2500Plus;
    
    // Leather patch pricing tiers
    @JsonProperty("leatherPatch_24")
    private BigDecimal leatherPatch24;
    
    @JsonProperty("leatherPatch_48")
    private BigDecimal leatherPatch48;
    
    @JsonProperty("leatherPatch_96")
    private BigDecimal leatherPatch96;
    
    @JsonProperty("leatherPatch_144")
    private BigDecimal leatherPatch144;
    
    @JsonProperty("leatherPatch_576")
    private BigDecimal leatherPatch576;
    
    @JsonProperty("leatherPatch_2500_plus")
    private BigDecimal leatherPatch2500Plus;
}
