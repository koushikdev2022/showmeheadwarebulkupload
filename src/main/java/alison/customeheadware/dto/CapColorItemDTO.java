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
public class CapColorItemDTO {
    
    @JsonProperty("hatColor")
    private String hatColor;
    
    @JsonProperty("sizes")
    private List<CapSizeDTO> sizes = new ArrayList<>();

     @JsonProperty("images")  // Color-specific sub images
    private List<ImageDTO> images = new ArrayList<>();
}

