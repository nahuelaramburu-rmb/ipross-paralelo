package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryCategory;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.person.model.*;
import com.capacidad.validationapi.module.person.reference.IdTypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.PAYCHECK;
import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportOperationResolver.HOLDER_STATEMENT;
import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportOperationResolver.RELATIVE_STATEMENT;
import static com.capacidad.validationapi.module.general.reference.StatusReference.BENEFICIARY_WITH_COVERAGE;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.HOLDER;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.SON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryCreateUpdateImportOperationTest {

    @Mock
    private Connection connection;

    @Mock
    private CallableStatement holderStatement;

    @Mock
    private CallableStatement relativeStatement;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Array insurancePlans;

    @InjectMocks
    private BeneficiaryCreateUpdateImportOperation createUpdateImportOperation;

    @Test
    public void testExecuteMultipleImportOperationsExecutesSuccessfullyWhenValidMixedData() throws SQLException {
        Beneficiary holderBeneficiary = new Beneficiary();
        Beneficiary relativeBeneficiary = new Beneficiary();

        RelationshipType holder = new RelationshipType();
        holder.setId(HOLDER.getId());

        RelationshipType son = new RelationshipType();
        son.setId(SON.getId());

        holderBeneficiary.setRelationshipType(holder);
        initCommonProperties(holderBeneficiary);
        initHolderProperties(holderBeneficiary);

        relativeBeneficiary.setRelationshipType(son);
        initCommonProperties(relativeBeneficiary);
        initRelativeProperties(relativeBeneficiary);

        List<Beneficiary> beneficiaryList = new ArrayList<>();
        beneficiaryList.add(holderBeneficiary);
        beneficiaryList.add(relativeBeneficiary);

        ObjectMapper objectMapperInstance = new ObjectMapper();
        when(objectMapper.createObjectNode()).thenReturn(objectMapperInstance.createObjectNode());
        when(holderStatement.getConnection()).thenReturn(connection);
        when(relativeStatement.getConnection()).thenReturn(connection);
        when(connection.createArrayOf(anyString(), any())).thenReturn(insurancePlans);
        ImportReport result = createUpdateImportOperation.executeImportOperation(beneficiaryList, buildStatementMap(), 100);

        assertThat(result.getOperations()).isEqualTo(2);
        verifyHolderProperties(beneficiaryList.get(0));
        verifyRelativeProperties(beneficiaryList.get(1));
        verify(holderStatement, times(1)).addBatch();
        verify(holderStatement, times(1)).executeBatch();
        verify(holderStatement, times(1)).clearBatch();
        verify(relativeStatement, times(1)).addBatch();
        verify(relativeStatement, times(1)).executeBatch();
        verify(relativeStatement, times(1)).clearBatch();
        verify(connection, times(1)).commit();
    }


    private void initCommonProperties(Beneficiary beneficiary) {
        beneficiary.setClientId("clientId");
        beneficiary.setCreatedAt(LocalDateTime.now());
        beneficiary.setCreatedBy("user");
        beneficiary.setModifiedAt(LocalDateTime.now());
        beneficiary.setModifiedBy("user");
        beneficiary.setTenantId(UUID.randomUUID());
        Address address = new Address();
        City city = new City();
        city.setId(1L);
        address.setApartment("1b");
        address.setDistrict("district");
        address.setStreet("street");
        address.setStreetNumber(1234);
        address.setCity(city);
        beneficiary.setAddress(address);
        Phone phone = new Phone();
        phone.setPhoneNumber(123456780L);
        phone.setPhoneType(PhoneType.MOVIL);
        beneficiary.setPhone(phone);
        beneficiary.setBirthDate(LocalDate.of(1990, 8, 10));
        beneficiary.setEmail("email@test.com");
        beneficiary.setGender(Gender.FEMENINO);
        beneficiary.setIdNumber(332211L);
        beneficiary.setWorkIdNumber(203322115L);
        IdType defaultIdType = new IdType();
        defaultIdType.setId(IdTypeReference.ID.getId());
        beneficiary.setIdType(defaultIdType);
        beneficiary.setName("name");
        beneficiary.setLastName("lastName");
        beneficiary.setBeneficiaryCode("aaaaa-55555");
        beneficiary.setMaritalStatus(null);
        BeneficiaryInsurancePlan beneficiaryInsurancePlan = new BeneficiaryInsurancePlan();
        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);
        beneficiaryInsurancePlan.setInsurancePlan(insurancePlan);
        beneficiaryInsurancePlan.setPriority(2);
        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryInsurancePlan);
    }

    private void initHolderProperties(Beneficiary beneficiary) {
        BeneficiaryCategory beneficiaryCategory = new BeneficiaryCategory();
        beneficiaryCategory.setId(1L);
        Company company = new Company();
        company.setId(1L);
        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());
        beneficiary.setBeneficiaryCategory(beneficiaryCategory);
        beneficiary.setCompany(company);
        beneficiary.setPaymentMethod(paycheck);
    }

    private void initRelativeProperties(Beneficiary beneficiary) {
        Beneficiary relatedBeneficiary = new Beneficiary();
        relatedBeneficiary.setBeneficiaryCode("aaaaa-55555");
        beneficiary.setRelatedBeneficiary(relatedBeneficiary);
    }

    private void verifyHolderProperties(Beneficiary beneficiary) throws SQLException {
        verifyCommonProperties(holderStatement, beneficiary);
        verify(holderStatement, times(1)).setLong(29, beneficiary.getBeneficiaryCategory().getId());
        verify(holderStatement, times(1)).setLong(30, beneficiary.getCompany().getId());
        verify(holderStatement, times(1)).setObject(31, beneficiary.getFamilyId());
        verify(holderStatement, times(1)).setObject(32, beneficiary.getPaymentMethod().getId());
    }

    private void verifyRelativeProperties(Beneficiary beneficiary) throws SQLException {
        verifyCommonProperties(relativeStatement, beneficiary);
        verify(relativeStatement, times(1)).setString(29, beneficiary.getRelatedBeneficiary().getBeneficiaryCode());
    }

    private void verifyCommonProperties(CallableStatement callableStatement, Beneficiary beneficiary) throws SQLException {
        Phone phone = beneficiary.getPhone();
        String phoneType = Optional.ofNullable(phone).map(p -> p.getPhoneType().name()).orElse(null);
        Long phoneNumber = Optional.ofNullable(phone).map(Phone::getPhoneNumber).orElse(null);
        verify(callableStatement, times(1)).setString(1, beneficiary.getClientId());
        verify(callableStatement, times(1)).setString(3, beneficiary.getCreatedBy());
        verify(callableStatement, times(2)).setTimestamp(anyInt(), any(Timestamp.class), any(Calendar.class));
        verify(callableStatement, times(1)).setString(5, beneficiary.getModifiedBy());
        verify(callableStatement, times(1)).setObject(6, beneficiary.getTenantId());
        verify(callableStatement, times(1)).setString(7, beneficiary.getAddress().getApartment());
        verify(callableStatement, times(1)).setString(8, beneficiary.getAddress().getDistrict());
        verify(callableStatement, times(1)).setString(9, beneficiary.getAddress().getStreet());
        verify(callableStatement, times(1)).setInt(10, beneficiary.getAddress().getStreetNumber());
        verify(callableStatement, times(1)).setLong(11, beneficiary.getAddress().getCity().getId());
        verify(callableStatement, times(1)).setLong(12, Objects.requireNonNullElse(phoneNumber, 0L));
        verify(callableStatement, times(1)).setString(13, phoneType);
        verify(callableStatement, times(1)).setDate(14, Date.valueOf(beneficiary.getBirthDate()));
        verify(callableStatement, times(1)).setString(15, beneficiary.getEmail());
        verify(callableStatement, times(1)).setString(16, beneficiary.getGender().name());
        verify(callableStatement, times(1)).setLong(17, beneficiary.getIdNumber());
        verify(callableStatement, times(1)).setLong(18, beneficiary.getIdType().getId());
        verify(callableStatement, times(1)).setString(19, beneficiary.getLastName());
        verify(callableStatement, times(1)).setNull(20, Types.BIGINT);
        verify(callableStatement, times(1)).setString(21, beneficiary.getName());
        verify(callableStatement, times(1)).setLong(22, beneficiary.getWorkIdNumber());
        verify(callableStatement, times(1)).setString(23, beneficiary.getBeneficiaryCode());
        verify(callableStatement, times(1)).setObject(24, beneficiary.getResourceId());
        verify(callableStatement, times(1)).setLong(25, beneficiary.getRelationshipType().getId());
        verify(callableStatement, times(1)).setLong(26, BENEFICIARY_WITH_COVERAGE.getId());
        verify(callableStatement, times(1)).setArray(27, insurancePlans);
        verify(callableStatement, times(1)).setLong(28, HOLDER.getId());
    }

    private Map<String, PreparedStatement> buildStatementMap() {
        Map<String, PreparedStatement> statementMap = new HashMap<>();
        statementMap.put(HOLDER_STATEMENT, holderStatement);
        statementMap.put(RELATIVE_STATEMENT, relativeStatement);
        return statementMap;
    }

}
