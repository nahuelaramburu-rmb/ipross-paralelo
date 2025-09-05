package com.capacidad.validationapi.module.general.controller;

import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.person.projection.IdTypeProjection;
import com.capacidad.validationapi.module.person.service.PersonService;
import com.capacidad.validationapi.module.practitioner.projection.MedicalSpecialtyProjection;
import com.capacidad.validationapi.module.practitioner.service.MedicalSpecialtyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_GENERAL)
public class GeneralController {

    private final MedicalSpecialtyService medicalSpecialtyService;
    private final PersonService personService;
    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public GeneralController(MedicalSpecialtyService medicalSpecialtyService,
                             PersonService personService,
                             BeneficiaryFinder beneficiaryFinder) {
        this.medicalSpecialtyService = medicalSpecialtyService;
        this.personService = personService;
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @GetMapping(value = "/medical-specialty-types")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllMedicalSpecialtyTypes() {
        return ResponseEntity.ok(medicalSpecialtyService.getAllMedicalSpecialtyTypes());
    }

    @GetMapping(value = "/medical-specialty-types/{medicalSpecialtyTypeId}")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getMedicalSpecialties(@PathVariable Long medicalSpecialtyTypeId) {
        return ResponseEntity.ok(medicalSpecialtyService.getMedicalSpecialties(medicalSpecialtyTypeId));
    }

    @GetMapping(value = "/medical-specialties")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllMedicalSpecialties() {
        return ResponseEntity.ok(medicalSpecialtyService.getAllMedicalSpecialties());
    }

    @GetMapping(value = "/medical-specialties", params = {"name"})
    public ResponseEntity<Set<MedicalSpecialtyProjection.Full>> getAllMedicalSpecialtiesByName(@RequestParam String name) {
        return ResponseEntity.ok(medicalSpecialtyService.findAllByName(name));
    }

    @GetMapping(value = "/id-types")
    public ResponseEntity<List<IdTypeProjection>> getAllIdTypes() {
        return ResponseEntity.ok(personService.getAllIdType());
    }

    @GetMapping(value = "/marital-statuses")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getMaritalStatuses() {
        return ResponseEntity.ok(personService.getAllMaritalStatus());
    }

    @GetMapping(value = "/studies")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllStudies() {
        return ResponseEntity.ok(personService.getAllStudies());
    }

    @GetMapping(value = "/occupations")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllOccupations() {
        return ResponseEntity.ok(personService.getAllOccupations());
    }

    @GetMapping(value = "/relationship-types")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllRelationshipTypes() {
        return ResponseEntity.ok(beneficiaryFinder.findAllRelationshipTypes());
    }

    @GetMapping(value = "/payment-methods")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllPaymentMethods() {
        return ResponseEntity.ok(beneficiaryFinder.getAllPaymentMethods());
    }

}
