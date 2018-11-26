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

package com.kelvin.jom.persistence.handlers.impl;

import com.kelvin.jom.defines.consts.SQL;
import com.kelvin.jom.defines.enums.ProcessQueryType;
import com.kelvin.jom.defines.interfaces.CallBack;
import com.kelvin.jom.persistence.factories.EntityQueryBuilderFactory;
import com.kelvin.jom.persistence.handlers.abst.AbstractEntityHandler;
import com.kelvin.jom.persistence.helpers.QueryBuilder;
import com.kelvin.jom.persistence.helpers.impl.ProcedureQueryBuilder;
import com.kelvin.jom.persistence.helpers.inspectors.DefaultEntityInspector;
import com.kelvin.jom.persistence.transform.EntityTransform;

import javax.sql.DataSource;

public class EntityProcedureHandler extends AbstractEntityHandler {
    public EntityProcedureHandler(DataSource dataSource, EntityTransform entityBinding, CallBack callBack) {
        super(dataSource, entityBinding, callBack);
    }

    @Override
    public <R, T> R call(String procedure, Object[] params, Class<T> clazz) {
        final Object instance = DefaultEntityInspector.getEntityFromCache(clazz);
        if (null != instance) {
            final QueryBuilder builder = EntityQueryBuilderFactory
                    .getQueryBuilder(ProcessQueryType.PROCEDURE, instance);
            if (builder instanceof ProcedureQueryBuilder) {
                ProcedureQueryBuilder named = (ProcedureQueryBuilder) builder;
                String sql = named.procedure(clazz.getSimpleName() + SQL.PERIOD + procedure);
                if (null != sql) {
                    return super.procedure(sql, params);
                }
            }
        }
        return null;
    }
}
