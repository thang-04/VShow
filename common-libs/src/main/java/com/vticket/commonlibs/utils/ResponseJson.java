package com.vticket.commonlibs.utils;

import com.google.gson.*;
import com.vticket.commonlibs.exception.ErrorCode;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ResponseJson {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString()); // ISO-8601
                }
            })
            .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                @Override
                public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString());
                }
            })
            .registerTypeAdapter(Instant.class, new JsonSerializer<Instant>() {
                @Override
                public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString());
                }
            })
            .create();

    public static String of(ErrorCode code) {
        return of(code, code.getMessage(), (JsonObject) null);
    }

    public static String of(ErrorCode code, String desc) {
        return of(code, desc, (JsonObject) null);
    }

    public static String of(ErrorCode code, Object data) {
        JsonElement jsonElement = gson.toJsonTree(data);
        JsonObject jsonObject = jsonElement != null && jsonElement.isJsonObject()
                ? jsonElement.getAsJsonObject()
                : null;
        return of(code, code.getMessage(), jsonObject);
    }

    public static String of(ErrorCode code, String desc, Object data) {
        JsonElement jsonElement = gson.toJsonTree(data);
        return of(code, desc, jsonElement);
    }

    public static String of(ErrorCode code, String desc, JsonElement data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", code.getCode());
        obj.addProperty("codeName", code.name());
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? code.getMessage() : desc);

        if (data != null) {
            obj.add("result", data);
        }
        return gson.toJson(obj);
    }

    public static String ofArray(ErrorCode code, String desc, JsonArray data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", code.getCode());
        obj.addProperty("codeName", code.name());
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? code.getMessage() : desc);

        if (data != null) {
            obj.add("result", data);
        }
        return gson.toJson(obj);
    }

    public static String success(String desc, Object data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", 1000);
        obj.addProperty("codeName", "SUCCESS");
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? "Success" : desc);

        if (data != null) {
            JsonElement jsonElement = gson.toJsonTree(data);
            obj.add("result", jsonElement);
        }
        return gson.toJson(obj);
    }

    public static String success(String desc) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", 1000);
        obj.addProperty("codeName", "SUCCESS");
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? "Success" : desc);
        return gson.toJson(obj);
    }

    public static String success(String desc, List<?> data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", 1000);
        obj.addProperty("codeName", "SUCCESS");
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? "Success" : desc);

        if (data != null) {
            JsonElement jsonElement = gson.toJsonTree(data);
            if (jsonElement.isJsonArray()) {
                obj.add("result", jsonElement.getAsJsonArray());
            } else {
                obj.add("result", jsonElement);
            }
        }
        return gson.toJson(obj);
    }
}
