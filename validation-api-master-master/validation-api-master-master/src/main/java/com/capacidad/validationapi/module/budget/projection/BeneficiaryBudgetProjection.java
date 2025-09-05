package com.capacidad.validationapi.module.budget.projection;

import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface BeneficiaryBudgetProjection extends BudgetProjection {

    @JsonIgnore
    IdAndNameOnlyProjection getCompany();

    @JsonIgnore
    BeneficiaryProjection.Minor getBeneficiary();

}
