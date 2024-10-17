/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.settings;

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
public class OllamaModelFetcher {

    private static final String API_URL = "http://localhost:11434";
    
    public String getAPIUrl() {
        return API_URL;
    }

    public List<String> fetchModelNames(String apiUrl) {
        List<String> modelNames = new ArrayList<>();

        try {
            URL url = new URL(apiUrl + "/api/tags");
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
                JSONArray models = jsonResponse.getJSONArray("models");

                for (int i = 0; i < models.length(); i++) {
                    JSONObject model = models.getJSONObject(i);
                    String name = model.getString("name");
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
        OllamaModelFetcher fetcher = new OllamaModelFetcher();
        List<String> names = fetcher.fetchModelNames(API_URL);
        System.out.println("Model Names: " + names);
    }
}
