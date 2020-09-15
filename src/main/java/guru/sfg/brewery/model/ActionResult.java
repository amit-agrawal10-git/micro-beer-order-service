package guru.sfg.brewery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionResult {
    @JsonProperty("id")
    private UUID id = null;
    private Boolean isSuccessful;
    private Boolean hasExceptionOccurred;

}
