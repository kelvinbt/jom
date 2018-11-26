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

package com.kelvin.jom.persistence.transform.impl;

import com.kelvin.jom.defines.annotations.Column;
import com.kelvin.jom.exceptions.InspectEntityFailedException;
import com.kelvin.jom.exceptions.ResultSetBindingException;
import com.kelvin.jom.persistence.transform.EntityTransform;
import com.kelvin.jom.utils.ClassInspectUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public final class EntityTransformImpl<RS, E> implements EntityTransform<RS> {
    private final E target;

    public EntityTransformImpl(E target) {
        this.target = target;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R bind(final RS rs) throws ResultSetBindingException, InspectEntityFailedException {
        if (rs instanceof Integer) {
            int i = (Integer) rs;
            if (i > 0)
                return (R) target;
            else {
                return null;
            }
        } else if (rs instanceof ResultSet) {
            synchronized (target) {
                final ResultSet resultSet = (ResultSet) rs;
                final Field[] fields = ClassInspectUtil.get(target.getClass());
                if (null == fields)
                    throw new InspectEntityFailedException("The defined field[] of target class not found!");
                try {
                    // move to last row and count the total rows
                    resultSet.last();
                    int total = resultSet.getRow();
                    // reset result set index
                    resultSet.beforeFirst();
                    final Collection<E> result = new ArrayList<>(total);
                    //final ResultSetMetaData metaData = resultSet.getMetaData();
                    while (resultSet.next()) {
                        try {
                            final E child = (E) target.getClass().newInstance();
                            for (Field field : fields) {
                                final Column column = field.getAnnotation(Column.class);
                                final Object value = resultSet.getObject(column.name());
                                if (null != value)
                                    ClassInspectUtil.InspectField.setFieldValue(child, field, value, column);
                            }
                            if (!result.contains(child))
                                result.add(child);
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new ResultSetBindingException("can not create new instance of result element! ", e);
                        }
                    }
                    return (R) result;
                } catch (SQLException e) {
                    throw new ResultSetBindingException("Something happens with result set!", e);
                }
            }
        } else {
            throw new ResultSetBindingException("Default entity binding accepted only java.sql.ResultSet or java.lang.Integer!");
        }
    }
}
