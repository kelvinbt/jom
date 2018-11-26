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

package com.kelvin.jom.persistence.helpers.abstr;

import com.kelvin.jom.defines.annotations.Column;
import com.kelvin.jom.defines.annotations.Query;
import com.kelvin.jom.defines.annotations.Table;
import com.kelvin.jom.defines.classes.LimitedMap;
import com.kelvin.jom.defines.consts.SQL;
import com.kelvin.jom.defines.enums.DefineQueryType;
import com.kelvin.jom.exceptions.EntityReferenceException;
import com.kelvin.jom.exceptions.NamedQueryNotFoundException;
import com.kelvin.jom.exceptions.QueryParamException;
import com.kelvin.jom.persistence.helpers.Inspector;
import com.kelvin.jom.persistence.helpers.QueryBuilder;
import com.kelvin.jom.persistence.helpers.inspectors.DefaultEntityInspector;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @param <T>
 */
public abstract class AbstractEntityQueryBuilder<T> implements QueryBuilder {
    private static volatile Map<Class, Inspector> inspectors = new LimitedMap<>();
    protected Inspector inspector;
    private T entity;
    private Object[] params;


    private AbstractEntityQueryBuilder() {
    }

    public AbstractEntityQueryBuilder(T entity) {
        this.entity = entity;
        create();
    }

    /**
     * for custom sql build
     *
     * @return sql String
     */
    protected abstract String build();

    public Object[] getParams() {
        return params;
    }

    protected void setParams(Object[] params) {
        this.params = params;
    }

    /**
     * create generic instance of entity for caching annotations and definition of entity named sql
     *
     * @throws EntityReferenceException
     */
    @SuppressWarnings("unchecked")
    private void create() throws EntityReferenceException {
        final Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            final Type actual = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (null == actual)
                throw new EntityReferenceException("Parameterize type must be not null and must be a instance of entity class");
            try {
                final Class<?> clazz = entity.getClass();
                this.inspector = inspectors.get(clazz);
                if (null == this.inspector) {
                    this.inspector = new DefaultEntityInspector(entity);
                    inspectors.put(clazz, this.inspector);
                } else {
                    this.inspector.update(entity);
                }
            } catch (ClassCastException e) {
                throw new EntityReferenceException("Can not cast param type class to actual entity class " + e.getMessage());
            }
        }
    }

    protected void reusableWhereBlock(final StringBuilder builder, Table table, Map<Column, Object> fields, boolean validateKeyColumn) {
        try {
            if (table.schema().length() > 0)
                builder.append(table.schema())
                        .append(SQL.PERIOD);
            builder.append(table.name())
                    .append(SQL.SPACE)
                    .append(SQL.TABLE_PREFIX);

            if (null != fields) {
                int last = fields.entrySet().size();
                int increase = 0;
                final Object[] params = new Object[fields.size()];
                Column key = null;
                for (Map.Entry<Column, Object> entry : fields.entrySet()) {
                    if (entry.getKey().id())
                        key = entry.getKey();
                    if (increase == 0)
                        builder.append(SQL.WHERE);
                    builder.append(SQL.TABLE_PREFIX)
                            .append(SQL.PERIOD)
                            .append(entry.getKey().name())
                            .append(SQL.EQUALS).append(SQL.QUESTION_MARK);
                    params[increase++] = entry.getValue();
                    if (increase != last)
                        builder.append(SQL.AND);
                }
                if (validateKeyColumn && null == key)
                    throw new QueryParamException("Nil key param from entity mapping for update sql");
                setParams(params);
            }
        } finally {
            builder.append(SQL.SEMICOLON);
        }
    }

    protected String getNamedQueryByType(String name, DefineQueryType type) {
        final Query query = inspector.getNamedQuery(name, type);
        if (null == query)
            throw new NamedQueryNotFoundException("Named query or procedure " + name + "not found!");
        return query.statement();
    }
}
