package alison.customeheadware.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapSizeDTO {
    
    @JsonProperty("hatSize")
    private String hatSize;
    
    @JsonProperty("availableQuantity")
    private Integer availableQuantity;
}

