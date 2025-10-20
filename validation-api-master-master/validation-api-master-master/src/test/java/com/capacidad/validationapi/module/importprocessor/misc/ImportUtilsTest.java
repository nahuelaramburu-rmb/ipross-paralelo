package com.capacidad.validationapi.module.importprocessor.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportConstants.TENANT_MAPPINGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ImportUtilsTest {

    @Test
    public void testParseIntegerReturnsEmptyWhenInvalid() {
        Optional<Integer> result = ImportUtils.parseInteger("invalid");

        assertThat(result).isNotPresent();
    }

    @Test
    public void testParseIntegerReturnsWhenValid() {
        Optional<Integer> result = ImportUtils.parseInteger("123");

        assertThat(result).contains(123);
    }

    @Test
    public void testParseLongReturnsEmptyWhenInvalid() {
        Optional<Long> result = ImportUtils.parseLong("invalid");

        assertThat(result).isNotPresent();
    }

    @Test
    public void testParseLongReturnsWhenValid() {
        Optional<Long> result = ImportUtils.parseLong("123");

        assertThat(result).contains(123L);
    }

    @Test
    public void testParseDateReturnsEmptyWhenInvalid() {
        Optional<LocalDate> result = ImportUtils.parseDate("invalid");

        assertThat(result).isNotPresent();
    }

    @Test
    public void testParseDateReturnsWhenValid() {
        Optional<LocalDate> result = ImportUtils.parseDate("01/08/1993");

        assertThat(result).contains(LocalDate.of(1993, 8, 1));
    }

    @Test
    public void testGetTenantSimpleMappingReturnsEmptyWhenEmpty() {
        Optional<Object> result = ImportUtils.getTenantMapping(new HashMap<>(), "key");

        assertThat(result).isNotPresent();
    }

    @Test
    public void testGetTenantSimpleMappingReturnsValidWhenNotNull() {
        String property = "property";
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> tenantProperties = new HashMap<>();
        properties.put(TENANT_MAPPINGS, tenantProperties);
        tenantProperties.put("key", property);
        Optional<Object> result = ImportUtils.getTenantMapping(properties, "key");

        assertThat(result).isPresent();
    }

    @Test
    public void testGetTenantCompoundMappingReturnsEmptyWhenEmpty() {
        Optional<Object> result = ImportUtils.getTenantMapping(new HashMap<>(), "container", "key");

        assertThat(result).isNotPresent();
    }

    @Test
    public void testGetTenantCompoundMappingReturnsEmptyWhenNullCompound() {
        Map<String, Object> tenantProperties = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        properties.put(TENANT_MAPPINGS, tenantProperties);

        Optional<Object> result = ImportUtils.getTenantMapping(properties, "container", "key");

        assertThat(result).isNotPresent();
    }

    @Test
    public void testGetTenantCompoundMappingReturnsValidWhenNotEmptyCompound() {
        Map<String, Object> tenantProperties = new HashMap<>();
        Map<String, Object> compoundProperties = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        properties.put(TENANT_MAPPINGS, tenantProperties);
        tenantProperties.put("container", compoundProperties);
        String property = "property";
        compoundProperties.put("key", property);

        Optional<Object> result = ImportUtils.getTenantMapping(properties, "container", "key");

        assertThat(result).isPresent();
    }

    @Test
    public void testStripReturnsEmptyWhenEmptyValue() {
        Optional<String> result = ImportUtils.strip(" ");

        assertThat(result).isNotPresent();
    }

    @Test
    public void testStripReturnsValidWhenNotEmptyValue() {
        Optional<String> result = ImportUtils.strip(" value ");

        assertThat(result).contains("value");
    }

    @Test
    public void testWriteStringAndFlushDoNothingWhenException() throws IOException {
        String value = "value";
        Writer writer = mock(Writer.class);

        doThrow(new IOException("")).when(writer).write(value);

        ImportUtils.writeStringAndFlush(value, writer);

        verify(writer, never()).write("\n");
        verify(writer, never()).flush();
    }

    @Test
    public void testExecuteBatchDoNotExecutesWhenNotDivisibleByBatchSize() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        ImportUtils.executeBatchAndCommit(70, 100, preparedStatement, 100);

        verify(preparedStatement, never()).executeBatch();
        verify(preparedStatement, never()).clearBatch();
    }

    @Test
    public void testExecuteBatchExecutesWhenDivisibleByBatchSize() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);

        when(preparedStatement.getConnection()).thenReturn(connection);

        ImportUtils.executeBatchAndCommit(100, 200, preparedStatement, 100);

        verify(preparedStatement, times(1)).executeBatch();
        verify(preparedStatement, times(1)).clearBatch();
        verify(connection, times(1)).commit();
    }

    @Test
    public void testExecuteBatchExecutesWhenLastElement() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);

        when(preparedStatement.getConnection()).thenReturn(connection);

        ImportUtils.executeBatchAndCommit(37, 37, preparedStatement, 100);

        verify(preparedStatement, times(1)).executeBatch();
        verify(preparedStatement, times(1)).clearBatch();
        verify(connection, times(1)).commit();
    }

}
