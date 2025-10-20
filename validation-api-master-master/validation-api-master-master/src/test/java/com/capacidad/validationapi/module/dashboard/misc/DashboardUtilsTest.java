package com.capacidad.validationapi.module.dashboard.misc;

import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardUtilsTest {

    @InjectMocks
    private DashboardUtils dashboardUtils;

    @Test
    public void testBuildReducedXYPointListReturnSameValuesWhenUnderLimit() {
        List<XYPoint> points = new ArrayList<>();
        points.add(new XYPoint("10/10/2019", 2L));
        points.add(new XYPoint("09/10/2019", 3L));

        List<XYPoint> reducedPoints = dashboardUtils.buildReducedXYPointList(points, 10);

        assertThat(reducedPoints.size()).isEqualTo(2);
    }

    @Test
    public void testBuildReducedXYPointListReturnReducedValuesWhenOverLimit() {
        List<XYPoint> points = new ArrayList<>();
        points.add(new XYPoint("10/10/2019", 2L));
        points.add(new XYPoint("09/10/2019", 3L));
        points.add(new XYPoint("08/10/2019", 4L));
        points.add(new XYPoint("07/10/2019", 5L));
        points.add(new XYPoint("06/10/2019", 6L));

        List<XYPoint> reducedPoints = dashboardUtils.buildReducedXYPointList(points, 4);

        assertThat(reducedPoints.size()).isEqualTo(5);
    }

    @Test
    public void testBuildXYPointListFromTupleReturnsEmptyWhenNullList() {
        List<XYPoint> points = dashboardUtils.buildXYPointListFromTuple(null);
        assertThat(points).isEmpty();
    }

    @Test
    public void testBuildXYPointListFromTupleReturnsEmptyWhenEmptyList() {
        List<XYPoint> points = dashboardUtils.buildXYPointListFromTuple(Collections.emptyList());
        assertThat(points).isEmpty();
    }

    @Test
    public void testBuildXYPointListFromTupleReturnsEmptyWhenLessThanThreeElements() {
        Tuple tuple = mock(Tuple.class);
        List<TupleElement<?>> tupleElements = mock(List.class);
        List<Tuple> tuples = Collections.singletonList(tuple);

        when(tuple.getElements()).thenReturn(tupleElements);
        when(tupleElements.size()).thenReturn(1);

        List<XYPoint> points = dashboardUtils.buildXYPointListFromTuple(tuples);

        assertThat(points).isEmpty();
    }

    @Test
    public void testBuildXYPointListFromTupleReturnValidResult() {
        Tuple tuple = mock(Tuple.class);
        List<TupleElement<?>> tupleElements = mock(List.class);
        List<Tuple> tuples = Collections.singletonList(tuple);

        when(tuple.getElements()).thenReturn(tupleElements);
        when(tupleElements.size()).thenReturn(3);
        when(tuple.get(0)).thenReturn("key");
        when(tuple.get(1)).thenReturn("x");
        when(tuple.get(2)).thenReturn("y");

        List<XYPoint> points = dashboardUtils.buildXYPointListFromTuple(tuples);

        assertThat(points.get(0).getKey()).isEqualTo(tuple.get(0));
        assertThat(points.get(0).getX()).isEqualTo(tuple.get(1));
        assertThat(points.get(0).getY()).isEqualTo(tuple.get(2));
    }

    @Test
    public void testBuildTendencyListFromTupleReturnsEmptyWhenNullList() {
        List<Tendency> points = dashboardUtils.buildTendencyListFromTuple(null);
        assertThat(points).isEmpty();
    }

    @Test
    public void testBuildTendencyListFromTupleReturnsEmptyWhenEmptyList() {
        List<Tendency> points = dashboardUtils.buildTendencyListFromTuple(Collections.emptyList());
        assertThat(points).isEmpty();
    }

    @Test
    public void testBuildTendencyListFromTupleReturnsEmptyWhenLessThanThreeElements() {
        Tuple tuple = mock(Tuple.class);
        List<TupleElement<?>> tupleElements = mock(List.class);
        List<Tuple> tuples = Collections.singletonList(tuple);

        when(tuple.getElements()).thenReturn(tupleElements);
        when(tupleElements.size()).thenReturn(1);

        List<Tendency> points = dashboardUtils.buildTendencyListFromTuple(tuples);

        assertThat(points).isEmpty();
    }

    @Test
    public void testBuildTendencyListFromTupleReturnValidResult() {
        Tuple tuple = mock(Tuple.class);
        List<TupleElement<?>> tupleElements = mock(List.class);
        List<Tuple> tuples = Collections.singletonList(tuple);

        when(tuple.getElements()).thenReturn(tupleElements);
        when(tupleElements.size()).thenReturn(3);
        when(tuple.get(0)).thenReturn("key");
        when(tuple.get(1)).thenReturn(1L);
        when(tuple.get(2)).thenReturn(2L);

        List<Tendency> points = dashboardUtils.buildTendencyListFromTuple(tuples);

        assertThat(points.get(0).getKey()).isEqualTo(tuple.get(0));
        assertThat(points.get(0).getCurrentValue()).isEqualTo(new BigDecimal(tuple.get(1).toString()));
        assertThat(points.get(0).getPreviousValue()).isEqualTo(new BigDecimal(tuple.get(2).toString()));
    }

}
