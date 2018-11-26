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

package com.kelvin.jom.persistence.handlers.abst;

import com.kelvin.jom.defines.interfaces.CallBack;
import com.kelvin.jom.exceptions.DatabaseException;
import com.kelvin.jom.exceptions.NotImplementedMethodException;
import com.kelvin.jom.persistence.handlers.EntityHandler;
import com.kelvin.jom.persistence.transform.EntityTransform;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class AbstractEntityHandler implements EntityHandler {
    private final EntityTransform entityBinding;
    private final CallBack callback;
    private final DataSource dataSource;

    public AbstractEntityHandler(DataSource dataSource, EntityTransform entityBinding, CallBack callBack) {
        this.dataSource = dataSource;
        this.entityBinding = entityBinding;
        this.callback = callBack;
    }

    @SuppressWarnings("unchecked")
    protected <T> T execute(String sql, Object[] params) throws DatabaseException {
        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement pstm = connection.prepareStatement(sql)) {
                int index = 1;
                for (Object param : params) {
                    pstm.setObject(index++, param);
                }
                int i = pstm.executeUpdate();
                if (null != callback)
                    callback.completed(entityBinding.bind(i));
                return (T) entityBinding.bind(i);
            }
        } catch (Exception e) {
            if (null != callback) {
                callback.exception(e);
            } else throw new DatabaseException(e);

        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T> T query(String sql, Object[] params) throws DatabaseException {
        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement pstm = connection.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY)) {
                if (null != params) {
                    int index = 1;
                    for (Object param : params)
                        pstm.setObject(index++, param);
                }
                try (ResultSet rs = pstm.executeQuery()) {
                    if (null != callback)
                        callback.completed(entityBinding.bind(rs));
                    else return (T) entityBinding.bind(rs);
                }
            }
        } catch (Exception e) {
            if (null != callback)
                callback.exception(e);
            else throw new DatabaseException(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T> T procedure(String sql, Object[] params) throws DatabaseException {
        try (final Connection connection = dataSource.getConnection()) {
            try (final CallableStatement cstm = connection.prepareCall(sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY)) {
                if (null != params) {
                    int index = 1;
                    for (Object param : params)
                        cstm.setObject(index++, param);
                }
                try (ResultSet rs = cstm.executeQuery()) {
                    if (null != callback)
                        callback.completed(entityBinding.bind(rs));
                    else return (T) entityBinding.bind(rs);
                }
            }
        } catch (Exception e) {
            if (null != callback)
                callback.exception(e);
            else throw new DatabaseException(e);
        }
        return null;
    }

    @Override
    public <T> T process(T entity) {
        throw new NotImplementedMethodException("Can not process this function from this class");
    }

    @Override
    public <R, T> R process(String queryName, Object[] params, Class<T> clazz) {
        throw new NotImplementedMethodException("Can not process this function from this class");
    }

    @Override
    public <R, T> R call(String procedure, Object[] params, Class<T> clazz) {
        throw new NotImplementedMethodException("Can not process this function from this class");
    }
}
