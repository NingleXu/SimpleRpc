package com.gdou.common.serializer;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;


public enum SerializerType {

    jdk(0) {
        @Override
        public byte[] serializer(Object obj) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return bos.toByteArray();
        }

        @Override
        public <T> T deserializer(Class<T> clazz, byte[] bytes) throws IOException, ClassNotFoundException {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) ois.readObject();
        }
    },
    json(1) {
        @Override
        public byte[] serializer(Object obj) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
            String json = gson.toJson(obj);
            return json.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public <T> T deserializer(Class<T> clazz, byte[] bytes) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
            String json = new String(bytes, StandardCharsets.UTF_8);
            return gson.fromJson(json, clazz);
        }
    };


    public abstract byte[] serializer(Object obj) throws IOException;

    public abstract <T> T deserializer(Class<T> clazz, byte[] bytes) throws IOException, ClassNotFoundException;

    SerializerType(int code) {
        this.code = code;
    }

    final int code;

    public int getCode() {
        return code;
    }

    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override             //   String.class
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            // class -> json
            return new JsonPrimitive(src.getName());
        }
    }
}
