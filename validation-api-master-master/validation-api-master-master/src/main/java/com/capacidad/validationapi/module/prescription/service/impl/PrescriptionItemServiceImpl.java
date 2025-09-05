package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionItemDTO;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionItemRepository;
import com.capacidad.validationapi.module.prescription.service.PrescriptionItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionItemServiceImpl extends BaseServiceImpl<PrescriptionItem, PrescriptionItemDTO, Long> implements PrescriptionItemService {
    @Autowired
    public PrescriptionItemServiceImpl(PrescriptionItemRepository repository) {
        super(repository);
    }
}
