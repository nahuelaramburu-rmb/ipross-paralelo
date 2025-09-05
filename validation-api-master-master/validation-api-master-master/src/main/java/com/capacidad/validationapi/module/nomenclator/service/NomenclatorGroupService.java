package com.capacidad.validationapi.module.nomenclator.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.nomenclator.dto.NomenclatorGroupDTO;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorGroup;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorSearch;

import java.util.List;

public interface NomenclatorGroupService extends BaseService<NomenclatorGroup, NomenclatorGroupDTO, Long> {

    List<NomenclatorSearch> getNomenclatorSearches(String groupName);

}
