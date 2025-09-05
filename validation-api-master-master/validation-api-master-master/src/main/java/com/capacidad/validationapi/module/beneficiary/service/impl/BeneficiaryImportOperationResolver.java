package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.importprocessor.misc.ImportUtils;
import com.capacidad.validationapi.module.importprocessor.model.ImportOperation;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import com.capacidad.validationapi.module.importprocessor.model.OperationResolverProperties;
import com.capacidad.validationapi.module.importprocessor.service.ImportOperationResolver;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
@Log4j2
public class BeneficiaryImportOperationResolver implements ImportOperationResolver<Beneficiary, Long> {

    protected static final String HOLDER_STATEMENT = "holderStatement";
    protected static final String RELATIVE_STATEMENT = "relativeStatement";
    protected static final String DISABLE_STATEMENT = "disableStatement";
    private final BeneficiaryCreateUpdateImportOperation createUpdateImportOperation;
    private final BeneficiaryDisableImportOperation disableImportOperation;
    private final DataSource processingDataSource;

    @Autowired
    public BeneficiaryImportOperationResolver(BeneficiaryCreateUpdateImportOperation createUpdateImportOperation,
                                              BeneficiaryDisableImportOperation disableImportOperation,
                                              @Qualifier("processingDataSource") DataSource processingDataSource) {
        this.createUpdateImportOperation = createUpdateImportOperation;
        this.disableImportOperation = disableImportOperation;
        this.processingDataSource = processingDataSource;
    }

    /**
     * Processing Pool autoCommit disabled by default.
     * If used pool autoCommit is true then
     * obtained Connection should be autoCommit = false.
     * Sleep between batches in order to avoid database overload.
     **/
    @Override
    public ImportReport executeImportOperation(OperationResolverProperties<Beneficiary> operationResolverProperties) throws ObjectNotValidException {
        if (operationResolverProperties.getImportProperties().getOperation() == null)
            throw new ObjectNotValidException("import.invalidOperation");
        Connection connection = DataSourceUtils.getConnection(processingDataSource);
        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            if (operationResolverProperties.getImportProperties().getOperation() == ImportOperation.CREATE_UPDATE)
                return executeCreateUpdateOperations(connection, operationResolverProperties);
            return executeDisableOperations(connection, operationResolverProperties);
        } catch (SQLException e) {
            log.error("{} - executeImportOperation - Error processing import statements: {}", this.getClass(), e.getMessage());
            ImportUtils.rollbackTransactions(connection);
        } finally {
            restoreAutoCommit(connection, autoCommit);
            DataSourceUtils.releaseConnection(connection, processingDataSource);
        }
        return new ImportReport();
    }

    private ImportReport executeCreateUpdateOperations(Connection connection, OperationResolverProperties<Beneficiary> operationResolverProperties) throws SQLException {
        try (PreparedStatement holderStatement = buildHolderStatement(connection);
             PreparedStatement relativeStatement = buildRelativeStatement(connection)) {
            Map<String, PreparedStatement> statementMap = new HashMap<>();
            statementMap.put(HOLDER_STATEMENT, holderStatement);
            statementMap.put(RELATIVE_STATEMENT, relativeStatement);
            return createUpdateImportOperation.executeImportOperation(operationResolverProperties.getObjects(),
                    statementMap,
                    operationResolverProperties.getBatchSize());
        }
    }

    private ImportReport executeDisableOperations(Connection connection, OperationResolverProperties<Beneficiary> operationResolverProperties) throws SQLException {
        try (PreparedStatement disableStatement = buildDisableStatement(connection)) {
            Map<String, PreparedStatement> statementMap = new HashMap<>();
            statementMap.put(DISABLE_STATEMENT, disableStatement);
            return disableImportOperation.executeImportOperation(operationResolverProperties.getObjects(),
                    statementMap,
                    operationResolverProperties.getBatchSize());
        }
    }

    private PreparedStatement buildHolderStatement(Connection connection) throws SQLException {
        return connection.prepareCall("call holder_creation(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?)");
    }

    private PreparedStatement buildRelativeStatement(Connection connection) throws SQLException {
        return connection.prepareCall("call relative_creation(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
    }

    private PreparedStatement buildDisableStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("update beneficiary set status_id = ?, modified_at = ?, modified_by = ? where beneficiary_code like ?");
    }

    private void restoreAutoCommit(Connection connection, boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            log.error("({}) - restoreAutoCommit: {}", this.getClass(), e.getMessage());
        }
    }

}
