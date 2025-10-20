package com.capacidad.validationapi.module.settlement.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.contract.controller.ContractController;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.controller.PractitionerController;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.settlement.controller.SettlementController;
import com.capacidad.validationapi.module.settlement.projection.SettlementProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_RECEIPT;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_SETTLEMENT_ITEMS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class SettlementResource extends EntityModel<SettlementProjection> {

    private final SelfModel<PractitionerProjection.Minor, Long> practitioner;
    private final SelfModel<IdAndNameOnlyProjection, Long> contract;

    public SettlementResource(SettlementProjection settlementProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(settlementProjection);
        add(linkTo(methodOn(SettlementController.class).getReceipt(settlementProjection.getId())).withRel(RESOURCE_RECEIPT));
        add(linkTo(methodOn(SettlementController.class).getOne(settlementProjection.getId())).withSelfRel());
        add(linkTo(methodOn(SettlementController.class).getSettlementItems(settlementProjection.getId(), 1, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_SETTLEMENT_ITEMS));
        practitioner = new SelfModel<>(settlementProjection.getPractitioner(), PractitionerController.class);
        contract = new SelfModel<>(settlementProjection.getContract(), ContractController.class);
    }

    @JsonProperty("practitioner")
    public SelfModel<PractitionerProjection.Minor, Long> getPractitioner() {
        return practitioner;
    }

    @JsonProperty("contract")
    public SelfModel<IdAndNameOnlyProjection, Long> getContract() {
        return contract;
    }

}
