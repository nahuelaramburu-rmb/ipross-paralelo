package com.capacidad.validationapi.module.contract.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.contract.controller.MaximumAdjustmentController;
import com.capacidad.validationapi.module.contract.controller.MonetaryAdjustmentController;
import com.capacidad.validationapi.module.contract.controller.UsageRateAdjustmentController;
import com.capacidad.validationapi.module.contract.projection.ContractAdjustmentProjection;
import com.capacidad.validationapi.module.contract.projection.MaximumAdjustmentProjection;
import com.capacidad.validationapi.module.contract.projection.MonetaryAdjustmentProjection;
import com.capacidad.validationapi.module.contract.projection.UsageRateAdjustmentProjection;
import org.springframework.hateoas.EntityModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ContractAdjustmentResource extends EntityModel<ContractAdjustmentProjection> {

    public ContractAdjustmentResource(MaximumAdjustmentProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(MaximumAdjustmentController.class).getOne(projection.getId())).withSelfRel());
    }

    public ContractAdjustmentResource(UsageRateAdjustmentProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(UsageRateAdjustmentController.class).getOne(projection.getId())).withSelfRel());
    }

    public ContractAdjustmentResource(MonetaryAdjustmentProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(MonetaryAdjustmentController.class).getOne(projection.getId())).withSelfRel());
    }

}
