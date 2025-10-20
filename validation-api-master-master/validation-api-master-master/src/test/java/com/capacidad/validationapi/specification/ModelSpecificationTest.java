package com.capacidad.validationapi.specification;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.PERCENT_SIGN;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModelSpecificationTest {


    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SearchCriteria searchCriteria;

    @Mock
    private CriteriaQuery criteriaQuery;

    @Mock
    private Root<Beneficiary> beneficiaryRoot;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    private ModelSpecification<Beneficiary, Long> modelSpecification;

    @Before
    public void init() {
        modelSpecification = new ModelSpecification<>(searchCriteria, objectMapper);
    }

    @Test
    public void testToPredicateEquallyAtomicValueOperation() {
        String key = "name";
        String value = "testName";
        Path keyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.EQUALITY);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.get(key)).thenReturn(keyPath);
        when(keyPath.getJavaType()).thenReturn(String.class);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).equal(keyPath, value);
    }

    @Test
    public void testToPredicateNotEquallyAtomicValueOperation() {
        String key = "name";
        String value = "testName";
        Path keyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.NEGATION);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.get(key)).thenReturn(keyPath);
        when(keyPath.getJavaType()).thenReturn(String.class);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).notEqual(keyPath, value);
    }

    @Test
    public void testToPredicateGreaterThanAtomicValueOperation() {
        String key = "beneficiaryCode";
        String value = "1234";
        Path keyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.GREATER_THAN);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.get(key)).thenReturn(keyPath);
        when(keyPath.getJavaType()).thenReturn(String.class);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).greaterThanOrEqualTo(keyPath, value);
    }

    @Test
    public void testToPredicateLessThanAtomicValueOperation() {
        String key = "beneficiaryCode";
        String value = "1234abc";
        Path keyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.LESS_THAN);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.get(key)).thenReturn(keyPath);
        when(keyPath.getJavaType()).thenReturn(String.class);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).lessThanOrEqualTo(keyPath, value);
    }

    @Test
    public void testToPredicateLikeAtomicValueOperation() {
        String key = "name";
        String value = "testName";
        Path keyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.LIKE);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.get(key)).thenReturn(keyPath);
        when(keyPath.getJavaType()).thenReturn(String.class);
        when(keyPath.as(String.class)).thenReturn(keyPath);
        when(criteriaBuilder.upper(keyPath)).thenReturn(keyPath);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).upper(keyPath);
        verify(criteriaBuilder, times(1)).like(criteriaBuilder.upper(keyPath), value.toUpperCase());
    }

    @Test
    public void testToPredicateStartsWithAtomicValueOperation() {
        String key = "name";
        String value = "testName";
        Path keyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.STARTS_WITH);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.get(key)).thenReturn(keyPath);
        when(keyPath.getJavaType()).thenReturn(String.class);
        when(keyPath.as(String.class)).thenReturn(keyPath);
        when(criteriaBuilder.upper(keyPath)).thenReturn(keyPath);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).upper(keyPath);
        verify(criteriaBuilder, times(1)).like(criteriaBuilder.upper(keyPath), StringUtils.join(value.toUpperCase(), PERCENT_SIGN));
    }

    @Test
    public void testToPredicateEndsWithAtomicValueOperation() {
        String key = "name";
        String value = "testName";
        Path keyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.ENDS_WITH);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.get(key)).thenReturn(keyPath);
        when(keyPath.getJavaType()).thenReturn(String.class);
        when(keyPath.as(String.class)).thenReturn(keyPath);
        when(criteriaBuilder.upper(keyPath)).thenReturn(keyPath);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).upper(keyPath);
        verify(criteriaBuilder, times(1)).like(criteriaBuilder.upper(keyPath), StringUtils.join(PERCENT_SIGN, value.toUpperCase()));
    }

    @Test
    public void testToPredicateContainsWithAtomicValueOperation() {
        String key = "name";
        String value = "testName";
        Path keyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.CONTAINS);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.get(key)).thenReturn(keyPath);
        when(keyPath.getJavaType()).thenReturn(String.class);
        when(keyPath.as(String.class)).thenReturn(keyPath);
        when(criteriaBuilder.upper(keyPath)).thenReturn(keyPath);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).upper(keyPath);
        verify(criteriaBuilder, times(1)).like(criteriaBuilder.upper(keyPath), StringUtils.join(PERCENT_SIGN, value.toUpperCase(), PERCENT_SIGN));
    }

    @Test
    public void testToPredicateGreaterThanLocalDateOneToOneRelationOperation() {
        String key = "status";
        SearchCriteria nestedSearchCriteria = new SearchCriteria();
        nestedSearchCriteria.setKey("createdAt");
        nestedSearchCriteria.setValue(LocalDate.now());

        Join joinResult = mock(Join.class);
        Path nestedKeyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.GREATER_THAN);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(nestedSearchCriteria);
        when(beneficiaryRoot.join(key)).thenReturn(joinResult);
        when(joinResult.get(nestedSearchCriteria.getKey())).thenReturn(nestedKeyPath);
        when(nestedKeyPath.getJavaType()).thenReturn(LocalDate.class);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).greaterThanOrEqualTo(nestedKeyPath, (Comparable) nestedSearchCriteria.getValue());
    }

    @Test
    public void testToPredicateLessThanLocalDateTimeOneToOneRelationOperation() {
        String key = "status";
        SearchCriteria nestedSearchCriteria = new SearchCriteria();
        nestedSearchCriteria.setKey("createdAt");
        nestedSearchCriteria.setValue(LocalDateTime.now());

        Join joinResult = mock(Join.class);
        Path nestedKeyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.GREATER_THAN);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(nestedSearchCriteria);
        when(beneficiaryRoot.join(key)).thenReturn(joinResult);
        when(joinResult.get(nestedSearchCriteria.getKey())).thenReturn(nestedKeyPath);
        when(nestedKeyPath.getJavaType()).thenReturn(LocalDateTime.class);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).greaterThanOrEqualTo(nestedKeyPath, (Comparable) nestedSearchCriteria.getValue());
    }

    @Test
    public void testToPredicateLessThanSerializableOneToOneRelationOperation() {
        String key = "status";
        SearchCriteria nestedSearchCriteria = new SearchCriteria();
        nestedSearchCriteria.setKey("id");
        nestedSearchCriteria.setValue(1L);

        Path nestedKeyPath = mock(Path.class);
        Join joinResult = mock(Join.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.GREATER_THAN);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(nestedSearchCriteria);
        when(beneficiaryRoot.join(key)).thenReturn(joinResult);
        when(joinResult.get(nestedSearchCriteria.getKey())).thenReturn(nestedKeyPath);
        when(nestedKeyPath.getJavaType()).thenReturn(Serializable.class);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).greaterThanOrEqualTo(nestedKeyPath, (Comparable) nestedSearchCriteria.getValue());
    }

    @Test
    public void testToPredicateLikeOneToOneRelationOperation() {
        String key = "status";
        SearchCriteria nestedSearchCriteria = new SearchCriteria();
        nestedSearchCriteria.setKey("name");
        nestedSearchCriteria.setValue("con cobertura");

        Path nestedKeyPath = mock(Path.class);
        Join joinResult = mock(Join.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.LIKE);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(nestedSearchCriteria);
        when(beneficiaryRoot.join(key)).thenReturn(joinResult);
        when(joinResult.get(nestedSearchCriteria.getKey())).thenReturn(nestedKeyPath);
        when(nestedKeyPath.getJavaType()).thenReturn(String.class);
        when(nestedKeyPath.as(String.class)).thenReturn(nestedKeyPath);
        when(criteriaBuilder.upper(nestedKeyPath)).thenReturn(nestedKeyPath);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).upper(nestedKeyPath);
        verify(criteriaBuilder, times(1)).like(criteriaBuilder.upper(nestedKeyPath), nestedSearchCriteria.getValue().toString().toUpperCase());
    }

    @Test
    public void testToPredicateLikeOneToManyAtomicRelationOperation() {
        String key = "listOfStrings.name";
        String[] parsedKey = StringUtils.split(key, DOT);
        String value = "testName";

        Join listKeyJoin = mock(Join.class);
        Path joinedKeyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.LIKE);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(value);
        when(beneficiaryRoot.join(parsedKey[0])).thenReturn(listKeyJoin);
        when(listKeyJoin.get(parsedKey[1])).thenReturn(joinedKeyPath);
        when(joinedKeyPath.getJavaType()).thenReturn(String.class);
        when(joinedKeyPath.as(String.class)).thenReturn(joinedKeyPath);
        when(criteriaBuilder.upper(joinedKeyPath)).thenReturn(joinedKeyPath);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).upper(joinedKeyPath);
        verify(criteriaBuilder, times(1)).like(criteriaBuilder.upper(joinedKeyPath), value.toUpperCase());
    }

    @Test
    public void testToPredicateLikeOneToManyNestedRelationOperation() {
        String key = "insurancePlans.name";
        String[] parsedKey = StringUtils.split(key, DOT);
        SearchCriteria nestedSearchCriteria = new SearchCriteria();
        nestedSearchCriteria.setKey("name");
        nestedSearchCriteria.setValue("plan 1");

        Join listKeyJoin = mock(Join.class);
        Join joinedKeyPath = mock(Join.class);
        Path joinedNestedKeyPath = mock(Path.class);

        when(searchCriteria.getOperation()).thenReturn(SearchOperation.LIKE);
        when(searchCriteria.getKey()).thenReturn(key);
        when(searchCriteria.getValue()).thenReturn(nestedSearchCriteria);
        when(beneficiaryRoot.join(parsedKey[0])).thenReturn(listKeyJoin);
        when(listKeyJoin.join(parsedKey[1])).thenReturn(joinedKeyPath);
        when(joinedKeyPath.get(nestedSearchCriteria.getKey())).thenReturn(joinedNestedKeyPath);
        when(joinedNestedKeyPath.getJavaType()).thenReturn(String.class);
        when(joinedNestedKeyPath.as(String.class)).thenReturn(joinedNestedKeyPath);
        when(criteriaBuilder.upper(joinedNestedKeyPath)).thenReturn(joinedNestedKeyPath);

        modelSpecification.toPredicate(beneficiaryRoot, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).upper(joinedNestedKeyPath);
        verify(criteriaBuilder, times(1)).like(criteriaBuilder.upper(joinedNestedKeyPath), nestedSearchCriteria.getValue().toString().toUpperCase());
    }


}
