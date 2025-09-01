package io.github.jeddict.ai.actions;

import javax.swing.AbstractAction;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.JMenuItem;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;

/**
 * A base class for context-aware actions that are presented as popup menu items.
 * This class provides common functionality for setting the action's name, icon,
 * and enabled state.
 */
public abstract class BaseContextAction extends AbstractAction implements Presenter.Popup {

    @StaticResource
    private static final String ICON = "icons/logo16.png";

    /**
     * Constructs a new BaseContextAction.
     *
     * @param enable true to enable the action, false to disable it.
     */
    public BaseContextAction(final boolean enable) { // Removed name parameter
        setEnabled(enable);

        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON, false));
        putValue("iconBase", ICON);
    }

    /**
     * Returns the popup presenter for this action.
     *
     * @return the JMenuItem that represents this action in a popup menu.
     */
    @Override
    public JMenuItem getPopupPresenter() {
        final JMenuItem item = new JMenuItem(this);
        item.setVisible(enabled);
        return item;
    }
}
