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

package com.kelvin.jom.persistence.helpers.impl;

import com.kelvin.jom.defines.annotations.Column;
import com.kelvin.jom.defines.annotations.Table;
import com.kelvin.jom.defines.consts.SQL;
import com.kelvin.jom.exceptions.QueryParamException;
import com.kelvin.jom.persistence.helpers.abstr.AbstractEntityQueryBuilder;

import java.util.Map;

public final class UpdateEntityQueryBuilder<T> extends AbstractEntityQueryBuilder<T> {
    public UpdateEntityQueryBuilder(T entity) {
        super(entity);
    }

    @Override
    public String sql() {
        return build();
    }

    @Override
    public Object[] params() {
        return super.getParams();
    }

    @Override
    protected String build() {
        final Table table = inspector.getTable();
        final Map<Column, Object> fields = inspector.getFields();
        int last = fields.entrySet().size();
        int increase = 0;
        final StringBuilder builder = new StringBuilder(SQL.UPDATE)
                .append(SQL.SPACE);
        if (table.schema().length() > 0)
            builder.append(table.schema()).append(SQL.PERIOD);
        builder.append(table.name())
                .append(SQL.SPACE)
                .append(SQL.TABLE_PREFIX);
        try {
            final Object[] params = new Object[fields.size()];
            Column key = null;
            final StringBuilder setBlock = new StringBuilder(SQL.SET);
            final StringBuilder whereBlock = new StringBuilder(SQL.WHERE);
            boolean afterWhereBlock = false;
            Object whereParams = null;
            for (Map.Entry<Column, Object> entry : fields.entrySet()) {
                if (entry.getKey().id()) {
                    key = entry.getKey();
                    whereBlock.append(SQL.TABLE_PREFIX)
                            .append(SQL.PERIOD)
                            .append(key.name())
                            .append(SQL.EQUALS)
                            .append(SQL.QUESTION_MARK);
                    whereParams = entry.getValue();
                    afterWhereBlock = true;
                } else {
                    if (increase > 0 && increase != last && !afterWhereBlock)
                        setBlock.append(SQL.COMMA);
                    setBlock.append(entry.getKey().name())
                            .append(SQL.EQUALS)
                            .append(SQL.QUESTION_MARK);
                    params[increase++] = entry.getValue();
                }
            }
            params[increase++] = whereParams;
            setParams(params);
            if (null == key)
                throw new QueryParamException("Nil key param from entity mapping for update sql");
            builder.append(setBlock)
                    .append(whereBlock);
        } finally {
            builder.append(SQL.SEMICOLON);
        }
        return builder.toString();
    }
}
