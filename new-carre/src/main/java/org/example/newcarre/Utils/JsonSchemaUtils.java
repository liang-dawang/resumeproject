package org.example.newcarre.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

public class JsonSchemaUtils {
    /**
     * 将 Java Class 转换为 JSON Schema 字符串
     */
    public static String generate(Class<?> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            mapper.acceptJsonFormatVisitor(mapper.constructType(clazz), visitor);
            JsonSchema jsonSchema = visitor.finalSchema();
            return mapper.writeValueAsString(jsonSchema);
        } catch (Exception e) {
            throw new RuntimeException("生成 JSON Schema 失败", e);
        }
    }
}