package com.capacidad.validationapi.module.prescription.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionItemDTO;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionItemProjection;

import java.util.Set;

public interface PrescriptionItemService extends BaseService<PrescriptionItem, PrescriptionItemDTO, Long> {
}
