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
package io.github.jeddict.ai.copilot;

import org.openide.modules.OnStop;
import org.openide.util.Lookup;

/**
 *
 * @author arsi arsi(at)arsi.sk
 */
/**
 * This class represents a {@link Runnable} task that handles the shutdown or
 * stopping procedure for the Copilot proxy component. When executed, it
 * retrieves an instance of {@code RunCopilotProxy} via the default
 * {@code Lookup} mechanism and invokes its {@code closeProxy()} method to
 * perform the necessary cleanup or termination operations.
 * <p>
 * The class is intended to be used in contexts where the Copilot proxy needs to
 * be gracefully stopped, such as application shutdown hooks or specific
 * lifecycle events.
 * </p>
 * <p>
 * This class is annotated with {@code @OnStop}, indicating that it is to be
 * triggered during the application's stop or shutdown phase.
 * </p>
 *
 * @see Runnable
 * @see RunCopilotProxy
 * @see Lookup
 */
@OnStop
public class CopilotProxyStop implements Runnable {

    @Override
    public void run() {
        RunCopilotProxy proxy = Lookup.getDefault().lookup(RunCopilotProxy.class);
        proxy.closeProxy();
    }

}
