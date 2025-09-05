package com.capacidad.validationapi.module.budget.projection;

import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PractitionerBudgetProjection extends BudgetProjection {

    @JsonIgnore
    IdAndNameOnlyProjection getMedicalCenter();

    @JsonIgnore
    PractitionerProjection.Minor getPractitioner();

}
