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

import com.kelvin.jom.defines.enums.DefineQueryType;
import com.kelvin.jom.exceptions.IllegalEntityDefineException;
import com.kelvin.jom.persistence.helpers.abstr.AbstractEntityQueryBuilder;

public final class NamedQueryBuilder<T> extends AbstractEntityQueryBuilder<T> {
    public NamedQueryBuilder(T entity) {
        super(entity);
    }

    @Override
    protected String build() {
        validate();
        return inspector.getQuery().statement();
    }

    public String query(String name) {
        return super.getNamedQueryByType(name, DefineQueryType.NAMED);
    }

    @Override
    public String sql() {
        return build();
    }

    @Override
    public Object[] params() {
        return super.getParams();
    }

    private void validate() {
        if (null == inspector.getQuery() || null == inspector.getQueries()) {
            throw new IllegalEntityDefineException("There are no named queries on the entity class!");
        }
    }
}
