package com.capacidad.validationapi.module.ruleprocessor.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountOnlyDTO;
import com.capacidad.validationapi.module.ruleprocessor.model.Rule;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleData;
import com.capacidad.validationapi.module.ruleprocessor.repository.RuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RuleConfigurationServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RuleRepository ruleRepository;

    @Spy
    @InjectMocks
    private RuleConfigurationServiceImpl ruleConfigurationService;

    @Test
    public void testValidateThrowsExceptionWhenAlreadyExistsGlobally() {
        Rule ruleRef = new Rule();
        ruleRef.setId(1L);
        ruleRef.setDtoClassName(MaxAmountOnlyDTO.class.getName());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(ruleRef);


        RuleConfiguration existentRuleConfiguration = new RuleConfiguration();
        ruleRef.getRuleConfigurations().add(existentRuleConfiguration);

        when(ruleRepository.findById(ruleRef.getId())).thenReturn(Optional.of(ruleRef));

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> ruleConfigurationService.validate(ruleConfiguration));

        assertThat(exception.getMessage()).isEqualTo("ruleConfiguration.alreadyExistsGlobally");
    }

    @Test
    public void testValidateThrowsExceptionWhenAlreadyExistsInContractsAndCreatedGlobally() {
        Rule ruleRef = new Rule();
        ruleRef.setId(1L);
        ruleRef.setDtoClassName(MaxAmountOnlyDTO.class.getName());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(ruleRef);

        RuleConfiguration existentRuleConfiguration = new RuleConfiguration();
        existentRuleConfiguration.setContract(new Contract());
        ruleRef.getRuleConfigurations().add(existentRuleConfiguration);

        when(ruleRepository.findById(ruleRef.getId())).thenReturn(Optional.of(ruleRef));

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> ruleConfigurationService.validate(ruleConfiguration));

        assertThat(exception.getMessage()).isEqualTo("ruleConfiguration.alreadyExistsInContract");
    }

    @Test
    public void testValidateThrowsExceptionWhenInvalidDataNotMapInstance() {
        Rule ruleRef = new Rule();
        ruleRef.setId(1L);
        ruleRef.setDtoClassName(MaxAmountOnlyDTO.class.getName());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setContract(new Contract());
        ruleConfiguration.setRuleRef(ruleRef);

        RuleConfiguration existentRuleConfiguration = new RuleConfiguration();
        existentRuleConfiguration.setContract(new Contract());
        ruleRef.getRuleConfigurations().add(existentRuleConfiguration);

        JsonNode content = objectMapper.createObjectNode();
        RuleData ruleData = new RuleData();
        ruleData.setDataIdentifier("invalidIdentifier");
        ruleData.setContent(content);

        ruleConfiguration.getData().add(ruleData);

        when(ruleRepository.findById(ruleRef.getId())).thenReturn(Optional.of(ruleRef));
        when(ruleConfigurationService.getObjectMapper()).thenReturn(objectMapper);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> ruleConfigurationService.validate(ruleConfiguration));

        assertThat(exception.getMessage()).isEqualTo("ruleConfiguration.invalidData");
    }

    @Test
    public void testValidateDoNotThrowsExceptionWhenValidDataProperties() throws ObjectNotValidException, ObjectNotFoundException {
        Rule ruleRef = new Rule();
        ruleRef.setId(1L);
        ruleRef.setDtoClassName(MaxAmountOnlyDTO.class.getName());

        Rule ruleRefExpected = new Rule();
        ruleRefExpected.setId(ruleRef.getId());
        ruleRefExpected.setDtoClassName(ruleRef.getDtoClassName());
        ruleRefExpected.setName("ruleRef");

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(ruleRef);

        IntNode content = new IntNode(1);
        RuleData ruleData = new RuleData();
        ruleData.setDataIdentifier("maxAmount");
        ruleData.setContent(content);

        ruleConfiguration.getData().add(ruleData);

        when(ruleRepository.findById(ruleRef.getId())).thenReturn(Optional.of(ruleRefExpected));
        when(ruleConfigurationService.getObjectMapper()).thenReturn(objectMapper);
        when(ruleConfigurationService.getValidator()).thenReturn(new LocalValidatorFactoryBean());

        ruleConfigurationService.validate(ruleConfiguration);

        assertThat(ruleConfiguration.getRuleRef()).isEqualTo(ruleRefExpected);
    }


}
