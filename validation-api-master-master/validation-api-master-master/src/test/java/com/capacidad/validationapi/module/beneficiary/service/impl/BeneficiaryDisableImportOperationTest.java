package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportOperationResolver.DISABLE_STATEMENT;
import static com.capacidad.validationapi.module.general.reference.StatusReference.BENEFICIARY_WITHOUT_COVERAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryDisableImportOperationTest {

    @InjectMocks
    private BeneficiaryDisableImportOperation disableImportOperation;

    @Test
    public void testExecuteMultipleImportOperationsReturnsValidReportOnSuccessfulExecution() throws SQLException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode("valid");
        beneficiary.setModifiedAt(LocalDateTime.now());
        beneficiary.setModifiedBy("user");
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        Map<String, PreparedStatement> statementMap = new HashMap<>();
        statementMap.put(DISABLE_STATEMENT, preparedStatement);

        when(preparedStatement.getConnection()).thenReturn(connection);

        ImportReport result = disableImportOperation.executeImportOperation(Collections.singletonList(beneficiary), statementMap, 100);

        assertThat(result.getOperations()).isEqualTo(1);
        verify(preparedStatement, times(1)).setLong(1, BENEFICIARY_WITHOUT_COVERAGE.getId());
        verify(preparedStatement, times(1)).setTimestamp(anyInt(), any(Timestamp.class), any(Calendar.class));
        verify(preparedStatement, times(1)).setString(3, beneficiary.getModifiedBy());
        verify(preparedStatement, times(1)).setString(4, beneficiary.getBeneficiaryCode());
        verify(preparedStatement, times(1)).addBatch();
        verify(preparedStatement, times(1)).executeBatch();
        verify(preparedStatement, times(1)).clearBatch();
        verify(connection, times(1)).commit();
    }


}
