package com.capacidad.validationapi.module.audittray.config;

import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.audittray.projection.AuditTrayProjection;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import static com.capacidad.validationapi.misc.constant.ModelConstants.*;

public class AuditHistoryMinorSerializer extends StdSerializer<AuditHistoryProjection.Minor> {

    private static final String TRAY = "auditTray";
    private static final String FROM_TRAY = "fromAuditTray";
    private static final String FROM_USER = "fromAuditor";
    private static final String REASON = "reason";

    public AuditHistoryMinorSerializer() {
        this(null);
    }

    public AuditHistoryMinorSerializer(Class<AuditHistoryProjection.Minor> auditHistoryClass) {
        super(auditHistoryClass);
    }

    @Override
    public void serialize(AuditHistoryProjection.Minor projection, JsonGenerator gen, SerializerProvider provider) throws IOException {
        ObjectNode payload = ((ObjectMapper) gen.getCodec()).createObjectNode();
        ObjectNode data = ((ObjectMapper) gen.getCodec()).valueToTree(projection.getMedicalAuthorization());
        payload.set(TRAY, buildAuditTrayObject(gen, projection.getAuditTray()));
        payload.put(ID, projection.getId());
        payload.set(DATA, data);
        appendExtraFields(gen, projection, payload);
        ObjectNode output = ((ObjectMapper) gen.getCodec()).createObjectNode();
        output.put(TYPE, projection.getEvent().toString());
        output.set(PAYLOAD, payload);
        gen.writeTree(output);
    }

    private ObjectNode buildAuditTrayObject(JsonGenerator gen, AuditTrayProjection.Minor auditTrayProjection) {
        ObjectNode auditTrayNode = ((ObjectMapper) gen.getCodec()).createObjectNode();
        auditTrayNode.put(RESOURCE_ID, auditTrayProjection.getResourceId().toString());
        auditTrayNode.put(ID, auditTrayProjection.getId());
        auditTrayNode.put(NAME, auditTrayProjection.getName());
        return auditTrayNode;
    }

    private void appendExtraFields(JsonGenerator gen, AuditHistoryProjection.Minor projection, ObjectNode output) {
        output.set(FROM_TRAY, projection.getFromAuditTray() == null ? null : buildAuditTrayObject(gen, projection.getFromAuditTray()));
        output.put(FROM_USER, projection.getFromAuditor() == null ? null : projection.getFromAuditor().getUsername());
        output.put(REASON, projection.getReason());
    }
}
