package com.capacidad.validationapi.specification;

import org.apache.commons.lang3.StringUtils;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;

public enum SearchOperation {
    EQUALITY, NEGATION, GREATER_THAN, LESS_THAN, LIKE, IN, STARTS_WITH, ENDS_WITH, CONTAINS;

    public static final String OR_PREDICATE_FLAG = "'";
    static final String[] SIMPLE_OPERATION_SET = {":", "!", ">", "<", "~", "-"};
    public static final String OPERATION_EXPRESSION = String.join("|", SearchOperation.SIMPLE_OPERATION_SET);
    private static final String REGEX_PREFIX = "(\\w+?)(";
    private static final String REGEX_SIMPLE_SUFFIX = ")(\\p{Punct}?)([\\p{L}\\p{Digit}\\/\\p{Space}-:\\(]+?)(\\p{Punct}?)";
    public static final String SIMPLE_REGEX_PATTERN = StringUtils.join(REGEX_PREFIX, OPERATION_EXPRESSION, REGEX_SIMPLE_SUFFIX);
    private static final String REGEX_NESTED_SUFFIX = ")(\\{)(\\w+?)(=)(\\p{Punct}?)([\\p{L}\\p{Digit}\\/\\p{Space}-:\\(]+?)(\\p{Punct}?)(})";
    public static final String NESTED_REGEX_PATTERN = StringUtils.join(REGEX_PREFIX, OPERATION_EXPRESSION, REGEX_NESTED_SUFFIX);
    public static final String REFERENCED_REGEX_PATTERN = StringUtils.join("(\\w+\\p{Punct}\\w+?)(", OPERATION_EXPRESSION, REGEX_NESTED_SUFFIX);

    public static SearchOperation getSimpleOperation(String input) {
        switch (input) {
            case COLON:
                return EQUALITY;
            case CLOSING_EXCLAMATION:
                return NEGATION;
            case GREATER_THAN_SIGN:
                return GREATER_THAN;
            case LESS_THAN_SIGN:
                return LESS_THAN;
            case TILDE:
                return LIKE;
            case DASH:
                return IN;
            default:
                return null;
        }
    }

}