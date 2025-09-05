package com.capacidad.validationapi.module.medicine.controller;

import com.capacidad.validationapi.module.medicine.service.MedicineService;
import com.capacidad.validationapi.prescription.integration.ProductWrapperDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_MEDICINES;

@RestController
@RequestMapping(value = ENDPOINT_MEDICINES)
public class MedicineController {

    private MedicineService medicineService;

    @Autowired
    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping(params = {"name", "beneficiaryCode"})
    public ResponseEntity<ProductWrapperDTO> getProducts(@RequestParam String name, @RequestParam String beneficiaryCode) {
        return ResponseEntity.ok(medicineService.getProducts(name, beneficiaryCode));
    }

}
