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
import com.kelvin.jom.persistence.helpers.abstr.AbstractEntityQueryBuilder;

import java.util.Map;

public final class SelectEntityQueryBuilder<T> extends AbstractEntityQueryBuilder<T> {
    public SelectEntityQueryBuilder(T entity) {
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
        final StringBuilder builder = new StringBuilder(SQL.SELECT)
                .append(SQL.SPACE)
                .append(SQL.ASTERISK)
                .append(SQL.SPACE)
                .append(SQL.FROM);
        super.reusableWhereBlock(builder, table, fields, false);
        return builder.toString();
    }
}
