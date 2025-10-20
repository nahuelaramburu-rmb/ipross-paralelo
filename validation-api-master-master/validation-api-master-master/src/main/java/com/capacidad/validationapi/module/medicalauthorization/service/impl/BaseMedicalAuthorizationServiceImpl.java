package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.repository.BaseMedicalAuthorizationRepository;
import com.capacidad.validationapi.module.medicalauthorization.service.*;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
public abstract class BaseMedicalAuthorizationServiceImpl<T extends MedicalAuthorization, D extends MedicalAuthorizationDTO> extends BaseServiceImpl<T, D, Long> implements BaseMedicalAuthorizationService<T, D> {

    private final BaseMedicalAuthorizationRepository<T, Long> repository;
    @Autowired
    public MedicalAuthorizationValidator medicalAuthorizationValidator;
    @Autowired
    public PractitionerService practitionerService;
    @Autowired
    private MedicalCenterService medicalCenterService;
    @Autowired
    private MedicalAuthorizationPreProcessor medicalAuthorizationPreProcessor;
    @Autowired
    private MedicalAuthorizationApprover medicalAuthorizationApprover;
    @Autowired
    private MedicalAuthorizationPostProcessor medicalAuthorizationPostProcessor;

    public BaseMedicalAuthorizationServiceImpl(BaseMedicalAuthorizationRepository<T, Long> repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public T initialize(D medicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException {
        T medicalAuthorization = this.mapDtoToInput(medicalAuthorizationDTO);
        initializePractitionerOrMedicalCenter(medicalAuthorization);
        initializeCity(medicalAuthorization);
        return medicalAuthorization;
    }

    private void initializePractitionerOrMedicalCenter(T medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        if (SecurityUtils.isMedicalCenter()) {
            medicalAuthorization.setMedicalCenter(this.getMedicalCenterService().getAuthMedicalCenter());
            medicalAuthorization.setPractitioner(this.getPractitionerService().findByIdLocked(medicalAuthorization.getPractitioner().getId()));
        }
        if (SecurityUtils.isPractitioner()) {
            medicalAuthorization.setPractitioner(this.getPractitionerService().getAuthPractitionerLocked());
            if (medicalAuthorization.getMedicalCenter() == null)
                throw new ObjectNotValidException("medicalAuthorization.missingMedicalCenter");
            medicalAuthorization.setMedicalCenter(this.getMedicalCenterService().findById(medicalAuthorization.getMedicalCenter().getId()));
        }
    }

    private void initializeCity(T medicalAuthorization) {
        MedicalCenter medicalCenter = medicalAuthorization.getMedicalCenter();
        City city = medicalCenter.getAddress().getCity();
        medicalAuthorization.setCity(city);
    }

    @Override
    public void preProcess(T medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        this.getMedicalAuthorizationPreProcessor().preProcess(medicalAuthorization);
    }

    @Override
    public T persist(T medicalAuthorization) {
        return repository.saveAndFlush(medicalAuthorization);
    }

    @Override
    public void approve(T medicalAuthorization) {
        this.getMedicalAuthorizationApprover().approve(medicalAuthorization);
    }

    @Override
    public MedicalAuthorizationProjection postProcess(T medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        this.getMedicalAuthorizationPostProcessor().postProcess(medicalAuthorization);
        repository.saveAndFlush(medicalAuthorization);
        repository.refresh(medicalAuthorization);
        this.getMedicalAuthorizationPostProcessor().notify(medicalAuthorization);
        return this.getProjectionFactory().createProjection(MedicalAuthorizationProjection.class, medicalAuthorization);
    }

    @Override
    public void validate(T medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        validatePractitioner(medicalAuthorization);
        this.getMedicalAuthorizationValidator().validateBeneficiaryStatus(medicalAuthorization.getBeneficiary());
    }

    private void validatePractitioner(T medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        this.getMedicalAuthorizationValidator().validatePractitionerStatus(medicalAuthorization.getPractitioner());
        this.getMedicalAuthorizationValidator().validatePractitionerMedicalCenter(medicalAuthorization.getPractitioner(), medicalAuthorization.getMedicalCenter());
        for (MedicalAuthorizationItem medicalAuthorizationItem : medicalAuthorization.getMedicalAuthorizationItems()) {
            Nomenclator nomenclator = this.getMedicalAuthorizationValidator().getValidatedNomenclator(medicalAuthorization.getPractitioner(), medicalAuthorizationItem.getNomenclator().getId());
            medicalAuthorizationItem.setNomenclator(nomenclator);
            this.getMedicalAuthorizationValidator().validateMaxQuantity(medicalAuthorizationItem);
        }
        this.getMedicalAuthorizationValidator().validateAndSetPetitioner(medicalAuthorization);
    }

    protected PractitionerService getPractitionerService() {
        return practitionerService;
    }

    protected MedicalCenterService getMedicalCenterService() {
        return medicalCenterService;
    }

    protected MedicalAuthorizationValidator getMedicalAuthorizationValidator() {
        return medicalAuthorizationValidator;
    }

    protected MedicalAuthorizationPreProcessor getMedicalAuthorizationPreProcessor() {
        return medicalAuthorizationPreProcessor;
    }

    protected MedicalAuthorizationApprover getMedicalAuthorizationApprover() {
        return medicalAuthorizationApprover;
    }

    protected MedicalAuthorizationPostProcessor getMedicalAuthorizationPostProcessor() {
        return medicalAuthorizationPostProcessor;
    }

}
