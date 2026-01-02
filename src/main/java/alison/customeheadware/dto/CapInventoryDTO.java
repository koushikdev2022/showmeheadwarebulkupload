package alison.customeheadware.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapInventoryDTO {
    
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
}

