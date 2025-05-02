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
package io.github.jeddict.ai.components;

import io.github.jeddict.ai.response.TokenGranularity;
import io.github.jeddict.ai.settings.PreferencesManager;
import static io.github.jeddict.ai.util.ColorUtil.isDarkColor;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;

public class TokenUsageChartFactory {

    public static boolean darkThemeEnabled = false;

    public static void resetTheme() {
        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        darkThemeEnabled = isDarkColor(backgroundColor);
    }

    public static JPanel createInputChartPanel() {
        JSONObject inputStats = PreferencesManager.getInstance().getDailyInputTokenStats();
        return createBarChartPanel(inputStats, "Input Token Usage", "Input Tokens", getInputColor());
    }

    public static JPanel createOutputChartPanel() {
        JSONObject outputStats = PreferencesManager.getInstance().getDailyOutputTokenStats();
        return createBarChartPanel(outputStats, "Output Token Usage", "Output Tokens", getOutputColor());
    }

    public static JPanel createCombinedChartPanel() {
        JSONObject inputStats = PreferencesManager.getInstance().getDailyInputTokenStats();
        JSONObject outputStats = PreferencesManager.getInstance().getDailyOutputTokenStats();
        return createCombinedBarChartPanel(inputStats, outputStats, "Combined Token Usage");
    }

    private static JPanel createBarChartPanel(JSONObject stats, String title, String label, Color color) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        TokenGranularity granularity = PreferencesManager.getInstance().getTokenGranularity();
        long now = System.currentTimeMillis() / granularity.intervalMillis;

        int count = 0;
        for (int i = 29; i >= 0; i--) {
            long bucket = now - i;
            int input = 0;
            try {
                input = stats.getInt(String.valueOf(bucket));
            } catch (Exception ex) {
            }
            count = count + input;
        }
        label = label + "(" + count + ")";
        for (int i = 29; i >= 0; i--) {
            long bucket = now - i;
            int tokens = 0;
            try {
                tokens = stats.getInt(String.valueOf(bucket));
            } catch (Exception ex) {}
            String labelStr = granularity.name().charAt(0) + granularity.name().substring(1).toLowerCase() + "-" + (30 - i);
            dataset.addValue(tokens, label, labelStr);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title + " (Last 30 " + granularity.name().toLowerCase() + "s)",
                granularity.name(), "Tokens", dataset);

        customizeChart(chart, color, 0);

        return new ChartPanel(chart);
    }

    private static JPanel createCombinedBarChartPanel(JSONObject inputStats, JSONObject outputStats, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        TokenGranularity granularity = PreferencesManager.getInstance().getTokenGranularity();
        long now = System.currentTimeMillis() / granularity.intervalMillis;

        int inputCount = 0, outputCount = 0;
        for (int i = 29; i >= 0; i--) {
            long bucket = now - i;
            int input = 0;
            int output = 0;
            try {
                input = inputStats.getInt(String.valueOf(bucket));
            } catch (Exception ex) {}
            try {
                output = outputStats.getInt(String.valueOf(bucket));
            } catch (Exception ex) {}

            inputCount = inputCount + input;
            outputCount = outputCount + output;
        }
        String inputLabel = "Input Tokens (" + inputCount + ")";
        String outputLabel = "Output Tokens (" + outputCount + ")";
        
        for (int i = 29; i >= 0; i--) {
            long bucket = now - i;
            int input = 0;
            int output = 0;
            try {
                input = inputStats.getInt(String.valueOf(bucket));
            } catch (Exception ex) {}
            try {
                output = outputStats.getInt(String.valueOf(bucket));
            } catch (Exception ex) {}

            String label = granularity.name().charAt(0) + granularity.name().substring(1).toLowerCase() + "-" + (30 - i);

            dataset.addValue(input, inputLabel, label);
            dataset.addValue(output, outputLabel, label);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title + " (Last 30 " + granularity.name().toLowerCase() + "s)",
                granularity.name(), "Tokens", dataset);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, getInputColor());
        renderer.setSeriesPaint(1, getOutputColor());
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setItemMargin(0.1);

        stylePlot(plot);
        styleChart(chart);

        return new ChartPanel(chart);
    }

    private static void customizeChart(JFreeChart chart, Color color, int seriesIndex) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(seriesIndex, color);
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setItemMargin(0.1);

        stylePlot(plot);
        styleChart(chart);
    }

    private static void stylePlot(CategoryPlot plot) {
        plot.setBackgroundPaint(darkThemeEnabled ? new Color(30, 30, 30) : Color.WHITE);
        plot.setRangeGridlinePaint(darkThemeEnabled ? Color.LIGHT_GRAY : Color.GRAY);
        plot.setDomainGridlinesVisible(true);
    }

    private static void styleChart(JFreeChart chart) {
        chart.setAntiAlias(true);
        chart.setBackgroundPaint(darkThemeEnabled ? new Color(40, 40, 40) : Color.WHITE);
        chart.getTitle().setPaint(darkThemeEnabled ? Color.WHITE : Color.BLACK);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setLabelPaint(darkThemeEnabled ? Color.WHITE : Color.BLACK);
        plot.getRangeAxis().setLabelPaint(darkThemeEnabled ? Color.WHITE : Color.BLACK);
        plot.getDomainAxis().setTickLabelPaint(darkThemeEnabled ? Color.WHITE : Color.BLACK);
        plot.getRangeAxis().setTickLabelPaint(darkThemeEnabled ? Color.WHITE : Color.BLACK);
    }

    private static Color getInputColor() {
        return darkThemeEnabled ? new Color(0x6BA4FF) : new Color(0x4A90E2);
    }

    private static Color getOutputColor() {
        return darkThemeEnabled ? new Color(0xFF7690) : new Color(0xE94E77);
    }
}
