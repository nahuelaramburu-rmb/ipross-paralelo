package com.capacidad.validationapi.module.nomenclator.service;

import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.fasterxml.jackson.databind.JsonNode;

public interface NomenclatorSupportService {

    JsonNode delete(Nomenclator nomenclator);

}
