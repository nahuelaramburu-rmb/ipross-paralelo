package com.capacidad.validationapi.module.dashboard.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
public class FilteredTendencyGraph {

    Map<Object, Tendency> tendencies;
    Map<Object, List<XYPoint>> graphs;
    private Object[] keys;

    public FilteredTendencyGraph(List<Tendency> tendencies, List<XYPoint> xyPoints) {
        if (!ObjectUtils.allNotNull(tendencies, xyPoints) || tendencies.isEmpty() || xyPoints.isEmpty()) {
            this.keys = new Object[0];
            this.tendencies = Collections.emptyMap();
            this.graphs = Collections.emptyMap();
        } else {
            this.tendencies = tendencies.stream()
                    .collect(LinkedHashMap::new,
                            (map, item) -> map.put(item.getKey(), item),
                            Map::putAll);
            this.graphs = xyPoints.stream()
                    .collect(Collectors.groupingBy(XYPoint::getKey));
            this.keys = graphs.keySet().toArray();
        }
    }

}
