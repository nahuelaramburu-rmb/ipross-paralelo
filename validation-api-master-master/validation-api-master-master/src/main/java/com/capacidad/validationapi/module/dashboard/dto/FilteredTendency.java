package com.capacidad.validationapi.module.dashboard.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class FilteredTendency {

    Map<Object, Tendency> tendencies;
    private Object[] keys;

    public FilteredTendency(List<Tendency> tendencies) {
        if (tendencies == null || tendencies.isEmpty()) {
            this.keys = new Object[0];
            this.tendencies = Collections.emptyMap();
        } else {
            this.tendencies = tendencies.stream()
                    .collect(LinkedHashMap::new,
                            (map, item) -> map.put(item.getKey(), item),
                            Map::putAll);
            this.keys = this.tendencies.keySet().toArray();
        }
    }

}
