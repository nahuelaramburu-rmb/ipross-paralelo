package com.capacidad.validationapi.module.nomenclator.service;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.nomenclator.dto.NomenclatorDTO;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorSearch;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NomenclatorService extends BaseService<Nomenclator, NomenclatorDTO, Long> {

    List<NomenclatorSearch> searchNomenclatorsAndGroups(String name);

    Nomenclator findByNomenclatorCode(String nomenclatorCode) throws ObjectNotFoundException;

    List<NomenclatorProjection.Minor> getMedicalSpecialtyNomenclators(long medicalSpecialtyId) throws ObjectNotFoundException;

    Page<NomenclatorProjection.Minor> getAuditTrayNomenclators(long auditTrayId, Pageable pageable);
}
