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

import io.github.jeddict.ai.util.EditorUtil;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 *
 * @author Gaurav Gupta
 */
public final class ReviewPanel extends JPanel implements DocumentListener, ComponentListener, FoldHierarchyListener {

    private Color backgroundColor = Color.WHITE;
    private final boolean enabled = true;
    private final JTextComponent textComponent;
    private final BaseDocument document;
    private final LookupListener lookupListener;

    private static int sideBarWidth;
    private static final Logger LOGGER = Logger.getLogger(ReviewPanel.class.getName());

    private final Map<ReviewValue, ReviewProvider> reviewValueToProvider = new HashMap<>();
    private List<ReviewValue> currentPopupValues;
    private Point currentPopupLocation;
    private ReviewFloatingWindow hoverPopup;
    private Point lastHoverPoint;

    public static ReviewPanel create(JTextComponent editor) {
        Font newFont = EditorUtil.getFontFromMimeType(MIME_PLAIN_TEXT);
        sideBarWidth = newFont.getSize();
        ReviewPanel panel = new ReviewPanel(editor);

        panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                panel.updateTooltip(evt);
            }
        });
        return panel;
    }

    private ReviewPanel(JTextComponent editor) {
        super(new BorderLayout());
        this.textComponent = editor;
        this.document = (BaseDocument) editor.getDocument();
        updateColors();

        Lookup.Result<FontColorSettings> lookupResult = MimeLookup.getLookup(MimePath.get(NbEditorUtilities.getMimeType(textComponent))).lookupResult(FontColorSettings.class);
        lookupListener = (LookupEvent le) -> updateColors();
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, lookupListener, lookupResult));
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    public BaseDocument getDocument() {
        return document;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = textComponent.getSize();
        dim.width = sideBarWidth;
        return dim;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        if (!enabled) {
            return;
        }
        super.paintComponent(g);
        Utilities.runViewHierarchyTransaction(textComponent, true, () -> paintComponentHierarchy(g));
    }

    private void paintComponentHierarchy(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Rectangle clip = g2d.getClipBounds();

        g2d.setColor(backgroundColor());
        g2d.fillRect(clip.x, clip.y, clip.width, clip.height);

        JTextComponent component = textComponent;
        TextUI textUI = component.getUI();
        if (textUI == null) {
            return;
        }
        EditorUI editorUI = Utilities.getEditorUI(component);
        if (editorUI == null) {
            return;
        }
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) {
            return;
        }
        try {
            List<ReviewBar> reviewBars = new ArrayList<>();

            int startPos = getPosFromY(component, textUI, clip.y);
            int startViewIndex = rootView.getViewIndex(startPos, Position.Bias.Forward);
            int rootViewCount = rootView.getViewCount();
            List<ReviewProvider> providers = getEnabledProviders();
            if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                Map<ReviewProvider, Map<String, List<ReviewValue>>> variableReviewValues = createVariableReviewValuesMap(providers);
                int clipEndY = clip.y + clip.height;
                int start = getStartIndex(startViewIndex, providers);

                double lineHeight;

                for (int i = start; i < rootViewCount; i++) {

                    reviewValueToProvider.clear();
                    View view = rootView.getView(i);
                    if (view == null) {
                        break;
                    }

                    Rectangle rec1 = component.modelToView(view.getStartOffset());
                    Rectangle rec2 = component.modelToView(view.getEndOffset() - 1);
                    if (rec2 == null || rec1 == null) {
                        break;
                    }

                    int y = rec1.y;
                    lineHeight = (rec2.getY() + rec2.getHeight() - rec1.getY());

                    if (document != null) {
                        String line = EditorUtil.getLineText((BaseDocument) document, view);
                        int indexOfLF = line.indexOf("\n");
                        if (indexOfLF != -1) {
                            line = line.substring(0, indexOfLF);
                        }

                        for (ReviewProvider provider : providers) {
                            List<ReviewValue> reviewValues = provider.getValues(document, line, i + 1, variableReviewValues.get(provider));
                            for (ReviewValue cv : reviewValues) {
                                reviewValueToProvider.put(cv, provider);
                            }
                        }

                        if (i < startViewIndex) {
                            continue;
                        }

                        List<ReviewValue> allReviewValues = new ArrayList<>(reviewValueToProvider.keySet());
                        if (!allReviewValues.isEmpty()) {
                            for (ReviewValue rv : allReviewValues) {
                                ReviewBar existingBar = null;
                                for (ReviewBar cb : reviewBars) {
                                    if (cb.color.equals(rv.getColor())) {
                                        existingBar = cb;
                                        break;
                                    }
                                }
                                if (existingBar == null) {
                                    // First occurrence: create new bar
                                    ReviewBar newBar = new ReviewBar(rv.getColor(), y + 2, (int) lineHeight - 4);
                                    reviewBars.add(newBar);
                                } else {
                                    // Extend the existing bar height
                                    int newY = y + 2;
                                    int newHeight = (int) lineHeight - 4;
                                    if (newY < existingBar.y) {
                                        // Adjust y and height accordingly
                                        int diff = existingBar.y - newY;
                                        existingBar.y = newY;
                                        existingBar.height += diff;
                                    } else if (newY == existingBar.y + existingBar.height) {
                                        existingBar.height += newHeight;
                                    } else if (newY > existingBar.y + existingBar.height) {
                                        // gap between bars, you may want to create separate bar or merge differently
                                        // For simplicity, just extend height to cover
                                        existingBar.height = (newY + newHeight) - existingBar.y;
                                    } else {
                                        // overlapping or inside range - ignore
                                    }
                                }
                            }
                        }
                    }
                    y += lineHeight;
                    if (y >= clipEndY) {
                        break;
                    }
                }

                for (ReviewBar cb : reviewBars) {
                    g2d.setColor(cb.color);
                    g2d.fillRect(0, cb.y, getWidth(), cb.height);

                    g2d.setColor(Color.GRAY);
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawLine(0, cb.y + cb.height, getWidth(), cb.y + cb.height);
                }
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Incorrect offset : {0}", ex.offsetRequested());
        }
    }
    
    
    private void updateTooltip(MouseEvent e) {
        int y = e.getY();
        int x = e.getX();

        try {
            TextUI textUI = textComponent.getUI();
            int pos;
            if (textUI instanceof BaseTextUI) {
                pos = ((BaseTextUI) textUI).getPosFromY(y);
            } else {
                pos = textUI.viewToModel(textComponent, new Point(0, y));
            }

            if (pos < 0) {
                setToolTipText(null);
                hideHoverPopup();
                return;
            }

            int line = LineDocumentUtils.getLineIndex(document, pos) + 1;
            if (line < 0) {
                setToolTipText(null);
                hideHoverPopup();
                return;
            }

            String lineText = EditorUtil.getLineText((BaseDocument) document, line);

            List<ReviewProvider> providers = getEnabledProviders();
            Map<ReviewProvider, Map<String, List<ReviewValue>>> variableReviewValues = createVariableReviewValuesMap(providers);

            List<ReviewValue> values = new ArrayList<>();
            for (ReviewProvider provider : providers) {
                values.addAll(provider.getValues(document, lineText, line, variableReviewValues.get(provider)));
            }

            if (values.isEmpty()) {
                setToolTipText(null);
                hideHoverPopup();
                currentPopupValues = null;
                currentPopupLocation = null;
                return;
            }

            ReviewValue hovered = null;

            if (x >= 0 && x <= sideBarWidth) {
                hovered = values.get(0);
            }

            setToolTipText(null);
            if (hovered != null) {
                Point screenLocation = e.getLocationOnScreen();

                // Only update popup if values or location changed
                if (!values.equals(currentPopupValues) || !screenLocation.equals(currentPopupLocation)) {
                    showHoverPopup(values, screenLocation);
                    currentPopupValues = new ArrayList<>(values);
                    currentPopupLocation = screenLocation;
                }
            } else {
                hideHoverPopup();
                currentPopupValues = null;
                currentPopupLocation = null;
            }

        } catch (Exception ex) {
            setToolTipText(null);
            hideHoverPopup();
            currentPopupValues = null;
            currentPopupLocation = null;
        }
    }

    private void showHoverPopup(List<ReviewValue> values, Point locationOnScreen) {
        if (hoverPopup != null && hoverPopup.isVisible()) {
            if (lastHoverPoint != null && lastHoverPoint.equals(locationOnScreen)) {
                return;
            }
            hoverPopup.dispose();
            hoverPopup = null;
        }
        lastHoverPoint = locationOnScreen;
        hoverPopup = new ReviewFloatingWindow(this, values);
        hoverPopup.show(new Point(locationOnScreen.x + sideBarWidth, locationOnScreen.y));
    }

    private void hideHoverPopup() {
        if (hoverPopup != null) {
            hoverPopup.dispose();
            hoverPopup = null;
            lastHoverPoint = null;
            currentPopupValues = null;
            currentPopupLocation = null;
        }
    }


    private static class ReviewBar {

        Color color;
        int y;
        int height;

        public ReviewBar(Color color, int y, int height) {
            this.color = color;
            this.y = y;
            this.height = height;
        }
    }

    private int getStartIndex(int currentIndex, List<ReviewProvider> providers) {
        int start = currentIndex;
        for (ReviewProvider provider : providers) {
            int startIndex = provider.getStartIndex(document, start);
            if (startIndex < 0) {
                LOGGER.log(Level.WARNING, "start index: {0}, it must be 0 or greater.", startIndex); // NOI18N
                startIndex = 0;
            }
            start = Math.min(start, startIndex);
        }
        return start;
    }

    private List<ReviewProvider> getEnabledProviders() {
        Collection<? extends ReviewProvider> allProviders = Lookup.getDefault().lookupAll(ReviewProvider.class);
        List<ReviewProvider> providers = new ArrayList<>();
        for (ReviewProvider provider : allProviders) {
            if (provider.isProviderEnabled(document)) {
                providers.add(provider);
            }
        }
        return providers;
    }

    private Map<ReviewProvider, Map<String, List<ReviewValue>>> createVariableReviewValuesMap(List<ReviewProvider> providers) {
        Map<ReviewProvider, Map<String, List<ReviewValue>>> variableReviewValues = new HashMap<>();
        providers.forEach(provider -> variableReviewValues.put(provider, new HashMap<>()));
        return variableReviewValues;
    }

    private int getPosFromY(JTextComponent component, @NonNull TextUI textUI, int y) throws BadLocationException {
        if (textUI instanceof BaseTextUI) {
            return ((BaseTextUI) textUI).getPosFromY(y);
        } else {
            return textUI.modelToView(component, textUI.viewToModel(component, new Point(0, y))).y;
        }
    }

    private Color backgroundColor() {
        return backgroundColor;
    }

    /**
     * Update colors.
     */
    public void updateColors() {
        EditorUI editorUI = Utilities.getEditorUI(textComponent);
        if (editorUI == null) {
            return;
        }
        String mimeType = NbEditorUtilities.getMimeType(textComponent);
        FontColorSettings fontColorSettings = MimeLookup.getLookup(MimePath.get(mimeType)).lookup(FontColorSettings.class);
        Coloring lineColoring = Coloring.fromAttributeSet(fontColorSettings.getFontColors(FontColorNames.LINE_NUMBER_COLORING));
        Coloring defaultColoring = Coloring.fromAttributeSet(fontColorSettings.getFontColors(FontColorNames.DEFAULT_COLORING));

        if (lineColoring == null) {
            return;
        }

        final Color backColor = lineColoring.getBackColor();
        if (org.openide.util.Utilities.isMac()) {
            backgroundColor = backColor;
        } else {
            backgroundColor = UIManager.getColor("NbEditorGlyphGutter.background"); //NOI18N
        }
        if (null == backgroundColor) {
            if (backColor != null) {
                backgroundColor = backColor;
            } else {
                backgroundColor = defaultColoring.getBackColor();
            }
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Mutex.EVENT.readAccess(() -> revalidate());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void foldHierarchyChanged(FoldHierarchyEvent e) {
        Mutex.EVENT.readAccess(() -> {
            repaint(textComponent.getVisibleRect());
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }
        });
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        refresh();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        refresh();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        refresh();
    }

    private void refresh() {
        SwingUtilities.invokeLater(() -> repaint());
    }
}
