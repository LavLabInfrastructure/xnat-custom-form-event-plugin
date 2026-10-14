package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xft.ItemI;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

final class CustomFormPayloadModule extends SimpleModule {
    private static final ThreadLocal<PayloadContext> PAYLOAD_CONTEXT = new ThreadLocal<>();

    CustomFormPayloadModule() {
        super("XNAT Custom Form Event Payloads");
        setSerializerModifier(new BeanSerializerModifier() {
            @Override
            @SuppressWarnings("unchecked")
            public JsonSerializer<?> modifySerializer(final SerializationConfig config,
                                                      final BeanDescription beanDescription,
                                                      final JsonSerializer<?> serializer) {
                if (XnatModelObject.class.isAssignableFrom(beanDescription.getBeanClass())
                        || ItemI.class.isAssignableFrom(beanDescription.getBeanClass())) {
                    return new CustomFormPayloadSerializer((JsonSerializer<Object>) serializer);
                }
                return serializer;
            }
        });
    }

    static void prepare(final String xsiType, final JsonNode customForms) {
        if (xsiType != null && customForms != null && customForms.isArray() && !customForms.isEmpty()) {
            PAYLOAD_CONTEXT.set(new PayloadContext(xsiType, customForms));
        }
    }

    private record PayloadContext(String xsiType, JsonNode customForms) {
    }

    private static final class CustomFormPayloadSerializer extends JsonSerializer<Object> {
        private final JsonSerializer<Object> delegate;

        private CustomFormPayloadSerializer(final JsonSerializer<Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void serialize(final Object value, final JsonGenerator generator,
                              final SerializerProvider provider) throws IOException {
            final PayloadContext context = takeMatchingContext(value);
            if (context == null) {
                delegate.serialize(value, generator, provider);
                return;
            }
            writeWithCustomForms(value, generator, provider, null, context);
        }

        @Override
        public void serializeWithType(final Object value, final JsonGenerator generator,
                                      final SerializerProvider provider,
                                      final TypeSerializer typeSerializer) throws IOException {
            final PayloadContext context = takeMatchingContext(value);
            if (context == null) {
                delegate.serializeWithType(value, generator, provider, typeSerializer);
                return;
            }
            writeWithCustomForms(value, generator, provider, typeSerializer, context);
        }

        private PayloadContext takeMatchingContext(final Object value) {
            final PayloadContext context = PAYLOAD_CONTEXT.get();
            if (context == null) {
                return null;
            }
            final String xsiType;
            if (value instanceof XnatModelObject modelObject) {
                xsiType = modelObject.getXsiType();
            } else if (value instanceof ItemI item) {
                xsiType = item.getXSIType();
            } else {
                xsiType = null;
            }
            PAYLOAD_CONTEXT.remove();
            if (context.xsiType().equals(xsiType)) {
                return context;
            }
            return null;
        }

        private void writeWithCustomForms(final Object value, final JsonGenerator generator,
                                          final SerializerProvider provider,
                                          final TypeSerializer typeSerializer,
                                          final PayloadContext context) throws IOException {
            final TokenBuffer buffer = new TokenBuffer(generator.getCodec(), false);
            delegate.serialize(value, buffer, provider);
            final JsonNode serialized = generator.getCodec().readTree(buffer.asParser());
            if (serialized == null || !serialized.isObject()) {
                throw new IOException("Serialized XNAT model payload is not a JSON object");
            }

            final WritableTypeId typeId;
            if (typeSerializer == null) {
                generator.writeStartObject();
                typeId = null;
            } else {
                typeId = typeSerializer.writeTypePrefix(
                        generator, typeSerializer.typeId(value, JsonToken.START_OBJECT));
            }
            final Iterator<Map.Entry<String, JsonNode>> fields = serialized.fields();
            while (fields.hasNext()) {
                final Map.Entry<String, JsonNode> field = fields.next();
                generator.writeFieldName(field.getKey());
                generator.writeTree(field.getValue());
            }
            generator.writeFieldName("customForms");
            generator.writeTree(context.customForms());
            if (typeSerializer == null) {
                generator.writeEndObject();
            } else {
                typeSerializer.writeTypeSuffix(generator, typeId);
            }
        }
    }
}
