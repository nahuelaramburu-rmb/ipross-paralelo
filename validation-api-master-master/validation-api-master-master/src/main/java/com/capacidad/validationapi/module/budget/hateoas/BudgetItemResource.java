package com.capacidad.validationapi.module.budget.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.budget.projection.BudgetItemProjection;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationController;
import com.capacidad.validationapi.module.nomenclator.controller.NomenclatorController;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

public class BudgetItemResource extends EntityModel<BudgetItemProjection> {

    private final SelfModel<BaseProjection<Long>, Long> authorization;
    private final SelfModel<NomenclatorProjection.Minor, Long> nomenclator;

    public BudgetItemResource(BudgetItemProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        authorization = new SelfModel<>(projection.getMedicalAuthorization(), MedicalAuthorizationController.class);
        nomenclator = new SelfModel<>(projection.getNomenclator(), NomenclatorController.class);
    }

    @JsonProperty("authorization")
    public SelfModel<BaseProjection<Long>, Long> getAuthorization() {
        return authorization;
    }

    @JsonProperty("nomenclator")
    public SelfModel<NomenclatorProjection.Minor, Long> getNomenclator() {
        return nomenclator;
    }

}
