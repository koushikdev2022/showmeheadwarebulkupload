package alison.customeheadware.dto;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapInventoryResponseDTO {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("totalGroups")
    private int totalGroups;
    
    @JsonProperty("totalItems")
    private int totalItems;
    
    @JsonProperty("data")
    private List<GroupedCapInventoryDTO> data;
}

