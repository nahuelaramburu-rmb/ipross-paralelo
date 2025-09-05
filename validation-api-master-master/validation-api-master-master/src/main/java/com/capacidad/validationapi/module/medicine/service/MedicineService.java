package com.capacidad.validationapi.module.medicine.service;

import com.capacidad.validationapi.prescription.integration.ProductWrapperDTO;

public interface MedicineService {

    ProductWrapperDTO getProducts(String name, String beneficiaryCode);

}
