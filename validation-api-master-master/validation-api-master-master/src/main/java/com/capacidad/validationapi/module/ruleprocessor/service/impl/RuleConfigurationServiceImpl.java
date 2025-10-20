package com.capacidad.validationapi.module.ruleprocessor.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.ruleprocessor.dto.RuleConfigurationDTO;
import com.capacidad.validationapi.module.ruleprocessor.dto.RuleProperty;
import com.capacidad.validationapi.module.ruleprocessor.model.Rule;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleData;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleType;
import com.capacidad.validationapi.module.ruleprocessor.projection.RuleProjection;
import com.capacidad.validationapi.module.ruleprocessor.repository.RuleConfigurationRepository;
import com.capacidad.validationapi.module.ruleprocessor.repository.RuleRepository;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleConfigurationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class RuleConfigurationServiceImpl extends BaseServiceImpl<RuleConfiguration, RuleConfigurationDTO, Long> implements RuleConfigurationService {

    private final RuleConfigurationRepository ruleConfigurationRepository;
    private final RuleRepository ruleRepository;

    @Autowired
    public RuleConfigurationServiceImpl(RuleConfigurationRepository repository,
                                        RuleRepository ruleRepository) {
        super(repository);
        this.ruleConfigurationRepository = repository;
        this.ruleRepository = ruleRepository;
    }

    @Override
    public List<RuleConfiguration> getRuleConfigurationsByContract(RuleType ruleType, Contract contract) {
        return ruleConfigurationRepository.findAllByRuleRefRuleType(ruleType).stream()
                .filter(r -> r.getContract() == null || r.getContract().getId().equals(contract.getId()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void validate(RuleConfiguration ruleConfiguration) throws ObjectNotFoundException, ObjectNotValidException {
        validate(ruleConfiguration, false);
    }

    @Override
    public void validateUpdate(RuleConfiguration ruleConfiguration) throws ObjectNotFoundException, ObjectNotValidException {
        validate(ruleConfiguration, true);
    }

    private void validate(RuleConfiguration ruleConfiguration, boolean isUpdate) throws ObjectNotValidException, ObjectNotFoundException {
        Rule rule = findRuleById(ruleConfiguration.getRuleRef().getId());
        ruleConfiguration.setRuleRef(rule);
        validateExistence(ruleConfiguration, isUpdate);
        RuleProperty ruleProperty = buildPropertyFromRuleConfiguration(ruleConfiguration)
                .orElseThrow(() -> new ObjectNotValidException("ruleConfiguration.invalidData"));
        validateProperties(ruleProperty);
    }

    private void validateExistence(RuleConfiguration ruleConfiguration, boolean isUpdate) throws ObjectAlreadyExistsException {
        Optional<RuleConfiguration> optExistentRuleConfig = isUpdate ? getExistentRuleConfigurationFiltered(ruleConfiguration)
                : getExistentRuleConfiguration(ruleConfiguration);
        if (optExistentRuleConfig.isPresent()) {
            RuleConfiguration existentRuleConfig = optExistentRuleConfig.get();
            if (existentRuleConfig.getContract() == null)
                throw new ObjectAlreadyExistsException("ruleConfiguration.alreadyExistsGlobally");
            if (existentRuleConfig.getContract() != null && ruleConfiguration.getContract() == null)
                throw new ObjectAlreadyExistsException("ruleConfiguration.alreadyExistsInContract", existentRuleConfig.getContract().getName());
        }
    }

    private Optional<RuleConfiguration> getExistentRuleConfiguration(RuleConfiguration ruleConfiguration) {
        return ruleConfiguration.getRuleRef().getRuleConfigurations().stream()
                .findFirst();
    }

    private Optional<RuleConfiguration> getExistentRuleConfigurationFiltered(RuleConfiguration ruleConfiguration) {
        return ruleConfiguration.getRuleRef().getRuleConfigurations().stream()
                .filter(i -> !i.getId().equals(ruleConfiguration.getId()))
                .findFirst();
    }

    private Rule findRuleById(Long ruleId) throws ObjectNotFoundException {
        return ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ObjectNotFoundException("ruleConfiguration.ruleNotFound", String.valueOf(ruleId)));
    }

    private void validateProperties(RuleProperty ruleProperty) throws ObjectNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(ruleProperty, ruleProperty.getClass().getSimpleName());
        this.getValidator().validate(ruleProperty, bindingResult);
        if (bindingResult.hasErrors())
            throw new ObjectNotValidException("ruleConfiguration.invalidData");
    }

    @Override
    public List<RuleProjection> getRules() {
        return ruleRepository.findAllProjectedBy(RuleProjection.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RuleProperty> Optional<T> buildPropertyFromRuleConfiguration(RuleConfiguration ruleConfiguration) {
        try {
            Class<T> dtoClazz = (Class<T>) Class.forName(ruleConfiguration.getRuleRef().getDtoClassName());
            JsonNode contentNode = buildContentNode(ruleConfiguration);
            return Optional.of(this.getObjectMapper().treeToValue(contentNode, dtoClazz));
        } catch (ClassNotFoundException | ClassCastException | JsonProcessingException e) {
            log.error("({}) - Error building DTO Property Object: {}", this.getClass(), e.getMessage());
        }
        return Optional.empty();
    }

    private JsonNode buildContentNode(RuleConfiguration ruleConfiguration) throws JsonProcessingException {
        Set<RuleData> ruleDataSet = ruleConfiguration.getData();
        ObjectNode contentNode = this.getObjectMapper().createObjectNode();
        if (ruleDataSet.iterator().next() instanceof Map)
            ruleDataSet = convertMapToRuleDataSet(ruleConfiguration);
        ruleDataSet.forEach(r -> contentNode.set(r.getDataIdentifier(), r.getContent()));
        return contentNode;
    }

    /**
     * Workaround method for PATCH flow.
     * This flow interprets RuleData as a LinkedHashMap and needs to be converted
     */
    private Set<RuleData> convertMapToRuleDataSet(RuleConfiguration ruleConfiguration) throws JsonProcessingException {
        Set<RuleData> convertedRuleDataSet = new HashSet<>();
        for (Object object : ruleConfiguration.getData()) {
            Map innerMap = (Map) object;
            JsonNode convertedNode = this.getObjectMapper().valueToTree(innerMap);
            RuleData ruleData = this.getObjectMapper().treeToValue(convertedNode, RuleData.class);
            convertedRuleDataSet.add(ruleData);
        }
        ruleConfiguration.setData(convertedRuleDataSet);
        return convertedRuleDataSet;
    }

}
