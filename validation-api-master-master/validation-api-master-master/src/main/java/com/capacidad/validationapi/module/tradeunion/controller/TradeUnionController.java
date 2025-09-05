package com.capacidad.validationapi.module.tradeunion.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.tradeunion.dto.TradeUnionCoverageItemDTO;
import com.capacidad.validationapi.module.tradeunion.dto.TradeUnionDTO;
import com.capacidad.validationapi.module.tradeunion.service.TradeUnionCoverageItemService;
import com.capacidad.validationapi.module.tradeunion.service.TradeUnionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_TRADE_UNIONS;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_TRADE_UNION_COVERAGE_ITEMS;
import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_TRADE_UNIONS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_TRADE_UNIONS)
public class TradeUnionController extends BaseControllerImpl<TradeUnionDTO, Long> {

    private final TradeUnionService tradeUnionService;
    private final TradeUnionCoverageItemService tradeUnionCoverageItemService;

    @Autowired
    public TradeUnionController(TradeUnionService tradeUnionService, TradeUnionCoverageItemService tradeUnionCoverageItemService) {
        super(tradeUnionService);
        this.tradeUnionService = tradeUnionService;
        this.tradeUnionCoverageItemService = tradeUnionCoverageItemService;
    }

    @GetMapping(params = {"name"})
    public ResponseEntity<CollectionModelWrapper<EntityModel<IdAndNameOnlyProjection>>> getTradeUnions(@RequestParam String name) {
        Set<IdAndNameOnlyProjection> results = tradeUnionService.getTradeUnions(name);
        ResourceAssembler<IdAndNameOnlyProjection, Long> assembler = new ResourceAssembler<>(this.getClass());
        Link self = linkTo(methodOn(this.getClass()).getTradeUnions(name)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_TRADE_UNIONS, assembler.toResources(results), self));
    }

    @PostMapping(value = "{tradeUnionId}/trade-union-coverage-items")
    public ResponseEntity<Object> addItemToTradeUnion(@PathVariable Long tradeUnionId, @Valid @RequestBody TradeUnionCoverageItemDTO dto) throws ObjectNotFoundException, ObjectNotValidException {
        var result = tradeUnionCoverageItemService.create(dto, this.tradeUnionService.validateReference(tradeUnionId));
        var location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_TRADE_UNION_COVERAGE_ITEMS, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

}
