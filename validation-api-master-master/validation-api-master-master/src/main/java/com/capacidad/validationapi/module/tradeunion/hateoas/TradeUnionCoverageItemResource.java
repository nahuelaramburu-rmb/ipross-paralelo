package com.capacidad.validationapi.module.tradeunion.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.tradeunion.controller.TradeUnionCoverageItemController;
import com.capacidad.validationapi.module.tradeunion.projection.TradeUnionCoverageItemProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_LOGS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class TradeUnionCoverageItemResource extends EntityModel<TradeUnionCoverageItemProjection> {

    public TradeUnionCoverageItemResource(TradeUnionCoverageItemProjection content) throws ObjectNotValidException, ObjectNotFoundException {
        super(content);
        add(linkTo(methodOn(TradeUnionCoverageItemController.class).getOne(content.getId())).withSelfRel());
        add(linkTo(methodOn(TradeUnionCoverageItemController.class).getAuditLogs(content.getId())).withRel(RESOURCE_AUDIT_LOGS));
    }
}
