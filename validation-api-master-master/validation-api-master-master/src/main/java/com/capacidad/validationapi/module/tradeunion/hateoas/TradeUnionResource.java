package com.capacidad.validationapi.module.tradeunion.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.tradeunion.controller.TradeUnionController;
import com.capacidad.validationapi.module.tradeunion.controller.TradeUnionCoverageItemController;
import com.capacidad.validationapi.module.tradeunion.projection.TradeUnionProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_LOGS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_TRADE_UNION_COVERAGE_ITEMS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


public class TradeUnionResource extends EntityModel<TradeUnionProjection> {

    public TradeUnionResource(TradeUnionProjection content) throws ObjectNotFoundException, ObjectNotValidException {
        super(content);
        add(linkTo(methodOn(TradeUnionController.class).getOne(content.getId())).withSelfRel());
        add(linkTo(methodOn(TradeUnionCoverageItemController.class).getCoverageItemsByTradeUnion(content.getId(), 1, DEFAULT_PAGE_SIZE, null)).withRel(RESOURCE_TRADE_UNION_COVERAGE_ITEMS).expand());
        add(linkTo(methodOn(TradeUnionController.class).getAuditLogs(content.getId())).withRel(RESOURCE_AUDIT_LOGS));
    }
}
