package org.obiba.opal.swagger;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.converter.ModelConverterContextImpl;
import io.swagger.v3.oas.models.media.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.swagger.ProtobufModelResolver;

import java.util.*;

public class ProtobufModelResolverTest {

    private final Iterator<ModelConverter> emptyIterator = Collections.emptyIterator();
    private ProtobufModelResolver protobufModelResolver;
    private ModelConverterContext context;

    @Before
    public void setUp() {
        protobufModelResolver = new ProtobufModelResolver();
        context = new ModelConverterContextImpl(protobufModelResolver);
    }

    @Test
    public void parseFileDtoSchemaTest() {
        String expectedSchemaName = "Opal_FileDto";
        String expectedRef = "#/components/schemas/" + expectedSchemaName;
        List<String> expectedTypeEnum = List.of("FILE", "FOLDER");

        Schema<?> refSchema = protobufModelResolver.resolve(new AnnotatedType(Opal.FileDto.class), context, emptyIterator);
        Assert.assertEquals(expectedRef, refSchema.get$ref());
        Schema<?> dtoSchema = context.getDefinedModels().get(expectedSchemaName);
        Assert.assertNotNull(dtoSchema);
        Map<String, Schema> properties = dtoSchema.getProperties();
        Assert.assertEquals(8, properties.size());
        Assert.assertTrue(properties.get("name") instanceof StringSchema);
        Assert.assertTrue(properties.get("path") instanceof StringSchema);
        Schema<?> typeSchema = properties.get("type");
        Assert.assertNotNull(typeSchema);
        List<?> enumValues = typeSchema.getEnum();
        Assert.assertEquals(expectedTypeEnum, enumValues);
        Schema<?> childrenSchema = properties.get("children");
        Assert.assertTrue(childrenSchema instanceof ArraySchema);
        Assert.assertEquals(refSchema, childrenSchema.getItems());
        assertInt64(properties.get("size"));
        assertInt64(properties.get("lastModifiedTime"));
        Assert.assertTrue(properties.get("readable") instanceof BooleanSchema);
        Assert.assertTrue(properties.get("writable") instanceof BooleanSchema);
    }

    private void assertInt64(Schema<?> schema) {
        Assert.assertTrue(schema instanceof IntegerSchema);
        Assert.assertEquals("int64", schema.getFormat());
    }
}
