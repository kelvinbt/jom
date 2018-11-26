/*
 *
 *  * Copyright (C) 2018 The JOM Project by Khanh Trinh <trinhkhanh@live.com>
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.kelvin.jom.utils;

import com.kelvin.jom.defines.annotations.Column;
import com.kelvin.jom.defines.classes.LimitedMap;
import com.kelvin.jom.exceptions.InspectEntityFailedException;

import java.lang.reflect.Field;
import java.util.Map;

public final class ClassInspectUtil {
    private static volatile Map<String, Field[]> cacheClassesFields = new LimitedMap<>();

    public static void put(final Class<?> clazz, final Field[] fields) {
        if (!cacheClassesFields.containsKey(clazz.getSimpleName()))
            cacheClassesFields.put(clazz.getSimpleName(), fields);
    }

    public static Field[] get(final Class<?> clazz) {
        return cacheClassesFields.get(clazz.getSimpleName());
    }


    public static class InspectField {
        public static Object getFieldValue(final Object target, final Field field, Column type) throws InspectEntityFailedException {
            if (null != type) {
                try {
                    field.setAccessible(true);
                    return field.get(target);
                } catch (IllegalAccessException e) {
                    throw new InspectEntityFailedException("Can not get column value", e);
                } catch (NullPointerException e) { /*bypass null field*/ }
            }
            return null;
        }

        public static void setFieldValue(final Object target, final Field field, Object value, Column type) throws InspectEntityFailedException {
            if (null != type) {
                try {
                    field.setAccessible(true);
                    field.set(target, value);
                } catch (IllegalAccessException e) {
                    throw new InspectEntityFailedException("Can not set column value", e);
                } catch (NullPointerException e) { /*bypass null field*/ }
            }
        }
    }
}
