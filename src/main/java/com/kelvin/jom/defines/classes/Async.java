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

package com.kelvin.jom.defines.classes;

import com.kelvin.jom.defines.interfaces.CallBack;

import java.util.concurrent.Callable;

public class Async<R> implements Runnable {
    private final CallBack<R> callBack;
    private final Callable<R> callable;
    private boolean running = true;

    public Async(Callable<R> callable, CallBack<R> callBack) {
        this.callable = callable;
        this.callBack = callBack;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            R result = callable.call();
            callBack.completed(result);
        } catch (Exception e) {
            callBack.exception(e);
        } finally {
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }
}
