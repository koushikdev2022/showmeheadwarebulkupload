package alison.customeheadware.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupedCapInventoryDTO {
    
    @JsonProperty("hatName")
    private String hatName;
    
    @JsonProperty("hatDescription")
    private String hatDescription;
    
    @JsonProperty("items")
    private List<CapColorItemDTO> items = new ArrayList<>();
}
