package com.capacidad.validationapi.module.beneficiary.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryDTO;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryRelationshipDTO;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryRelativeDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;

public interface BeneficiaryService extends BaseService<Beneficiary, BeneficiaryDTO, Long> {

    Beneficiary createRelative(long holderBeneficiaryId, BeneficiaryRelativeDTO beneficiaryRelativeDto) throws ObjectNotValidException, ObjectNotFoundException;

    BeneficiaryProjection updateStatus(long beneficiaryId, StatusUpdateDTO input) throws ObjectNotValidException, ObjectNotFoundException;

    BeneficiaryProjection updateRelationship(long beneficiaryId, BeneficiaryRelationshipDTO input) throws ObjectNotValidException, ObjectNotFoundException;

    Beneficiary save(Beneficiary beneficiary);

    BeneficiaryProjection associateTradeUnion(long beneficiaryId, long tradeUnionId) throws ObjectNotFoundException, ObjectNotValidException;

    BeneficiaryProjection dissociateTradeUnion(long beneficiaryId, long tradeUnionId) throws ObjectNotFoundException, ObjectNotValidException;

}
