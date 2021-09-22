package org.rcsb.mojave.tools.utils;

import org.jsonschema2pojo.exception.GenerationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AppUtils {

    private AppUtils() {}

    public static final String PROPERTIES_RESOURCE_NAME = "/tools.module.properties";

    public static Object invoke(Object obj, Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method m = clazz.getDeclaredMethod(methodName, parameterTypes);
            m.setAccessible(true);
            return m.invoke(obj, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new GenerationException(e);
        }
    }
}