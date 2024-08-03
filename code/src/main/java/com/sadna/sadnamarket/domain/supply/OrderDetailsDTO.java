package com.sadna.sadnamarket.domain.supply;

import java.util.HashMap;
import java.util.Map;

public class OrderDetailsDTO {
    Map<Integer, Integer> productAmountMap;

    public OrderDetailsDTO(Map<Integer, Integer> mapToClone) {
        this.productAmountMap = new HashMap<>();
        for(Integer key : mapToClone.keySet()){
            productAmountMap.put(key, mapToClone.get(key));
        }
    }

    public Map<Integer, Integer> getProductAmountMap() {
        return productAmountMap;
    }
}
