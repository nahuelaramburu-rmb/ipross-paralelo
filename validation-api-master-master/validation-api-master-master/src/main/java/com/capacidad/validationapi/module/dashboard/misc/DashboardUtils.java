package com.capacidad.validationapi.module.dashboard.misc;

import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import com.capacidad.validationapi.module.nomenclator.reference.MedicalSpecialtyReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Tuple;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.*;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Component
public class DashboardUtils {

    private final ObjectMapper objectMapper;

    @Autowired
    public DashboardUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<XYPoint> buildReducedXYPointList(List<XYPoint> allPoints, int amount) {
        List<XYPoint> reducedPoints = allPoints.stream()
                .limit(amount).collect(Collectors.toList());
        allPoints.removeAll(reducedPoints);
        if (!allPoints.isEmpty()) {
            XYPoint othersPoint = new XYPoint();
            othersPoint.setX(OTHERS);
            allPoints.forEach(point -> {
                Long newY = (Long) point.getY();
                Long currentY = othersPoint.getY() == null ? 0L : (Long) othersPoint.getY();
                othersPoint.setY(newY + currentY);
            });
            reducedPoints.add(othersPoint);
        }
        return reducedPoints;
    }

    public List<XYPoint> buildXYPointListFromTuple(List<Tuple> tuple) {
        if (tuple == null)
            return Collections.emptyList();
        return tuple.stream()
                .filter(t -> t.getElements().size() == 3)
                .map(t -> new XYPoint(t.get(0), t.get(1), t.get(2)))
                .collect(Collectors.toList());
    }

    public List<Tendency> buildTendencyListFromTuple(List<Tuple> tuple) {
        if (tuple == null)
            return Collections.emptyList();
        return tuple.stream()
                .filter(t -> t.getElements().size() == 3)
                .map(t -> new Tendency(t.get(0), (Long) (t.get(1)), (Long) t.get(2)))
                .collect(Collectors.toList());
    }

    public List<LocalDateTime> parseDates(JsonNode params) {
        List<LocalDateTime> parsedDates = new ArrayList<>();
        LocalDateTime dateFrom = parseDateFrom(params);
        LocalDateTime dateTo = parseDateTo(params);
        parsedDates.add(dateFrom);
        parsedDates.add(dateTo);
        parsedDates.addAll(buildTendencyDates(dateFrom, dateTo));
        return parsedDates;
    }

    public long parseSpecialtyIdOrDefault(JsonNode params) {
        return params.has(SPECIALTY_ID_FILTER) ? params.get(SPECIALTY_ID_FILTER).asLong() : MedicalSpecialtyReference.ALL.getId();
    }

    private LocalDateTime parseDateFrom(JsonNode params) {
        if (params.has(DATE_FROM_FILTER))
            return objectMapper.convertValue(params.get(DATE_FROM_FILTER), LocalDate.class).atTime(0, 0);
        return LocalDate.now().with(firstDayOfMonth()).atTime(0, 0);
    }

    private LocalDateTime parseDateTo(JsonNode params) {
        if (params.has(DATE_TO_FILTER))
            return objectMapper.convertValue(params.get(DATE_TO_FILTER), LocalDate.class).atTime(23, 59, 59);
        return LocalDate.now().with(lastDayOfMonth()).atTime(23, 59, 59);
    }

    private List<LocalDateTime> buildTendencyDates(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> tendencyDates = new ArrayList<>();
        Duration duration = Duration.between(from, to);
        LocalDateTime prevFrom = from.minus(duration).minusSeconds(1);
        LocalDateTime prevTo = to.minus(duration).minusSeconds(1);
        tendencyDates.add(prevFrom);
        tendencyDates.add(prevTo);
        return tendencyDates;
    }

}
