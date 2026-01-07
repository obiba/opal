package org.obiba.opal.web.swagger;

import com.fasterxml.jackson.databind.JavaType;
import com.google.protobuf.*;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ProtobufModelResolver extends ModelResolver {

    public ProtobufModelResolver() {
        super(Json.mapper());
    }

    @Override
    public Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        JavaType type = this._mapper.constructType(annotatedType.getType());
        if (type == null) {
            return null;
        }
        Class<?> cls = type.getRawClass();
        if (Message.class.isAssignableFrom(cls)) {
            return createSchema(cls, modelConverterContext);
        }
        return super.resolve(annotatedType, modelConverterContext, iterator);
    }

    private Schema<?> createSchema(Class<?> cls, ModelConverterContext modelConverterContext) {
        try {
            Method newBuilder = cls.getMethod("getDescriptor");
            Descriptors.Descriptor messageDescriptor = (Descriptors.Descriptor) newBuilder.invoke(null);
            return createSchema(messageDescriptor, modelConverterContext);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to get descriptor for Protobuf message class: " + cls.getName(), e);
        }
    }

    private Schema<?> createSchema(Descriptors.Descriptor messageDescriptor, ModelConverterContext modelConverterContext) {
        String refId = messageDescriptor.getFullName().replace('.', '_');
        Schema<?> refSchema = new Schema<>();
        refSchema.set$ref(refId);
        if (modelConverterContext.getDefinedModels().containsKey(refId)) {
            return refSchema;
        }
        final ObjectSchema schema = new ObjectSchema();
        modelConverterContext.defineModel(refId, schema);
        for (Descriptors.FieldDescriptor descriptor : messageDescriptor.getFields()) {
            Schema<?> propertySchema = createPropertySchema(descriptor, modelConverterContext);
            if (descriptor.isRepeated()) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.items(propertySchema);
                propertySchema = arraySchema;
            }
            schema.addProperty(descriptor.getJsonName(), propertySchema);
            if (descriptor.isRequired()) {
                schema.addRequiredItem(descriptor.getJsonName());
            }
        }
        return refSchema;
    }

    private Schema<?> createPropertySchema(Descriptors.FieldDescriptor descriptor, ModelConverterContext modelConverterContext) {
        switch (descriptor.getJavaType()) {
            case INT -> {
                return new IntegerSchema();
            }
            case LONG -> {
                IntegerSchema schema = new IntegerSchema();
                schema.setFormat("int64");
                return schema;
            }
            case FLOAT -> {
                NumberSchema schema = new NumberSchema();
                schema.setFormat("float");
                return schema;
            }
            case DOUBLE -> {
                NumberSchema schema = new NumberSchema();
                schema.setFormat("double");
                return schema;
            }
            case BOOLEAN -> {
                return new BooleanSchema();
            }
            case STRING -> {
                return new StringSchema();
            }
            case BYTE_STRING -> {
                return new ByteArraySchema();
            }
            case ENUM -> {
                return createEnumSchema(descriptor.getEnumType());
            }
            case MESSAGE -> {
                return createSchema(descriptor.getMessageType(), modelConverterContext);
            }
        }
        throw new IllegalStateException();
    }

    private StringSchema createEnumSchema(Descriptors.EnumDescriptor enumDescriptor) {
        StringSchema schema = new StringSchema();
        for (Descriptors.EnumValueDescriptor value : enumDescriptor.getValues()) {
            schema.addEnumItem(value.getName());
        }
        return schema;
    }
}
