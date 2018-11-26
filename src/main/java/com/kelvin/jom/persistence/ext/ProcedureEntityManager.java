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

import com.kelvin.jom.defines.interfaces.CallBack;
import com.kelvin.jom.persistence.transform.EntityTransform;

public interface ProcedureEntityManager {
    <R, T> R procedure(String name, Object[] params, Class<T> clazz);

    <R, T> R procedure(String name, Object[] params, CallBack<R> callback, Class<T> clazz);

    <R, T> R procedure(String name, Object[] params, EntityTransform<R> transform, CallBack<R> callBack, Class<T> clazz);
}
