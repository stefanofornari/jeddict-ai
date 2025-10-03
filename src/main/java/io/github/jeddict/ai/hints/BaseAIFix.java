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
package io.github.jeddict.ai.hints;

import io.github.jeddict.ai.completion.Action;
import io.github.jeddict.ai.lang.JeddictBrain;
import io.github.jeddict.ai.settings.PreferencesManager;
import java.util.List;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.spi.java.hints.JavaFix;

/**
 *
 */
public abstract class BaseAIFix extends JavaFix {

    protected final PreferencesManager pm = PreferencesManager.getInstance();

    protected final Action action;

    public BaseAIFix(final TreePathHandle treePathHandle, final Action action) {
        super(treePathHandle);
        this.action = action;
    }

    protected JeddictBrain newJeddictBrain() {
        return new JeddictBrain(pm.getModelName(), false, List.of());
    }

}
