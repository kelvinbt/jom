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

package com.kelvin.jom.persistence.impl;

import com.kelvin.jom.defines.enums.ProcessQueryType;
import com.kelvin.jom.defines.interfaces.CallBack;
import com.kelvin.jom.exceptions.CreateInstanceFailedException;
import com.kelvin.jom.exceptions.NilHandlerException;
import com.kelvin.jom.persistence.EntityManager;
import com.kelvin.jom.persistence.handlers.EntityHandler;
import com.kelvin.jom.persistence.handlers.impl.*;
import com.kelvin.jom.persistence.transform.EntityTransform;
import com.kelvin.jom.persistence.transform.impl.EntityTransformImpl;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.Collection;

public final class EntityManagerImpl implements EntityManager {
    private final DataSource dataSource;

    public EntityManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> void insert(T entity) {
        this.getProxy(ProcessQueryType.INSERT, new EntityTransformImpl<Integer, T>(entity), null)
                .process(entity);
    }

    @Override
    public <T> T update(T entity) {
        return this.getProxy(ProcessQueryType.UPDATE, new EntityTransformImpl<Integer, T>(entity), null)
                .process(entity);
    }

    @Override
    public <T> void delete(T entity) {
        this.getProxy(ProcessQueryType.DELETE, new EntityTransformImpl<Integer, T>(entity), null)
                .process(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<T> select(T entity) {
        return (Collection<T>) this.getProxy(ProcessQueryType.SELECT, new EntityTransformImpl<ResultSet, T>(entity), null)
                .process(entity);
    }

    @Override
    public <T> void update(T entity, EntityTransform<Integer> bind) {
        this.getProxy(ProcessQueryType.UPDATE, bind, null)
                .process(entity);
    }

    @Override
    public <T> void insert(T entity, EntityTransform<Integer> bind) {
        this.getProxy(ProcessQueryType.INSERT, bind, null)
                .process(entity);
    }

    @Override
    public <T> void delete(T entity, EntityTransform<Integer> bind) {
        this.getProxy(ProcessQueryType.DELETE, bind, null)
                .process(entity);
    }

    @Override
    public <T> void select(T entity, EntityTransform<ResultSet> bind) {
        this.getProxy(ProcessQueryType.SELECT, bind, null)
                .process(entity);
    }

    @Override
    public <T> void update(T entity, CallBack callBack) {
        this.getProxy(ProcessQueryType.UPDATE, new EntityTransformImpl<Integer, T>(entity), callBack)
                .process(entity);
    }

    @Override
    public <T> void select(T entity, CallBack callBack) {
        this.getProxy(ProcessQueryType.SELECT, new EntityTransformImpl<ResultSet, T>(entity), callBack)
                .process(entity);
    }

    @Override
    public <T> void delete(T entity, CallBack callBack) {
        this.getProxy(ProcessQueryType.DELETE, new EntityTransformImpl<Integer, T>(entity), callBack)
                .process(entity);
    }

    @Override
    public <T> void insert(T entity, CallBack callBack) {
        this.getProxy(ProcessQueryType.INSERT, new EntityTransformImpl<Integer, T>(entity), callBack)
                .process(entity);
    }

    @Override
    public <R, T> R named(String name, Object[] params, Class<T> clazz) {
        try {
            return named(name, params, new EntityTransformImpl<>(clazz.newInstance()), null, clazz);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CreateInstanceFailedException(e);
        }
    }

    @Override
    public <R, T> R named(String name, Object[] params, CallBack<R> callback, Class<T> clazz) {
        try {
            return named(name, params, new EntityTransformImpl<>(clazz.newInstance()), callback, clazz);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CreateInstanceFailedException(e);
        }
    }

    @Override
    public <R, T> R named(String name, Object[] params, EntityTransform<R> transform, CallBack<R> callBack, Class<T> clazz) {
        final EntityHandler handler = this.getProxy(ProcessQueryType.NAMED, transform, callBack);
        if (null != handler) {
            return handler.process(name, params, clazz);
        }
        return null;
    }


    @Override
    public <R, T> R procedure(String name, Object[] params, Class<T> clazz) {
        try {
            return procedure(name, params, new EntityTransformImpl<>(clazz.newInstance()), null, clazz);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CreateInstanceFailedException(e);
        }
    }

    @Override
    public <R, T> R procedure(String name, Object[] params, CallBack<R> callback, Class<T> clazz) {
        try {
            return procedure(name, params, new EntityTransformImpl<>(clazz.newInstance()), callback, clazz);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CreateInstanceFailedException(e);
        }
    }

    @Override
    public <R, T> R procedure(String name, Object[] params, EntityTransform<R> transform, CallBack<R> callBack, Class<T> clazz) {
        final EntityHandler handler = this.getProxy(ProcessQueryType.PROCEDURE, transform, callBack);
        if (null != handler) {
            return handler.call(name, params, clazz);
        }
        return null;
    }

    private <B> EntityHandler getProxy(ProcessQueryType type, EntityTransform<B> binding, CallBack callBack) throws NullPointerException, UnsupportedOperationException {
        EntityHandler desEntityHandler;
        switch (type) {
            case INSERT:
                desEntityHandler = (EntityHandler) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{EntityHandler.class},
                        new EntityProxyInvocationHandler(new EntityInsertHandler(dataSource, binding, callBack)));
                break;
            case DELETE:
                desEntityHandler = (EntityHandler) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{EntityHandler.class},
                        new EntityProxyInvocationHandler(new EntityDeleteHandler(dataSource, binding, callBack)));
                break;
            case SELECT:
                desEntityHandler = (EntityHandler) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{EntityHandler.class},
                        new EntityProxyInvocationHandler(new EntitySelectHandler(dataSource, binding, callBack)));
                break;
            case UPDATE:
                desEntityHandler = (EntityHandler) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{EntityHandler.class},
                        new EntityProxyInvocationHandler(new EntityUpdateHandler(dataSource, binding, callBack)));
                break;
            case NAMED:
                desEntityHandler = (EntityHandler) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{EntityHandler.class},
                        new EntityProxyInvocationHandler(new EntityNamedQueryHandler(dataSource, binding, callBack)));
                break;
            case PROCEDURE:
                desEntityHandler = (EntityHandler) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{EntityHandler.class},
                        new EntityProxyInvocationHandler(new EntityProcedureHandler(dataSource, binding, callBack)));
                break;
            default:
                throw new UnsupportedOperationException("This Query type is not defined!");
        }
        return desEntityHandler;
    }

    private class EntityProxyInvocationHandler implements InvocationHandler {
        private final EntityHandler entityHandler;

        private EntityProxyInvocationHandler(EntityHandler entityHandler) {
            this.entityHandler = entityHandler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (null == entityHandler)
                throw new NilHandlerException("the handler can not be null!");
            return method.invoke(entityHandler, args);
        }
    }
}
