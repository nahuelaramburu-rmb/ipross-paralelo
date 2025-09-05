package com.capacidad.validationapi.module.nomenclator.service.impl;

import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.nomenclator.dto.NomenclatorGroupDTO;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorGroup;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorSearch;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorSearchType;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorGroupProjection;
import com.capacidad.validationapi.module.nomenclator.repository.NomenclatorGroupRepository;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NomenclatorGroupServiceImpl extends BaseServiceImpl<NomenclatorGroup, NomenclatorGroupDTO, Long> implements NomenclatorGroupService {

    private final NomenclatorGroupRepository nomenclatorGroupRepository;

    @Autowired
    public NomenclatorGroupServiceImpl(NomenclatorGroupRepository repository) {
        super(repository);
        this.nomenclatorGroupRepository = repository;
    }

    @Override
    public List<NomenclatorSearch> getNomenclatorSearches(String groupName) {
        List<NomenclatorGroupProjection.Extended> results = nomenclatorGroupRepository.findAllByNameContainingIgnoreCase(groupName);
        return results.stream()
                .map(n -> {
                    NomenclatorSearch search = new NomenclatorSearch();
                    search.setType(NomenclatorSearchType.GROUP);
                    search.setContent(n);
                    return search;
                })
                .collect(Collectors.toUnmodifiableList());
    }
}
