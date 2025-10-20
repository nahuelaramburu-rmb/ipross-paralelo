package com.capacidad.validationapi.module.batch.service;

import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.fasterxml.jackson.databind.JsonNode;

public interface BatchItemSupportService {

    JsonNode delete(BatchItem batchItem);

}
