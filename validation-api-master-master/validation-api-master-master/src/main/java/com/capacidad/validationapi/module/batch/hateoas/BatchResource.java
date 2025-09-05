package com.capacidad.validationapi.module.batch.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.batch.controller.BatchController;
import com.capacidad.validationapi.module.batch.projection.BatchProjection;
import com.capacidad.validationapi.module.beneficiary.controller.BeneficiaryController;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_BATCH_ITEMS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_FILES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BatchResource extends EntityModel<BatchProjection> {

    private SelfModel<BeneficiaryProjection.Minor, Long> beneficiary;

    public BatchResource(BatchProjection.Full projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        beneficiary = new SelfModel<>(projection.getBeneficiary(), BeneficiaryController.class);
        add(linkTo(methodOn(BatchController.class).getOne(projection.getId())).withSelfRel());
        add(linkTo(methodOn(BatchController.class).retrieveFileList(projection.getId())).withRel(RESOURCE_FILES));
        add(linkTo(methodOn(BatchController.class).getAllBatchItems(projection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_BATCH_ITEMS));
    }

    public BatchResource(BatchProjection.Minor projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(BatchController.class).getOne(projection.getId())).withSelfRel());
        add(linkTo(methodOn(BatchController.class).retrieveFileList(projection.getId())).withRel(RESOURCE_FILES));
        add(linkTo(methodOn(BatchController.class).getAllBatchItems(projection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_BATCH_ITEMS));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("beneficiary")
    public SelfModel<BeneficiaryProjection.Minor, Long> getBeneficiary() {
        return this.beneficiary;
    }

}
