package com.capacidad.validationapi.specification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SearchCriteriaTest {

    @Test
    public void testCreateSearchCriteriaWithOrPredicateReturnsValidCriteria() {
        SearchCriteria searchCriteria = new SearchCriteria("'", "key", SearchOperation.CONTAINS, "simpleValue");

        assertThat(searchCriteria.isOrPredicate()).isTrue();
        assertThat(searchCriteria.getKey()).isEqualTo("key");
        assertThat(searchCriteria.getValue()).isEqualTo("simpleValue");
        assertThat(searchCriteria.getOperation()).isEqualTo(SearchOperation.CONTAINS);
    }

    @Test
    public void testCreateSearchCriteriaWithInvalidOrPredicateReturnsNotOrCriteria() {
        SearchCriteria searchCriteria = new SearchCriteria(",", "key", SearchOperation.CONTAINS, "simpleValue");

        assertThat(searchCriteria.isOrPredicate()).isFalse();
        assertThat(searchCriteria.getKey()).isEqualTo("key");
        assertThat(searchCriteria.getValue()).isEqualTo("simpleValue");
        assertThat(searchCriteria.getOperation()).isEqualTo(SearchOperation.CONTAINS);
    }

    @Test
    public void testCreateSearchCriteriaWithCompoundValueReturnsValidCriteria() {
        SearchCriteria searchCriteria = new SearchCriteria(null, "key", SearchOperation.CONTAINS, "name=test");

        assertThat(searchCriteria.isOrPredicate()).isFalse();
        assertThat(searchCriteria.getKey()).isEqualTo("key");
        assertThat(((SearchCriteria) searchCriteria.getValue()).getKey()).isEqualTo("name");
        assertThat(((SearchCriteria) searchCriteria.getValue()).getValue()).isEqualTo("test");
        assertThat(searchCriteria.getOperation()).isEqualTo(SearchOperation.CONTAINS);
    }

    @Test
    public void testCreateSearchCriteriaWithNotStringValueReturnsValidCriteria() {
        SearchCriteria searchCriteria = new SearchCriteria(null, "key", SearchOperation.CONTAINS, 1234L);

        assertThat(searchCriteria.isOrPredicate()).isFalse();
        assertThat(searchCriteria.getKey()).isEqualTo("key");
        assertThat(searchCriteria.getOperation()).isEqualTo(SearchOperation.CONTAINS);
        assertThat(searchCriteria.getValue()).isInstanceOf(Long.class);
    }

}
