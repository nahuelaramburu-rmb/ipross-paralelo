package com.capacidad.validationapi.specification;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.ASTERISK;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.COMA;

@Service("specBuilder")
public class SpecificationBuilder<T extends BaseEntity<I>, I extends Serializable> {

    private final Pattern simplePattern;
    private final Pattern nestedPattern;
    private final Pattern referencedPattern;
    private final ObjectMapper objectMapper;

    @Autowired
    public SpecificationBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        simplePattern = Pattern.compile(SearchOperation.SIMPLE_REGEX_PATTERN);
        nestedPattern = Pattern.compile(SearchOperation.NESTED_REGEX_PATTERN);
        referencedPattern = Pattern.compile(SearchOperation.REFERENCED_REGEX_PATTERN);
    }

    public Optional<Specification<T>> parseAndBuild(String search) {
        if (StringUtils.isBlank(search))
            return Optional.empty();

        List<SearchCriteria> params = new ArrayList<>();

        String[] searchQueries = StringUtils.split(search, COMA);
        for (String query : searchQueries) {
            Matcher simpleMatcher = simplePattern.matcher(query);
            if (simpleMatcher.matches()) {
                processSimpleMatcher(params, simpleMatcher);
            } else {
                Matcher compoundMatcher = nestedPattern.matcher(query);
                if (compoundMatcher.matches())
                    processCompoundMatcher(params, compoundMatcher);
                else {
                    Matcher referencedMatcher = referencedPattern.matcher(query);
                    if (referencedMatcher.matches())
                        processReferencedMatcher(params, referencedMatcher);
                }
            }
        }
        return build(params);
    }

    private void processSimpleMatcher(List<SearchCriteria> params, Matcher simpleMatcher) {
        with(params,
                simpleMatcher.group(1),
                simpleMatcher.group(2),
                simpleMatcher.group(4),
                simpleMatcher.group(3),
                simpleMatcher.group(5));
    }

    private void processCompoundMatcher(List<SearchCriteria> params, Matcher compoundMatcher) {
        with(params,
                compoundMatcher.group(1),
                compoundMatcher.group(2),
                StringUtils.join(
                        compoundMatcher.group(4),
                        compoundMatcher.group(5),
                        compoundMatcher.group(7)),
                compoundMatcher.group(6),
                compoundMatcher.group(8));
    }

    private void processReferencedMatcher(List<SearchCriteria> params, Matcher referencedMatcher) {
        with(params,
                referencedMatcher.group(1),
                referencedMatcher.group(2),
                StringUtils.join(
                        referencedMatcher.group(4),
                        referencedMatcher.group(5),
                        referencedMatcher.group(7)),
                referencedMatcher.group(6),
                referencedMatcher.group(8));
    }

    private void with(List<SearchCriteria> params,
                      String key, String operation, Object value, String prefix, String suffix) {

        SearchOperation op = SearchOperation.getSimpleOperation(StringUtils.substring(operation, 0, 1));
        if (op == SearchOperation.EQUALITY) {
            boolean startWithAsterisk = prefix.contains(ASTERISK);
            boolean endWithAsterisk = suffix.contains(ASTERISK);

            if (startWithAsterisk && endWithAsterisk) {
                op = SearchOperation.CONTAINS;
            } else if (startWithAsterisk) {
                op = SearchOperation.ENDS_WITH;
            } else if (endWithAsterisk) {
                op = SearchOperation.STARTS_WITH;

            }
        }
        params.add(new SearchCriteria(null, key, op, value));
    }

    private Optional<Specification<T>> build(List<SearchCriteria> params) {
        if (params.isEmpty())
            return Optional.empty();

        Specification<T> result = new ModelSpecification<>(params.get(0), objectMapper);

        for (int i = 1; i < params.size(); i++) {
            result = params.get(i).isOrPredicate()
                    ? Objects.requireNonNull(Specification.where(result)).or(new ModelSpecification<>(params.get(i), objectMapper))
                    : Objects.requireNonNull(Specification.where(result)).and(new ModelSpecification<>(params.get(i), objectMapper));
        }
        return Optional.ofNullable(result);
    }
}
