package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.importprocessor.model.ImportOperation;
import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import com.capacidad.validationapi.module.importprocessor.model.OperationResolverProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryImportOperationResolverTest {

    @Mock
    private BeneficiaryCreateUpdateImportOperation createUpdateImportOperation;

    @Mock
    private BeneficiaryDisableImportOperation disableImportOperation;

    @Mock
    private DataSource dataSource;

    @Spy
    @InjectMocks
    private BeneficiaryImportOperationResolver importOperationResolver;

    @Test
    public void testExecuteImportOperationThrowsExceptionWhenNullStrategy() {
        OperationResolverProperties<Beneficiary> operationResolverProperties = mock(OperationResolverProperties.class);
        ImportProperties importProperties = mock(ImportProperties.class);

        when(operationResolverProperties.getImportProperties()).thenReturn(importProperties);
        when(importProperties.getOperation()).thenReturn(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importOperationResolver
                .executeImportOperation(operationResolverProperties));

        assertThat(exception.getMessage()).isEqualTo("import.invalidOperation");
    }

    @Test
    public void testExecuteImportOperationExecutesCreateWhenValidOperation() throws ObjectNotValidException, SQLException {
        OperationResolverProperties<Beneficiary> operationResolverProperties = mock(OperationResolverProperties.class);
        ImportProperties importProperties = mock(ImportProperties.class);

        Beneficiary beneficiary = new Beneficiary();
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(createUpdateImportOperation.executeImportOperation(anyList(), anyMap(), anyInt())).thenReturn(new ImportReport());
        when(operationResolverProperties.getObjects()).thenReturn(Collections.singletonList(beneficiary));
        when(operationResolverProperties.getImportProperties()).thenReturn(importProperties);
        when(importProperties.getOperation()).thenReturn(ImportOperation.CREATE_UPDATE);

        ImportReport result = importOperationResolver.executeImportOperation(operationResolverProperties);

        assertThat(result).isNotNull();
        verify(disableImportOperation, never()).executeImportOperation(anyList(), anyMap(), anyInt());
    }

    @Test
    public void testExecuteImportOperationExecutesDisableWhenValidOperation() throws ObjectNotValidException, SQLException {
        OperationResolverProperties<Beneficiary> operationResolverProperties = mock(OperationResolverProperties.class);
        ImportProperties importProperties = mock(ImportProperties.class);

        Beneficiary beneficiary = new Beneficiary();
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(disableImportOperation.executeImportOperation(anyList(), anyMap(), anyInt())).thenReturn(new ImportReport());
        when(operationResolverProperties.getObjects()).thenReturn(Collections.singletonList(beneficiary));
        when(operationResolverProperties.getImportProperties()).thenReturn(importProperties);
        when(importProperties.getOperation()).thenReturn(ImportOperation.DISABLE);

        ImportReport result = importOperationResolver.executeImportOperation(operationResolverProperties);

        assertThat(result).isNotNull();
        verify(createUpdateImportOperation, never()).executeImportOperation(anyList(), anyMap(), anyInt());
    }

}
