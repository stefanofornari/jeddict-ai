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
package io.github.jeddict.ai.agent.tools.build;

import io.github.jeddict.ai.agent.tools.BaseTest;
import org.assertj.core.api.BDDAssertions;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class BaseBuildToolTest extends BaseTest {
    @Test
    public void config_command() {
        then(new BaseBuildTool(projectDir, "first command").command).isEqualTo("first command");
        then(new BaseBuildTool(projectDir, "second command").command).isEqualTo("second command");
    }

    @Test
    public void constructor_sanity_check() {
        for (String S: new String[] { null, " ", "\n ", "   \t" }) {
            BDDAssertions.thenThrownBy(() -> {
                new BaseBuildTool(projectDir, S);
            }).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command can not be null or blank");
        }
    }
}
