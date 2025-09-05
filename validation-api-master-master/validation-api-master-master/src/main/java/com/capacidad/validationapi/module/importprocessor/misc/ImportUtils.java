package com.capacidad.validationapi.module.importprocessor.misc;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportConstants.TENANT_MAPPINGS;

@Log4j2
public final class ImportUtils {

    private ImportUtils() {

    }

    public static <E> Optional<E> filterMap(Map<String, E> map, String key) {
        return Optional.ofNullable(map.get(key));
    }

    public static Optional<Integer> parseInteger(String stringValue) {
        try {
            return Optional.of(Integer.parseInt(stringValue));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLong(String stringValue) {
        try {
            return Optional.of(Long.parseLong(stringValue));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<LocalDate> parseDate(String date) {
        try {
            String formattedBirthDate = StringUtils.strip(StringUtils.replaceChars(date, "-", "/"));
            return Optional.of(LocalDate.parse(formattedBirthDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static Optional<Object> getTenantMapping(Map<String, Object> properties, String key) {
        Map<String, Object> mappings = (Map<String, Object>) properties.get(TENANT_MAPPINGS);
        if (mappings != null)
            return Optional.ofNullable(mappings.get(key));
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static Optional<Object> getTenantMapping(Map<String, Object> properties, String container, String key) {
        Map<String, Object> mappings = (Map<String, Object>) properties.get(TENANT_MAPPINGS);
        if (mappings != null) {
            Map<String, Object> containerMap = (Map<String, Object>) mappings.get(container);
            if (containerMap != null)
                return Optional.ofNullable(containerMap.get(key));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <E> E getProperty(Map<String, Object> properties, String key) {
        return (E) properties.get(key);
    }

    public static Optional<String> strip(String value) {
        String striped = StringUtils.strip(value);
        if (StringUtils.isEmpty(striped))
            return Optional.empty();
        return Optional.of(striped);
    }

    public static String lowerAndCapitalizeStriped(String value) {
        return StringUtils.capitalize(StringUtils.lowerCase(StringUtils.strip(value)));
    }

    public static void writePercentage(long count, long size, Writer writer) {
        BigDecimal percentage = BigDecimal.valueOf(count).divide(BigDecimal.valueOf(size), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.UNNECESSARY);
        ImportUtils.writeStringAndFlush(percentage.toString(), writer);
    }

    public static void writeStringAndFlush(String value, Writer writer) {
        try {
            writer.write(value);
            writer.write("\n");
            writer.flush();
        } catch (IOException | NullPointerException e) {
            log.error("{} writeStringAndFlush - An error occurred trying to write output on OutputStream: {}", ImportUtils.class, e.getMessage());
        }
    }

    public static void executeBatchAndCommit(int count, int size, PreparedStatement preparedStatement, int batchSize) throws SQLException {
        if (count % batchSize == 0 || count == size) {
            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            preparedStatement.getConnection().commit();
        }
    }

    public static void executeBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.executeBatch();
        preparedStatement.clearBatch();
    }

    public static void rollbackTransactions(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error("{} - rollbackTransactions - Error processing rollback: {}", ImportUtils.class, e.getMessage());
        }
    }

}
