/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.jeddict.ai.models;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Shiwani Gupta
 */
public class GroqModelFetcher {

    public static final String API_URL = "https://api.groq.com/openai/v1";

    public String getAPIUrl() {
        return API_URL;
    }

    public static List<String> fetchModels(String baseUrl, String token) {
        if (baseUrl == null || baseUrl.isEmpty() || token == null || token.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/models"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray modelsArray = jsonResponse.getJSONArray("data"); // Fetch data array
                
                List<String> modelList = new ArrayList<>();
                for (int i = 0; i < modelsArray.length(); i++) {
                    JSONObject modelObject = modelsArray.getJSONObject(i);
                    String modelId = modelObject.getString("id"); // Get the "id" of the model
                    modelList.add(modelId);
                }
                return modelList;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.EMPTY_LIST;
    }
}
