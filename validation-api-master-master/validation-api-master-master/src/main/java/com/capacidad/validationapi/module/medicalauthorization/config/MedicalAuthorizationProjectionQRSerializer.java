package com.capacidad.validationapi.module.medicalauthorization.config;

import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Collections;

public class MedicalAuthorizationProjectionQRSerializer extends StdSerializer<MedicalAuthorizationProjection.QR> {

    private static final String BENEFICIARY = "beneficiary";
    private static final String PETITIONER = "petitioner";
    private static final String MEDICAL_AUTHORIZATION_ITEMS = "medicalAuthorizationItems";
    private static final String CODE = "code";

    public MedicalAuthorizationProjectionQRSerializer() {
        this(null);
    }

    protected MedicalAuthorizationProjectionQRSerializer(Class<MedicalAuthorizationProjection.QR> t) {
        super(t);
    }

    @Override
    public void serialize(MedicalAuthorizationProjection.QR value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        ObjectMapper mapper = (ObjectMapper) gen.getCodec();
        ObjectNode content = mapper.createObjectNode();
        content.set(BENEFICIARY, mapper.readTree(mapper.writeValueAsString(value.getBeneficiary())));
        content.put(CODE, value.getCode());
        if (value.getPetitioner() != null)
            content.set(PETITIONER, mapper.readTree(mapper.writeValueAsString(value.getPetitioner())));
        else
            content.set(PETITIONER, null);
        if (value.getMedicalAuthorizationItems() != null && !value.getMedicalAuthorizationItems().isEmpty())
            content.set(MEDICAL_AUTHORIZATION_ITEMS, mapper.readTree(mapper.writeValueAsString(value.getMedicalAuthorizationItems())));
        else
            content.set(MEDICAL_AUTHORIZATION_ITEMS, mapper.readTree(mapper.writeValueAsString(Collections.emptySet())));
        gen.writeTree(content);
    }
}
