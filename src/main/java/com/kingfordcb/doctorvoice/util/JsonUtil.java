package com.kingfordcb.doctorvoice.util;

import com.google.gson.Gson;

/**
 * @Author: lpf
 * @Date: 2023/06/07
 */
public class JsonUtil {

    public final static Gson gson = new Gson();

    public static String objectToJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T jsonToObject(String json, Class<T> object) {
        return gson.fromJson(json, object);
    }

}
