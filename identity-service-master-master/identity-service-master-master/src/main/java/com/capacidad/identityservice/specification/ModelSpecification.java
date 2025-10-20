package com.capacidad.identityservice.specification;

import com.capacidad.identityservice.model.base.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.identityservice.misc.constant.ApplicationConstants.PERCENT_SIGN;

@Log4j2
public class ModelSpecification<T extends BaseEntity<I>, I extends Serializable> implements Specification<T> {

    private static final String PARSE_ERROR = "Criteria Value cannot be parsed: {}";
    private final transient SearchCriteria criteria;
    private final ObjectMapper objectMapper;
    private Class javaType;

    ModelSpecification(SearchCriteria criteria, ObjectMapper objectMapper) {
        this.criteria = criteria;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate toPredicate(
            Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        switch (criteria.getOperation()) {
            case EQUALITY:
                String nullSearch = getCriteriaObjectValue().toString();
                if (StringUtils.equals(nullSearch, "null"))
                    return builder.isNull(getCriteriaStringExpressionKey(root));
                return builder.equal(getCriteriaStringExpressionKey(root), parseValue(nullSearch));
            case NEGATION:
                return builder.notEqual(getCriteriaStringExpressionKey(root), parseValue(getCriteriaObjectValue().toString()));
            case GREATER_THAN:
                return builder.greaterThanOrEqualTo(getCriteriaExpressionKey(root), parseValue(getCriteriaObjectValue().toString()));
            case LESS_THAN:
                return builder.lessThanOrEqualTo(getCriteriaExpressionKey(root), parseValue(getCriteriaObjectValue().toString()));
            case LIKE:
                return builder.like(builder.upper(getCriteriaStringExpressionKey(root).as(String.class)), getCriteriaObjectValue().toString().toUpperCase());
            case STARTS_WITH:
                return builder.like(builder.upper(getCriteriaStringExpressionKey(root).as(String.class)), StringUtils.join(getCriteriaObjectValue().toString().toUpperCase(), PERCENT_SIGN));
            case ENDS_WITH:
                return builder.like(builder.upper(getCriteriaStringExpressionKey(root).as(String.class)), StringUtils.join(PERCENT_SIGN, getCriteriaObjectValue().toString().toUpperCase()));
            case CONTAINS:
                return builder.like(builder.upper(getCriteriaStringExpressionKey(root).as(String.class)), StringUtils.join(PERCENT_SIGN, getCriteriaObjectValue().toString().toUpperCase(), PERCENT_SIGN));
            case IN:
                return builder.equal(getCriteriaExpressionJoinKey(root), parseValue(getCriteriaObjectValue().toString()));
            default:
                return null;
        }
    }

    private Object getCriteriaObjectValue() {
        Object valueExpression = criteria.getValue();
        if (criteria.getValue() instanceof SearchCriteria)
            valueExpression = ((SearchCriteria) criteria.getValue()).getValue();
        return valueExpression;
    }

    private Path<Comparable> getCriteriaExpressionJoinKey(Root<T> root) {
        Path<Comparable> keyPath = criteria.getValue() instanceof SearchCriteria ?
                root.join(criteria.getKey()).get(((SearchCriteria) criteria.getValue()).getKey()) :
                root.join(criteria.getKey());
        javaType = keyPath.getJavaType();
        return keyPath;
    }

    private Path<Comparable> getCriteriaExpressionKey(Root<T> root) {
        Path<Comparable> keyPath = criteria.getValue() instanceof SearchCriteria ?
                root.join(criteria.getKey()).get(((SearchCriteria) criteria.getValue()).getKey()) :
                root.get(criteria.getKey());
        javaType = keyPath.getJavaType();
        return keyPath;
    }

    private Path<String> getCriteriaStringExpressionKey(Root<T> root) {
        if (StringUtils.contains(criteria.getKey(), DOT)) {
            String[] keys = StringUtils.split(criteria.getKey(), DOT);
            return getCompoundCriteriaStringExpressionKey(root, keys);
        }
        Path<String> keyPath = criteria.getValue() instanceof SearchCriteria ?
                root.join(criteria.getKey()).get(((SearchCriteria) criteria.getValue()).getKey()) :
                root.get(criteria.getKey());
        javaType = keyPath.getJavaType();
        return keyPath;
    }

    private Path<String> getCompoundCriteriaStringExpressionKey(Root<T> root, String[] keys) {
        Path<String> keyPath = criteria.getValue() instanceof SearchCriteria ?
                root.join(keys[0]).get(keys[1]).get(((SearchCriteria) criteria.getValue()).getKey()) :
                root.join(keys[0]).get(keys[1]);
        javaType = keyPath.getJavaType();
        return keyPath;
    }

    private Comparable parseValue(String value) {
        Comparable parsedValue = null;
        if (javaType == LocalDate.class)
            parsedValue = parseLocalDate(value);
        if (javaType == LocalDateTime.class)
            parsedValue = parseLocalDateTime(value);
        if (javaType == Serializable.class)
            parsedValue = Long.valueOf(value);
        if (parsedValue == null)
            parsedValue = parse(value);
        return parsedValue;
    }

    private Comparable parseLocalDate(String value) {
        Comparable parsedValue = null;
        try {
            parsedValue = LocalDate.parse(value);
        } catch (Exception ex) {
            log.debug(PARSE_ERROR, ex.getMessage());
        }
        return parsedValue;
    }

    private Comparable parseLocalDateTime(String value) {
        Comparable parsedValue = null;
        try {
            parsedValue = LocalDateTime.parse(value);
        } catch (Exception ex) {
            log.debug(PARSE_ERROR, ex.getMessage());
        }
        return parsedValue;
    }

    private Comparable parse(String value) {
        Comparable parsedValue = null;
        try {
            parsedValue = (Comparable) objectMapper.readValue(objectMapper.writeValueAsBytes(value), javaType);
        } catch (Exception ex) {
            log.debug(PARSE_ERROR, ex.getMessage());
        }
        return parsedValue;
    }

    public SearchCriteria getCriteria() {
        return this.criteria;
    }

}
