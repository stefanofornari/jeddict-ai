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
package io.github.jeddict.ai.agent.pair;

import java.util.logging.Logger;


/**
 * This is just a marker for PairProgrammer agents like JavadocSpecialist,
 * RestSpecialist, etc.
 *
 *
 */
public interface PairProgrammer {

    final Logger LOG = Logger.getLogger(PairProgrammer.class.getCanonicalName());

    public static enum Specialist {
        ADVISOR(CodeAdvisor.class),
        GHOSTWRITER(Ghostwriter.class),
        JAVADOC(JavadocSpecialist.class),
        REFACTOR(RefactorSpecialist.class),
        REST(RestSpecialist.class);

        public final Class specialistClass;

        Specialist(final Class specialist) {
            this.specialistClass = specialist;
        }
    }

}
