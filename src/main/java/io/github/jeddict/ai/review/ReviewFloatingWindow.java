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
package io.github.jeddict.ai.review;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

final class ReviewFloatingWindow implements AWTEventListener, WindowFocusListener {

    private final JWindow mainWindow;
    private final List<ReviewValue> reviewValues;
    private ReviewFloatingPanel floatingPanel = null;

    public ReviewFloatingWindow(ReviewPanel master, List<ReviewValue> reviewValues1) {
        this.reviewValues = reviewValues1;
        Window window = SwingUtilities.windowForComponent(master);
        mainWindow = new JWindow(window);
    }

    public void show(Point point) {
        floatingPanel = new ReviewFloatingPanel(new ArrayList<>(reviewValues));
        mainWindow.add(floatingPanel);
        mainWindow.pack();
        mainWindow.setLocation(point);

        mainWindow.setVisible(true);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        mainWindow.addWindowFocusListener(this);
        mainWindow.getOwner().addWindowFocusListener(this);
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            onClick(event);
        }
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        if (mainWindow != null && e.getOppositeWindow() == null) {
            shutdown();
        }
    }

    private void onClick(AWTEvent event) {
        Component component = (Component) event.getSource();
        Window w = SwingUtilities.windowForComponent(component);
        if (w != mainWindow) {
            shutdown();
        }
    }

    void shutdown() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        mainWindow.getOwner().removeWindowFocusListener(this);
        mainWindow.removeWindowFocusListener(this);
        mainWindow.dispose();
        if (floatingPanel != null) {
            floatingPanel.shutdown();
        }
    }

    public boolean isVisible() {
        return mainWindow.isVisible();
    }

    public void dispose() {
        shutdown();
    }
}
