package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CustomFormEventPublisherTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void reportsChangedFormValues() throws Exception {
        final JsonNode oldFields = mapper.readTree("{\"12345678-1234-1234-1234-123456789abc\":{\"field\":\"old\"}}");
        final JsonNode newFields = mapper.readTree("{\"12345678-1234-1234-1234-123456789abc\":{\"field\":\"new\"}}");

        final Map<String, JsonNode> changes = CustomFormEventPublisher.findChangedForms(oldFields, newFields);

        assertThat(changes).containsEntry("12345678-1234-1234-1234-123456789abc",
                mapper.readTree("{\"field\":\"new\"}"));
    }

    @Test
    void ignoresUnchangedForms() throws Exception {
        final JsonNode fields = mapper.readTree("{\"12345678-1234-1234-1234-123456789abc\":{\"field\":\"same\"}}");

        assertThat(CustomFormEventPublisher.findChangedForms(fields, fields)).isEmpty();
    }

    @Test
    void ignoresNonFormCustomValues() throws Exception {
        final JsonNode oldFields = mapper.readTree("{\"other-key\":{\"field\":\"old\"}}");
        final JsonNode newFields = mapper.readTree("{\"other-key\":{\"field\":\"new\"}}");

        assertThat(CustomFormEventPublisher.findChangedForms(oldFields, newFields)).isEmpty();
    }

    @Test
    void reportsAddedAndRemovedForms() throws Exception {
        final JsonNode fields = mapper.readTree("{\"12345678-1234-1234-1234-123456789abc\":{\"field\":\"value\"}}");
        final Map<String, JsonNode> added = CustomFormEventPublisher.findChangedForms(null, fields);
        final Map<String, JsonNode> removed = CustomFormEventPublisher.findChangedForms(fields, null);

        assertThat(added).containsEntry("12345678-1234-1234-1234-123456789abc",
                mapper.readTree("{\"field\":\"value\"}"));
        assertThat(removed).containsKey("12345678-1234-1234-1234-123456789abc");
        assertThat(removed.get("12345678-1234-1234-1234-123456789abc").isNull()).isTrue();
    }
}
