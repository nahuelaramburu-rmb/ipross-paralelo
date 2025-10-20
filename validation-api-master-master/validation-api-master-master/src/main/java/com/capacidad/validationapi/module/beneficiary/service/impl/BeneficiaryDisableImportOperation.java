package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.importprocessor.misc.ImportUtils;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportOperationResolver.DISABLE_STATEMENT;
import static com.capacidad.validationapi.module.general.reference.StatusReference.BENEFICIARY_WITHOUT_COVERAGE;

@Component
@Log4j2
public class BeneficiaryDisableImportOperation {

    public ImportReport executeImportOperation(List<Beneficiary> entities, Map<String, PreparedStatement> statementMap, int batchSize) throws SQLException {
        ImportReport importReport = new ImportReport();
        PreparedStatement disableStatement = statementMap.get(DISABLE_STATEMENT);
        int count = 0;
        for (Beneficiary beneficiary : entities) {
            disableStatement.setLong(1, BENEFICIARY_WITHOUT_COVERAGE.getId());
            disableStatement.setTimestamp(2, Timestamp.valueOf(beneficiary.getModifiedAt()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            disableStatement.setString(3, beneficiary.getModifiedBy());
            disableStatement.setString(4, beneficiary.getBeneficiaryCode());
            disableStatement.addBatch();

            count++;

            ImportUtils.executeBatchAndCommit(count, entities.size(), disableStatement, batchSize);
        }
        importReport.setOperations(count);
        return importReport;
    }

}
