package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.importprocessor.misc.ImportUtils;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import com.capacidad.validationapi.module.person.model.MaritalStatus;
import com.capacidad.validationapi.module.person.model.Phone;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.functional.ThrowingRunnable.throwingRunnable;
import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportOperationResolver.HOLDER_STATEMENT;
import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportOperationResolver.RELATIVE_STATEMENT;
import static com.capacidad.validationapi.module.general.reference.StatusReference.BENEFICIARY_WITH_COVERAGE;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.HOLDER;

@Log4j2
@Component
public class BeneficiaryCreateUpdateImportOperation {

    private final ObjectMapper objectMapper;

    @Autowired
    public BeneficiaryCreateUpdateImportOperation(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ImportReport executeImportOperation(List<Beneficiary> entities, Map<String, PreparedStatement> statementMap, int batchSize) throws SQLException {
        ImportReport importReport = new ImportReport();
        PreparedStatement holderStatement = statementMap.get(HOLDER_STATEMENT);
        PreparedStatement relativeStatement = statementMap.get(RELATIVE_STATEMENT);
        int count = 0;
        final int lastCommonIndex = 28;
        boolean lastHolder = false;
        List<Beneficiary> sortedList = entities.stream().
                sorted(Comparator.comparing(a -> a.getRelationshipType().getId()))
                .collect(Collectors.toList());
        for (Beneficiary beneficiary : sortedList) {
            if (beneficiary.getRelationshipType().getId().equals(HOLDER.getId())) {
                setBeneficiaryCommonParameters(holderStatement, beneficiary);
                setBeneficiaryHolderParameters(holderStatement, beneficiary, lastCommonIndex);
                holderStatement.addBatch();
                count++;
                ImportUtils.executeBatchAndCommit(count, entities.size(), holderStatement, batchSize);
            } else {
                if (!lastHolder)
                    ImportUtils.executeBatch(holderStatement);
                lastHolder = true;
                setBeneficiaryCommonParameters(relativeStatement, beneficiary);
                setBeneficiaryRelativeParameters(relativeStatement, beneficiary, lastCommonIndex);
                relativeStatement.addBatch();
                count++;
                ImportUtils.executeBatchAndCommit(count, entities.size(), relativeStatement, batchSize);
            }
        }
        importReport.setOperations(count);
        return importReport;
    }

    private void setBeneficiaryCommonParameters(PreparedStatement preparedStatement, Beneficiary beneficiary) throws SQLException {
        Phone phone = beneficiary.getPhone();
        String phoneType = Optional.ofNullable(phone).map(p -> p.getPhoneType().name()).orElse(null);
        MaritalStatus maritalStatus = beneficiary.getMaritalStatus();
        Long phoneNumber = Optional.ofNullable(phone).map(Phone::getPhoneNumber).orElse(null);
        Long maritalStatusId = maritalStatus != null ? maritalStatus.getId() : null;
        preparedStatement.setString(1, beneficiary.getClientId());
        preparedStatement.setTimestamp(2, Timestamp.valueOf(beneficiary.getCreatedAt()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
        preparedStatement.setTimestamp(2, Timestamp.valueOf(beneficiary.getCreatedAt()));
        preparedStatement.setString(3, beneficiary.getCreatedBy());
        preparedStatement.setTimestamp(4, Timestamp.valueOf(beneficiary.getModifiedAt()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
        preparedStatement.setString(5, beneficiary.getModifiedBy());
        preparedStatement.setObject(6, beneficiary.getTenantId());
        preparedStatement.setString(7, beneficiary.getAddress().getApartment());
        preparedStatement.setString(8, beneficiary.getAddress().getDistrict());
        preparedStatement.setString(9, beneficiary.getAddress().getStreet());
        setNullableIntegerStatement(preparedStatement, 10, beneficiary.getAddress().getStreetNumber());
        preparedStatement.setLong(11, beneficiary.getAddress().getCity().getId());
        preparedStatement.setLong(12, Objects.requireNonNullElse(phoneNumber, 0L));
        preparedStatement.setString(13, phoneType);
        preparedStatement.setDate(14, Date.valueOf(beneficiary.getBirthDate()));
        preparedStatement.setString(15, beneficiary.getEmail());
        preparedStatement.setString(16, beneficiary.getGender().name());
        preparedStatement.setLong(17, beneficiary.getIdNumber());
        preparedStatement.setLong(18, beneficiary.getIdType().getId());
        preparedStatement.setString(19, beneficiary.getLastName());
        setNullableLongStatement(preparedStatement, 20, maritalStatusId);
        preparedStatement.setString(21, beneficiary.getName());
        setNullableLongStatement(preparedStatement, 22, beneficiary.getWorkIdNumber());
        preparedStatement.setString(23, beneficiary.getBeneficiaryCode());
        preparedStatement.setObject(24, beneficiary.getResourceId());
        preparedStatement.setLong(25, beneficiary.getRelationshipType().getId());
        preparedStatement.setLong(26, BENEFICIARY_WITH_COVERAGE.getId());
        preparedStatement.setArray(27, preparedStatement.getConnection()
                .createArrayOf("varchar", buildInsurancePlansObjects(beneficiary)));
        preparedStatement.setLong(28, HOLDER.getId());
    }

    private void setNullableLongStatement(PreparedStatement preparedStatement, int index, Long value) {
        Optional.ofNullable(value).ifPresentOrElse(throwingConsumer(v -> preparedStatement.setLong(index, v)), throwingRunnable(() -> preparedStatement.setNull(index, Types.BIGINT)));
    }

    private void setNullableIntegerStatement(PreparedStatement preparedStatement, int index, Integer value) {
        Optional.ofNullable(value).ifPresentOrElse(throwingConsumer(v -> preparedStatement.setInt(index, v)), throwingRunnable(() -> preparedStatement.setNull(index, Types.INTEGER)));
    }

    private String[] buildInsurancePlansObjects(Beneficiary beneficiary) {
        String[] stringArray = new String[beneficiary.getBeneficiaryInsurancePlans().size()];
        int i = 0;
        for (BeneficiaryInsurancePlan beneficiaryInsurancePlan : beneficiary.getBeneficiaryInsurancePlans()) {
            String expirationDate = Optional.ofNullable(beneficiaryInsurancePlan.getExpirationDate()).map(LocalDate::toString).orElse(null);
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("insurancePlanId", beneficiaryInsurancePlan.getInsurancePlan().getId());
            objectNode.put("priority", beneficiaryInsurancePlan.getPriority());
            objectNode.put("expirationDate", expirationDate);
            stringArray[i] = objectNode.toString();
            i++;
        }
        return stringArray;
    }

    private void setBeneficiaryHolderParameters(PreparedStatement preparedStatement, Beneficiary beneficiary, int lastCommonIndex) throws SQLException {
        Long beneficiaryCategoryId = Optional.ofNullable(beneficiary.getBeneficiaryCategory()).map(BaseEntity::getId).orElse(null);
        Long companyId = Optional.ofNullable(beneficiary.getCompany()).map(BaseEntity::getId).orElse(null);
        setNullableLongStatement(preparedStatement, lastCommonIndex + 1, beneficiaryCategoryId);
        setNullableLongStatement(preparedStatement, lastCommonIndex + 2, companyId);
        preparedStatement.setObject(lastCommonIndex + 3, beneficiary.getFamilyId());
        preparedStatement.setObject(lastCommonIndex + 4, beneficiary.getPaymentMethod().getId());
    }

    private void setBeneficiaryRelativeParameters(PreparedStatement preparedStatement, Beneficiary beneficiary, int lastCommonIndex) {
        String relatedBeneficiaryCode = Optional.ofNullable(beneficiary.getRelatedBeneficiary()).map(Beneficiary::getBeneficiaryCode).orElse(null);
        setNullableStringStatement(preparedStatement, lastCommonIndex + 1, relatedBeneficiaryCode);
    }

    private void setNullableStringStatement(PreparedStatement preparedStatement, int index, String value) {
        Optional.ofNullable(value).ifPresentOrElse(throwingConsumer(v -> preparedStatement.setString(index, v)), throwingRunnable(() -> preparedStatement.setNull(index, Types.VARCHAR)));
    }

}
