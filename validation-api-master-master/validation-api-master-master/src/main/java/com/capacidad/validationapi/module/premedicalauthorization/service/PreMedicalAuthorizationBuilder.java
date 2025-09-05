package com.capacidad.validationapi.module.premedicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.premedicalauthorization.dto.BasePreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;

public interface PreMedicalAuthorizationBuilder {

    <P extends BasePreMedicalAuthorizationDTO> PreMedicalAuthorization build(P dto) throws ObjectNotFoundException;

}
