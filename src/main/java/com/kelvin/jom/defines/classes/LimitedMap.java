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

import com.kelvin.jom.defines.consts.LIMIT;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * stored limited objects and remove the eldest entry if the size excess the threshold
 * default threshold is 500
 *
 * @param <Key>
 * @param <Value>
 */
public class LimitedMap<Key, Value> extends LinkedHashMap<Key, Value> {
    private int threshold = LIMIT.STORED_LIMIT;

    public LimitedMap() {
    }

    public LimitedMap(int initialCapacity) {
        threshold = initialCapacity;
    }

    public int acceptedThreshold() {
        return threshold - size();
    }

    private boolean check() {
        return size() > threshold;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Key, Value> eldest) {
        return check();
    }
}


