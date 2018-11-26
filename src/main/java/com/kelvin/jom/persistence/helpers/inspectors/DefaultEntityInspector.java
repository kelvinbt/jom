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

package com.kelvin.jom.persistence.helpers.inspectors;

import com.kelvin.jom.defines.annotations.Column;
import com.kelvin.jom.defines.annotations.Queries;
import com.kelvin.jom.defines.annotations.Query;
import com.kelvin.jom.defines.annotations.Table;
import com.kelvin.jom.defines.classes.LimitedMap;
import com.kelvin.jom.defines.consts.SQL;
import com.kelvin.jom.defines.enums.DefineQueryType;
import com.kelvin.jom.exceptions.EntityReferenceException;
import com.kelvin.jom.exceptions.IllegalEntityDefineException;
import com.kelvin.jom.exceptions.InspectEntityFailedException;
import com.kelvin.jom.persistence.helpers.Inspector;
import com.kelvin.jom.utils.ClassInspectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.kelvin.jom.utils.ClassInspectUtil.InspectField.getFieldValue;

public final class DefaultEntityInspector implements Inspector {
    private static final Map<String, Object> entities = new LimitedMap<>();
    private static final Map<String, Query> namedQueries = new LimitedMap<>();
    private static final Map<String, Query> procedureQueries = new LimitedMap<>();
    private static volatile Viewer viewer;
    private Object target;
    private Table table;
    private Queries queries;
    private Query query;
    private Map<Column, Object> fields;

    /**
     * @param target entity
     */
    public DefaultEntityInspector(Object target) {
        this.target = target;
        this.inspect(target);
        registerTargetViewer();
    }

    @SuppressWarnings("unchecked")
    /**
     * @param clazz entity class
     */
    public static <T> T getEntityFromCache(Class<T> clazz) {
        synchronized (entities) {
            Object entity = entities.get(clazz.getSimpleName());
            if (null == entity) {
                try {
                    viewer.value(clazz.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new EntityReferenceException("Can not create new entity instance for inspect metadata!");
                }
            }
            return (T) entities.get(clazz.getSimpleName());
        }
    }

    @Override
    public Map<Column, Object> getFields() {
        return fields;
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public Queries getQueries() {
        return queries;
    }

    @Override
    public Query getQuery() {
        return query;
    }

    @Override
    public Query getNamedQuery(String named, DefineQueryType type) {
        if (null != type) {
            switch (type) {
                case PROCEDURE:
                    return procedureQueries.get(named);
                case NAMED:
                    return namedQueries.get(named);
            }
        }
        return null;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    /**
     * update entity parameters value
     */
    @Override
    public Inspector update(Object target) {
        this.target = target;
        inspect(target);
        return this;
    }

    /**
     * start inspecting object
     * build metadata for entity object
     *
     * @param object entity
     * @throws IllegalEntityDefineException
     * @throws InspectEntityFailedException
     */
    private void inspect(Object object) throws IllegalEntityDefineException, InspectEntityFailedException {
        synchronized (this) {
            if (null == object)
                throw new InspectEntityFailedException("Nil inspect target!");
            final Class clazz = object.getClass();
            inspectType(clazz.getAnnotations());
            inspectFields(clazz.getDeclaredFields());
            cacheEntity(object);
        }
    }

    /**
     * inspect object defined fields
     *
     * @param innerFields entity defined fields
     * @throws InspectEntityFailedException
     */
    private void inspectFields(Field[] innerFields) throws InspectEntityFailedException {
        if (null != innerFields && innerFields.length > 0) {
            //cache all fields
            ClassInspectUtil.put(target.getClass(), innerFields);
            // loop to read all annotation and get field value
            for (Field inner : innerFields) {
                final Column type = inner.getAnnotation(Column.class);
                final Object object = getFieldValue(target, inner, type);
                if (null != object) {
                    if (null == this.fields)
                        this.fields = new HashMap<>(innerFields.length);
                    fields.put(type, object);
                }
            }
        }
    }

    /**
     * cache entity
     *
     * @param target entity
     * @param <T>    entity type
     */
    private <T> void cacheEntity(T target) {
        if (null != target) {
            Object cached = entities.get(target.getClass().getSimpleName());
            if (null == cached) {
                try {
                    cached = target.getClass().newInstance();
                    entities.put(target.getClass().getSimpleName(), cached);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new EntityReferenceException("can not create cache instance for entity " + target.getClass().getSimpleName(), e);
                }
            }
        }
    }

    /**
     * inspect object defined types
     *
     * @param types annotations
     * @throws IllegalEntityDefineException
     * @throws InspectEntityFailedException
     */
    private void inspectType(Annotation[] types) throws IllegalEntityDefineException, InspectEntityFailedException {
        for (Annotation type : types) {
            if (type instanceof Table)
                table = (Table) type;
            if (type instanceof Queries) {
                queries = (Queries) type;
                int length = queries.queries()
                        .length;
                if (length > 0) {
                    synchronized (namedQueries) {
                        for (Query query : queries.queries()) {
                            final String identity = target.getClass().getSimpleName() + SQL.PERIOD + query.name();
                            if (query.type() == DefineQueryType.NAMED)
                                namedQueries.put(identity, query);
                            else if (query.type() == DefineQueryType.PROCEDURE)
                                procedureQueries.put(identity, query);
                        }
                    }
                }
            }
            if (type instanceof Query)
                query = (Query) type;
            if (null != query && null != queries)
                throw new IllegalEntityDefineException("EntityHandler can not have both com.kelvin.jom.defines.annotations.Query and com.kelvin.jom.defines.annotations.Queries annotations!");
            if (null == table)
                throw new IllegalEntityDefineException("EntityHandler must define com.kelvin.jom.defines.annotations.Table!");
        }
    }

    /**
     * register entity target viewer ( for inspecting )
     */
    private void registerTargetViewer() {
        if (null == viewer) {
            viewer = new TargetViewer();
            new InspectTarget(viewer);
        }
    }

    /**
     * section for all observer entity classes
     */


    /**
     * interface of Viewer
     */
    private interface Viewer {
        void bind(TargetObserver obs);

        void value(Object target);

        Object get();

        void notifyChange();
    }

    /**
     * observer holder
     */
    private class TargetViewer implements Viewer {
        private TargetObserver observers;
        private Object target;

        @Override
        public void bind(TargetObserver obs) {
            this.observers = obs;
        }

        @Override
        public void value(Object target) {
            this.target = target;
            notifyChange();
        }

        @Override
        public Object get() {
            return target;
        }

        @Override
        public void notifyChange() {
            observers.update();
        }
    }

    /**
     * extend class of TargetObserver
     * the class for inspect missing entity
     */
    private class InspectTarget extends TargetObserver {
        private InspectTarget(Viewer viewer) {
            super(viewer);
            super.targetViewer.bind(this);
        }

        @Override
        protected void update() {
            if (null == super.targetViewer)
                throw new InspectEntityFailedException("nil target viewer object for inspector");
            final Object target = super.get();
            if (null != target)
                inspect(target);
        }
    }

    /**
     * abstract class for entity observer target
     */
    private abstract class TargetObserver {
        private final Viewer targetViewer;

        private TargetObserver(Viewer targetViewer) {
            this.targetViewer = targetViewer;
        }

        private Object get() {
            return targetViewer.get();
        }

        protected abstract void update();
    }
}
