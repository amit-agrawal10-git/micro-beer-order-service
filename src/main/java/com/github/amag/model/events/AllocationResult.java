package com.github.amag.model.events;

import com.github.amag.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllocationResult {
    BeerOrderDto beerOrderDto;
    Boolean partialAllocation;
    Boolean errorOccurred;
}
