package com.capacidad.validationapi.module.batch.service.impl;

import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.repository.BatchItemRepository;
import com.capacidad.validationapi.module.batch.service.BatchItemSupportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class BatchItemSupportServiceImpl implements BatchItemSupportService {

    private final BatchItemRepository batchItemRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    @Autowired
    public BatchItemSupportServiceImpl(BatchItemRepository batchItemRepository,
                                       ApplicationEventPublisher applicationEventPublisher,
                                       ObjectMapper objectMapper) {
        this.batchItemRepository = batchItemRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(BatchItem batchItem) {
        batchItem.setDeleted(true);
        batchItem.setDeletionToken(UUID.randomUUID());
        batchItemRepository.save(batchItem);
        applicationEventPublisher.publishEvent(new AfterSoftDeleteEvent<>(batchItem, batchItemRepository));
        ObjectNode response = objectMapper.createObjectNode();
        return response.put("id", batchItem.getId().toString());
    }
}
