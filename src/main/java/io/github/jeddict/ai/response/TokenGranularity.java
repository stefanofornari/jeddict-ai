/**
 * Copyright 2025 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.jeddict.ai.response;

/**
 *
 * @author Gaurav Gupta
 */
public enum TokenGranularity {
    MINUTE(60_000L),
    HOUR(3_600_000L),
    DAY(86_400_000L),
    WEEK(7 * 86_400_000L),        // 604,800,000 ms
    MONTH(30 * 86_400_000L);      // 2,592,000,000 ms (approximate)

    public final long intervalMillis;

    TokenGranularity(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public long getCurrentBucketKey() {
        return System.currentTimeMillis() / intervalMillis;
    }
}
