package com.capacidad.validationapi.module.settlement.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationController;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalcenter.controller.MedicalCenterController;
import com.capacidad.validationapi.module.nomenclator.controller.NomenclatorController;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.capacidad.validationapi.module.settlement.projection.SettlementItemProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

public class SettlementItemResource extends EntityModel<SettlementItemProjection> {

    private final SelfModel<IdAndNameOnlyProjection, Long> medicalCenter;
    private final SelfModel<NomenclatorProjection.Minor, Long> nomenclator;
    private final SelfModel<MedicalAuthorizationProjection.IdCreationDateBeneficiary, Long> medicalAuthorization;

    public SettlementItemResource(SettlementItemProjection settlementItemProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(settlementItemProjection);
        medicalCenter = new SelfModel<>(settlementItemProjection.getMedicalCenter(), MedicalCenterController.class);
        nomenclator = new SelfModel<>(settlementItemProjection.getNomenclator(), NomenclatorController.class);
        medicalAuthorization = new SelfModel<>(settlementItemProjection.getMedicalAuthorization(), MedicalAuthorizationController.class);
    }

    @JsonProperty("medicalCenter")
    public SelfModel<IdAndNameOnlyProjection, Long> getMedicalCenter() {
        return medicalCenter;
    }

    @JsonProperty("nomenclator")
    public SelfModel<NomenclatorProjection.Minor, Long> getNomenclator() {
        return nomenclator;
    }

    @JsonProperty("medicalAuthorization")
    public SelfModel<MedicalAuthorizationProjection.IdCreationDateBeneficiary, Long> getMedicalAuthorization() {
        return medicalAuthorization;
    }

}
