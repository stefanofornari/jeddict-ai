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
package io.github.jeddict.ai.settings;

import org.json.JSONObject;

/**
 *
 * @author Gaurav Gupta
 */
public class ReportManager {

    private static ReportManager instance;
    private static final String DAILY_INPUT_TOKEN_STATS_KEY = "dailyInputTokenStats";
    private static final String DAILY_OUTPUT_TOKEN_STATS_KEY = "dailyOutputTokenStats";
    private JSONObject dailyInputTokenStats;
    private JSONObject dailyOutputTokenStats;

    private ReportManager() {
        // Constructor is now empty
    }

    public static ReportManager getInstance() {
        if (instance == null) {
            synchronized (ReportManager.class) {
                if (instance == null) {
                    instance = new ReportManager();
                }
            }
        }
        return instance;
    }
    
    public JSONObject getDailyInputTokenStats() {
        if (dailyInputTokenStats == null) {
            dailyInputTokenStats = PreferencesManager.getInstance().getChild(DAILY_INPUT_TOKEN_STATS_KEY);
        }
        return dailyInputTokenStats;
    }

    public void setDailyInputTokenStats(JSONObject usage) {
        this.dailyInputTokenStats = usage;
        PreferencesManager.getInstance().setChild(DAILY_INPUT_TOKEN_STATS_KEY, usage);
    }

    public JSONObject getDailyOutputTokenStats() {
        if (dailyOutputTokenStats == null) {
            dailyOutputTokenStats = PreferencesManager.getInstance().getChild(DAILY_OUTPUT_TOKEN_STATS_KEY);
        }
        return dailyOutputTokenStats;
    }

    public void setDailyOutputTokenStats(JSONObject usage) {
        this.dailyOutputTokenStats = usage;
        PreferencesManager.getInstance().setChild(DAILY_OUTPUT_TOKEN_STATS_KEY, usage);
    }
}
