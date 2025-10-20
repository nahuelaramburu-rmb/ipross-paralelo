package com.capacidad.validationapi.module.beneficiary.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.HOLDER;

@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryRelationshipDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private IdDTO<Long> relationshipType;

    @Valid
    private IdDTO<Long> relatedBeneficiary;

    public boolean isHolder() {
        return relationshipType.getId().equals(HOLDER.getId());
    }

}
