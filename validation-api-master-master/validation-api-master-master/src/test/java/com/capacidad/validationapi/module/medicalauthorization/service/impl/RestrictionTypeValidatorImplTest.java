package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RestrictionTypeValidatorImplTest {

    @Mock
    private Utils utils;

    @Mock
    private LocaleHandler localeHandler;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RestrictionTypeValidatorImpl restrictionTypeValidator;

    @Before
    public void init() {
        when(localeHandler.getLocaleMessage(anyString(), any())).thenReturn(Optional.empty());
    }

    @Test
    public void testRestrictionIsNotAppliedInMedicalAuthorizationItemWhenRestrictionTypeIsNull() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        FailureType type = FailureType.MEDICAL_COVERAGE;
        String name = "failure";
        String expected = "expectedValue";
        String current = "currentValue";


        RestrictionMessage restrictionMessage = new RestrictionMessage(name, expected, current, null);
        Restriction restriction = restrictionTypeValidator.buildRestriction(null, type, restrictionMessage);

        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getFailures().size()).isEqualTo(1);

        Failure failure = medicalAuthorizationItem.getFailures().iterator().next();

        assertThat(failure.getFailureType()).isEqualTo(type);
        assertThat(failure.getName()).isEqualTo(name);
        assertThat(failure.getAllowed()).isEqualTo(expected);
        assertThat(failure.getCurrent()).isEqualTo(current);
        assertThat(failure.getExtra()).isNull();
        assertThat(medicalAuthorizationItem.getStatus()).isNull();
    }

    @Test
    public void testRestrictionAppliesInMedicalAuthorizationItemWhenRestrictionTypeIsAuditAndStatusIsNull() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setStatus(null);

        FailureType type = FailureType.MEDICAL_COVERAGE;
        String name = "failure";
        String expected = "expectedValue";
        String current = "currentValue";

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.AUDIT.getId());

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_PENDING.getId())).thenReturn(pending);

        RestrictionMessage restrictionMessage = new RestrictionMessage(name, expected, current, null);
        Restriction restriction = restrictionTypeValidator.buildRestriction(restrictionType, type, restrictionMessage);

        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getFailures().size()).isEqualTo(1);

        Failure failure = medicalAuthorizationItem.getFailures().iterator().next();

        assertThat(failure.getFailureType()).isEqualTo(type);
        assertThat(failure.getName()).isEqualTo(name);
        assertThat(failure.getAllowed()).isEqualTo(expected);
        assertThat(failure.getCurrent()).isEqualTo(current);
        assertThat(failure.getExtra()).isNull();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(pending);
    }

    @Test
    public void testRestrictionAppliesInMedicalAuthorizationItemWhenRestrictionTypeIsAuditAndStatusIsApproved() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        FailureType type = FailureType.MEDICAL_COVERAGE;
        String name = "failure";
        String expected = "expectedValue";
        String current = "currentValue";

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.AUDIT.getId());

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        medicalAuthorizationItem.setStatus(approved);

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_PENDING.getId())).thenReturn(pending);

        RestrictionMessage restrictionMessage = new RestrictionMessage(name, expected, current, null);
        Restriction restriction = restrictionTypeValidator.buildRestriction(restrictionType, type, restrictionMessage);

        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getFailures().size()).isEqualTo(1);

        Failure failure = medicalAuthorizationItem.getFailures().iterator().next();

        assertThat(failure.getFailureType()).isEqualTo(type);
        assertThat(failure.getName()).isEqualTo(name);
        assertThat(failure.getAllowed()).isEqualTo(expected);
        assertThat(failure.getCurrent()).isEqualTo(current);
        assertThat(failure.getExtra()).isNull();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(pending);
    }

    @Test
    public void testRestrictionDoNotAppliesInMedicalAuthorizationItemWhenRestrictionTypeIsAuditAndStatusIsAlreadyRejected() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        FailureType type = FailureType.MEDICAL_COVERAGE;
        String name = "failure";
        String expected = "expectedValue";
        String current = "currentValue";

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.AUDIT.getId());

        Status rejected = new Status();
        rejected.setId(VALIDATION_REJECTED.getId());

        medicalAuthorizationItem.setStatus(rejected);

        RestrictionMessage restrictionMessage = new RestrictionMessage(name, expected, current, null);
        Restriction restriction = restrictionTypeValidator.buildRestriction(restrictionType, type, restrictionMessage);

        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getFailures().size()).isEqualTo(1);

        Failure failure = medicalAuthorizationItem.getFailures().iterator().next();

        assertThat(failure.getFailureType()).isEqualTo(type);
        assertThat(failure.getName()).isEqualTo(name);
        assertThat(failure.getAllowed()).isEqualTo(expected);
        assertThat(failure.getCurrent()).isEqualTo(current);
        assertThat(failure.getExtra()).isNull();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(rejected);

        verify(utils, never()).getGenericsEntityReference(Status.class, VALIDATION_PENDING.getId());
    }

    @Test
    public void testRestrictionAppliesInMedicalAuthorizationItemWhenRestrictionTypeIsReject() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        FailureType type = FailureType.MEDICAL_COVERAGE;
        String name = "failure";
        String expected = "expectedValue";
        String current = "currentValue";

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        Status reject = new Status();
        reject.setId(VALIDATION_REJECTED.getId());

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_REJECTED.getId())).thenReturn(reject);

        RestrictionMessage restrictionMessage = new RestrictionMessage(name, expected, current, null);
        Restriction restriction = restrictionTypeValidator.buildRestriction(restrictionType, type, restrictionMessage);

        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getFailures().size()).isEqualTo(1);

        Failure failure = medicalAuthorizationItem.getFailures().iterator().next();

        assertThat(failure.getFailureType()).isEqualTo(type);
        assertThat(failure.getName()).isEqualTo(name);
        assertThat(failure.getAllowed()).isEqualTo(expected);
        assertThat(failure.getCurrent()).isEqualTo(current);
        assertThat(failure.getExtra()).isNull();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(reject);
    }

    @Test
    public void testRestrictionAppliesInMedicalAuthorizationWhenRestrictionTypeIsReject() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorizationItem medicalAuthorizationItem1 = new MedicalAuthorizationItem();

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem1);

        FailureType type = FailureType.MEDICAL_COVERAGE;
        String name = "failure";
        String expected = "expectedValue";
        String current = "currentValue";

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        Status reject = new Status();
        reject.setId(VALIDATION_REJECTED.getId());

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_REJECTED.getId())).thenReturn(reject);

        RestrictionMessageExtra restrictionMessageExtra = new RestrictionMessageExtra(RestrictionMessageExtraType.AUTHORIZATION_ID,
                Collections.singletonList(1L),
                "param");
        RestrictionMessage restrictionMessage = new RestrictionMessage(name, expected, current, restrictionMessageExtra);
        Restriction restriction = restrictionTypeValidator.buildRestriction(restrictionType, type, restrictionMessage);

        JsonNode mockNode = mock(JsonNode.class);
        when(objectMapper.valueToTree(restrictionMessageExtra)).thenReturn(mockNode);

        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorization);

        assertThat(medicalAuthorization.getFailures().size()).isEqualTo(1);

        Failure failure = medicalAuthorization.getFailures().iterator().next();

        assertThat(failure.getFailureType()).isEqualTo(type);
        assertThat(failure.getName()).isEqualTo(name);
        assertThat(failure.getAllowed()).isEqualTo(expected);
        assertThat(failure.getCurrent()).isEqualTo(current);
        assertThat(failure.getExtra()).isEqualTo(mockNode);
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(reject);
        assertThat(medicalAuthorizationItem1.getStatus()).isEqualTo(reject);
    }
}
