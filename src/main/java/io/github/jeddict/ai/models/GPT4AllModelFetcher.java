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
package io.github.jeddict.ai.models;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Shiwani Gupta
 */
public class GPT4AllModelFetcher {

    private static final String API_URL = "http://localhost:4891/v1";

    public String getAPIUrl() {
        return API_URL;
    }

    public List<String> fetchModelNames(String apiUrl) {
        List<String> modelNames = new ArrayList<>();

        try {
            URL url = new URL(apiUrl + "/models");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray models = jsonResponse.getJSONArray("data");

                for (int i = 0; i < models.length(); i++) {
                    JSONObject model = models.getJSONObject(i);
                    String name = model.getString("id");  // Assuming 'id' holds the model name
                    modelNames.add(name);
                }
            } else {
                System.err.println("GET request failed. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return modelNames;
    }

    public static void main(String[] args) {
        GPT4AllModelFetcher fetcher = new GPT4AllModelFetcher();
        List<String> names = fetcher.fetchModelNames(API_URL);
        System.out.println("Model Names: " + names);
    }
}
