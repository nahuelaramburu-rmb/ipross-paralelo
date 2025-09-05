package com.capacidad.validationapi.module.ruleprocessor.controller;

import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.ruleprocessor.dto.RuleConfigurationDTO;
import com.capacidad.validationapi.module.ruleprocessor.projection.RuleProjection;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_RULE_CONFIGURATIONS;

@RestController
@RequestMapping(value = ENDPOINT_RULE_CONFIGURATIONS)
public class RuleConfigurationController extends BaseControllerImpl<RuleConfigurationDTO, Long> {

    private final RuleConfigurationService ruleConfigurationService;

    @Autowired
    public RuleConfigurationController(RuleConfigurationService abstractService) {
        super(abstractService);
        this.ruleConfigurationService = abstractService;
    }

    @GetMapping(value = "/rules")
    public ResponseEntity<List<RuleProjection>> getRules() {
        return ResponseEntity.ok(ruleConfigurationService.getRules());
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        String innerSearch = buildRuleConfigurationSearchString(null, search);
        return ResponseEntity.ok(ruleConfigurationService.findAll(pageable, search, innerSearch));
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @GetMapping(params = {"page", "size", "contractId"})
    public ResponseEntity<Object> getAllByContract(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam Long contractId,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        String innerSearch = buildRuleConfigurationSearchString(contractId, search);
        PageModelWrapper<EntityModel<RuleProjection>> result = ruleConfigurationService.findAll(pageable, search, innerSearch);
        result.addQueryParam("contractId", contractId.toString());
        return ResponseEntity.ok(result);
    }

    private String buildRuleConfigurationSearchString(Long contractId, String search) {
        if (contractId == null)
            return Utils.buildCompoundSearchString("contract:null", search);
        return Utils.buildCompoundSearchString(String.format("contract:{id=%d}", contractId), search);
    }

}
