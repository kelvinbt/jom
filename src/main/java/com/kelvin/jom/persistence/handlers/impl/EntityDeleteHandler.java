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

import com.kelvin.jom.defines.enums.ProcessQueryType;
import com.kelvin.jom.defines.interfaces.CallBack;
import com.kelvin.jom.persistence.factories.EntityQueryBuilderFactory;
import com.kelvin.jom.persistence.handlers.abst.AbstractEntityHandler;
import com.kelvin.jom.persistence.helpers.QueryBuilder;
import com.kelvin.jom.persistence.transform.EntityTransform;

import javax.sql.DataSource;

public class EntityDeleteHandler extends AbstractEntityHandler {
    public EntityDeleteHandler(DataSource dataSource, EntityTransform entityBinding, CallBack callBack) {
        super(dataSource, entityBinding, callBack);
    }

    @Override
    public <T> T process(T entity) {
        final QueryBuilder builder = EntityQueryBuilderFactory.getQueryBuilder(ProcessQueryType.DELETE, entity);
        try {
            execute(builder.sql(), builder.params());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
