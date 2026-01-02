package alison.customeheadware.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapInventoryItemDTO {
    
    @JsonProperty("hatColor")
    private String hatColor;
    
    @JsonProperty("hatSize")
    private String hatSize;
    
    @JsonProperty("hatDescription")
    private String hatDescription;
    
    @JsonProperty("availableQuantity")
    private Integer availableQuantity;
}

