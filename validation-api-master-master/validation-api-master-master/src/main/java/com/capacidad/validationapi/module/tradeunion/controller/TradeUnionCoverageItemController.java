package com.capacidad.validationapi.module.tradeunion.controller;

import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.tradeunion.dto.TradeUnionCoverageItemDTO;
import com.capacidad.validationapi.module.tradeunion.projection.TradeUnionCoverageItemProjection;
import com.capacidad.validationapi.module.tradeunion.service.TradeUnionCoverageItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_TRADE_UNION_COVERAGE_ITEMS;

@RestController
@RequestMapping(value = ENDPOINT_TRADE_UNION_COVERAGE_ITEMS)
public class TradeUnionCoverageItemController extends ReducedBaseControllerImpl<TradeUnionCoverageItemDTO, Long> {

    private final TradeUnionCoverageItemService tradeUnionCoverageItemService;

    @Autowired
    public TradeUnionCoverageItemController(TradeUnionCoverageItemService tradeUnionCoverageItemService) {
        super(tradeUnionCoverageItemService);
        this.tradeUnionCoverageItemService = tradeUnionCoverageItemService;
    }

    @GetMapping(params = {"page", "size", "tradeUnionId"})
    public ResponseEntity<PageModelWrapper<EntityModel<TradeUnionCoverageItemProjection>>> getCoverageItemsByTradeUnion(@RequestParam long tradeUnionId,
                                                                                                                        @RequestParam int page,
                                                                                                                        @RequestParam int size,
                                                                                                                        @RequestParam(required = false) String search) {
        var pageable = PageRequest.of(page, size);
        PageModelWrapper<EntityModel<TradeUnionCoverageItemProjection>> currentPage = tradeUnionCoverageItemService.findAll(pageable, search, buildTradeUnionItemSearchString(tradeUnionId));
        currentPage.addQueryParam("tradeUnionId", String.valueOf(tradeUnionId));
        return ResponseEntity.ok(currentPage);
    }

    private String buildTradeUnionItemSearchString(Long tradeUnionId) {
        return String.format("tradeUnion:{id=%d}", tradeUnionId);
    }

}
