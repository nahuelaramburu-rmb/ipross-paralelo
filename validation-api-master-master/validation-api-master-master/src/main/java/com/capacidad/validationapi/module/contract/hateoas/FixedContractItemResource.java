package com.capacidad.validationapi.module.contract.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.contract.controller.ContractItemSpecialPriceController;
import com.capacidad.validationapi.module.contract.controller.FixedContractItemController;
import com.capacidad.validationapi.module.contract.projection.ContractItemSpecialPriceProjection;
import com.capacidad.validationapi.module.contract.projection.FixedContractItemProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_LOGS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_CONTRACT_ITEM_SPECIAL_PRICES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class FixedContractItemResource extends EntityModel<FixedContractItemProjection> {

    private final Map<String, Object> embeddedResources = new HashMap<>();

    public FixedContractItemResource(FixedContractItemProjection fixedContractItemProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(fixedContractItemProjection);
        add(linkTo(methodOn(FixedContractItemController.class).getOne(fixedContractItemProjection.getId())).withSelfRel());
        add(linkTo(methodOn(FixedContractItemController.class).getAuditLogs(fixedContractItemProjection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        List<SelfModel<ContractItemSpecialPriceProjection, Long>> embeddedContractItemSpecialPrices = new ArrayList<>();
        for (ContractItemSpecialPriceProjection c : fixedContractItemProjection.getSpecialPrices())
            embeddedContractItemSpecialPrices.add(new SelfModel<>(c, ContractItemSpecialPriceController.class));
        embeddedResources.put(RESOURCE_CONTRACT_ITEM_SPECIAL_PRICES, embeddedContractItemSpecialPrices);
    }


    @JsonUnwrapped
    @JsonProperty("_embedded")
    public Map<String, Object> getEmbeddedResources() {
        return embeddedResources;
    }

}
