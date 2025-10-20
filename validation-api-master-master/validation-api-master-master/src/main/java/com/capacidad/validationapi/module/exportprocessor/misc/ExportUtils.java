package com.capacidad.validationapi.module.exportprocessor.misc;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.specification.SearchOperation.OPERATION_EXPRESSION;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public final class ExportUtils {

    private ExportUtils() {

    }

    public static String tupleValueOrDefault(Tuple tuple, int index, String defaultVal) {
        return tuple.get(index) != null ? tuple.get(index).toString() : defaultVal;
    }

    public static Set<String> tupleStringArrayToSet(String value, String separator) {
        return new HashSet<>(Arrays.asList(StringUtils.split(value, separator)));
    }

    public static String tupleIsoToLocalDateTimeOrDefault(Tuple tuple, int index, String defaultVal) {
        if (tuple.get(index) == null)
            return defaultVal;
        try {
            return LocalDateTime.parse(tuple.get(index).toString(), ISO_LOCAL_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static String tupleIsoToLocalDateOrDefault(Tuple tuple, int index, String defaultVal) {
        if (tuple.get(index) == null)
            return defaultVal;
        try {
            return LocalDate.parse(tuple.get(index).toString(), ISO_LOCAL_DATE)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static List<Expression<?>> selectionsToExpressions(List<Selection<?>> selections) {
        return selections.stream()
                .map(s -> (Expression<?>) s)
                .collect(Collectors.toList());
    }

    public static String parseAndBuildSpecificationFromParentSearch(String search, String keyToRemove, String keyToAdd) {
        if (StringUtils.isEmpty(search))
            return search;
        String[] searchItems = StringUtils.split(search, COMA);
        String result = "";
        for (int i = 0; i < searchItems.length; i++) {
            String searchItem = searchItems[i];
            if (searchItem.contains(keyToRemove))
                result = StringUtils.replace(search, StringUtils.join(keyToRemove, DOT), "");
            else {
                Pattern pattern = Pattern.compile(OPERATION_EXPRESSION);
                Matcher matcher = pattern.matcher(searchItem);
                if (!searchItem.contains("{") &&
                        matcher.find())
                    result = StringUtils.join(result, resolveCompound(searchItem, keyToAdd, matcher.group(0)));
                else
                    result = StringUtils.join(result, keyToAdd, DOT, searchItem);
            }
            if (i + 1 != searchItems.length)
                result = StringUtils.join(result, COMA);
        }
        return result;
    }

    private static String resolveCompound(String value, String keyToAdd, String operation) {
        String[] splitted = StringUtils.split(value, operation);
        if (splitted.length != 2)
            return "";
        return StringUtils.join(keyToAdd, operation, "{", splitted[0], "=", splitted[1], "}");
    }


}
