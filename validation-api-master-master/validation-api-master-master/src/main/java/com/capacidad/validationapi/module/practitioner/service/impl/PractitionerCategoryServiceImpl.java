package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.general.dto.NameDTO;
import com.capacidad.validationapi.module.practitioner.model.PractitionerCategory;
import com.capacidad.validationapi.module.practitioner.repository.PractitionerCategoryRepository;
import com.capacidad.validationapi.module.practitioner.service.PractitionerCategoryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class PractitionerCategoryServiceImpl extends BaseServiceImpl<PractitionerCategory, NameDTO, Long> implements PractitionerCategoryService {
    @Autowired
    public PractitionerCategoryServiceImpl(PractitionerCategoryRepository repository) {
        super(repository);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerCategory practitionerCategory = this.findById(objectId);
        if (!practitionerCategory.getContractItems().isEmpty() && !allContractItemsDeleted(practitionerCategory.getContractItems()))
            throw new ObjectNotValidException("practitionerCategory.cannotDeleteContractItemsAttached");
        practitionerCategory.getPractitioners().forEach(p -> p.setPractitionerCategory(null));
        practitionerCategory.setDeleted(true);
        practitionerCategory.setDeletionToken(UUID.randomUUID());
        this.getRepository().save(practitionerCategory);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(practitionerCategory, this.getRepository()));
        return this.buildIdResponse(objectId);
    }

    private boolean allContractItemsDeleted(Set<ContractItem> contractItems) {
        return contractItems.size() == contractItems.stream()
                .filter(BaseEntity::getDeleted)
                .count();
    }

}
