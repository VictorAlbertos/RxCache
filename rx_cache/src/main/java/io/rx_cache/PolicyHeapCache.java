/*
 * Copyright 2015 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rx_cache;

/**
 * Sets the percentage to be used for the the in memory cache layer.
 * The memory cache will use as much memory as resulting of this percentage,
 * regardless the current memory available in the heap. So you may incur in out of memory errors
 * if you by your own allocates chunks of memory not managed by the library
 */
public enum PolicyHeapCache {
    CONSERVATIVE(.40), MODERATE(.60), AGGRESSIVE(.80);

    private double percentage;

    PolicyHeapCache(double percentage) {
        this.percentage = percentage;
    }

    public double getPercentageReserved() {
        return percentage;
    }
}


