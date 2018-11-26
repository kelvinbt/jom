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

package com.kelvin.jom.persistence.ext;

import com.kelvin.jom.persistence.transform.EntityTransform;

import java.sql.ResultSet;

public interface TransformableEntityManager {
    <T> void update(T entity, EntityTransform<Integer> bind);

    <T> void select(T entity, EntityTransform<ResultSet> bind);

    <T> void delete(T entity, EntityTransform<Integer> bind);

    <T> void insert(T entity, EntityTransform<Integer> bind);
}
