package com.capacidad.validationapi.specification;

import org.junit.Test;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SearchOperationTest {

    @Test
    public void testGetSimpleOperationReturnsEqualityWhenInputIsColon() {
        SearchOperation result = SearchOperation.getSimpleOperation(COLON);

        assertThat(result).isEqualTo(SearchOperation.EQUALITY);
    }

    @Test
    public void testGetSimpleOperationReturnsNegationWhenInputIsClosingExclamation() {
        SearchOperation result = SearchOperation.getSimpleOperation(CLOSING_EXCLAMATION);

        assertThat(result).isEqualTo(SearchOperation.NEGATION);
    }

    @Test
    public void testGetSimpleOperationReturnsGreaterThanWhenInputIsGreaterThanSign() {
        SearchOperation result = SearchOperation.getSimpleOperation(GREATER_THAN_SIGN);

        assertThat(result).isEqualTo(SearchOperation.GREATER_THAN);
    }

    @Test
    public void testGetSimpleOperationReturnsLessThanWhenInputIsLessThanSign() {
        SearchOperation result = SearchOperation.getSimpleOperation(LESS_THAN_SIGN);

        assertThat(result).isEqualTo(SearchOperation.LESS_THAN);
    }

    @Test
    public void testGetSimpleOperationReturnsLikeWhenInputIsTilde() {
        SearchOperation result = SearchOperation.getSimpleOperation(TILDE);

        assertThat(result).isEqualTo(SearchOperation.LIKE);
    }

    @Test
    public void testGetSimpleOperationReturnsNullWhenInputIsInvalid() {
        SearchOperation result = SearchOperation.getSimpleOperation("@");

        assertThat(result).isNull();
    }


}
