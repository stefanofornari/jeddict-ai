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
import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import javax.swing.*;
import java.awt.*;
import org.json.JSONObject;

public class TokenUsageChartDialog extends JDialog {

    private final JTabbedPane tabbedPane = new JTabbedPane();

    public TokenUsageChartDialog(Frame owner) {
        super(owner, "Token Usage Charts", true);
        setLayout(new BorderLayout());

        boolean isDark = ColorUtil.isDarkColor(getBackgroundColorFromMimeType(MIME_PLAIN_TEXT));
        // Common fonts and colors
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        Color bgColor = isDark ? new Color(43, 43, 43) : Color.WHITE;
        Color fgColor = isDark ? new Color(187, 187, 187) : Color.BLACK;
        Color tabBgColor = isDark ? new Color(60, 63, 65) : Color.WHITE;
        Color borderColor = isDark ? new Color(70, 70, 70) : Color.LIGHT_GRAY;

        getContentPane().setBackground(bgColor);

        tabbedPane.setFont(font);
        tabbedPane.setBackground(tabBgColor);
        tabbedPane.setForeground(fgColor);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        topPanel.setBackground(bgColor);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(bgColor);

        JComboBox<TokenGranularity> timeframeComboBox = new JComboBox<>(TokenGranularity.values());
        timeframeComboBox.setSelectedItem(PreferencesManager.getInstance().getTokenGranularity());
        timeframeComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        timeframeComboBox.setBackground(isDark ? new Color(69, 73, 74) : Color.WHITE);
        timeframeComboBox.setForeground(fgColor);

        JLabel label = new JLabel("Timeframe:");
        label.setForeground(fgColor);
        label.setFont(font);

        buttonPanel.add(label);
        buttonPanel.add(timeframeComboBox);

        timeframeComboBox.addActionListener(e -> {
            TokenGranularity selected = (TokenGranularity) timeframeComboBox.getSelectedItem();
            TokenGranularity current = PreferencesManager.getInstance().getTokenGranularity();
            if (!selected.equals(current)) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Changing timeframe will erase existing token usage data.\nContinue?",
                        "Confirm Timeframe Change",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (response == JOptionPane.YES_OPTION) {
                    PreferencesManager.getInstance().setDailyInputTokenStats(new JSONObject());
                    PreferencesManager.getInstance().setDailyOutputTokenStats(new JSONObject());

                    PreferencesManager.getInstance().setTokenGranularity(selected);

                    getContentPane().removeAll();
                    dispose();
                } else {
                    timeframeComboBox.setSelectedItem(current);
                }
            }
        });

        add(topPanel, BorderLayout.NORTH);

        TokenUsageChartFactory.resetTheme();
        rebuildCharts(); // Initial load

        add(tabbedPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        closeButton.setBackground(new Color(0x4A90E2));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setSize(900, 600);
        setLocationRelativeTo(owner);
    }

    private void rebuildCharts() {
        tabbedPane.removeAll();
        tabbedPane.addTab("Input Tokens", TokenUsageChartFactory.createInputChartPanel());
        tabbedPane.addTab("Output Tokens", TokenUsageChartFactory.createOutputChartPanel());
        tabbedPane.addTab("Combined", TokenUsageChartFactory.createCombinedChartPanel());
    }

    public static void showDialog(Component parentComponent) {
        Window window = SwingUtilities.getWindowAncestor(parentComponent);
        Frame frame = window instanceof Frame ? (Frame) window : null;

        TokenUsageChartDialog dialog = new TokenUsageChartDialog(frame);
        dialog.setVisible(true);
    }
}
