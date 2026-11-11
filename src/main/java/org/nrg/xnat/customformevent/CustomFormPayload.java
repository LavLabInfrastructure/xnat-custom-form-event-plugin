package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class CustomFormPayload {
    private CustomFormPayload() {
    }

    static JsonNode withMetadata(final JsonNode basePayload, final JsonNode customForms,
                                 final JsonNode changedFields) {
        if (basePayload == null || !basePayload.isObject()) {
            throw new IllegalArgumentException("Serialized XNAT event payload must be a JSON object");
        }
        final ObjectNode payload = (ObjectNode) basePayload.deepCopy();
        payload.set("customForms", customForms.deepCopy());
        payload.set("changedFields", changedFields.deepCopy());
        return payload;
    }
}
