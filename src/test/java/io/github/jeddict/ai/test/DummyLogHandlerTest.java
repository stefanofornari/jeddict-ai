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
package io.github.jeddict.ai.test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.assertj.core.api.BDDAssertions.fail;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.Test;

/**
*
*/
public class DummyLogHandlerTest {

    static final private LogRecord LOG1 = new LogRecord(Level.INFO, "first"),
                                   LOG2 = new LogRecord(Level.SEVERE, "second"),
                                   LOG3 = new LogRecord(Level.FINE, "third");

    @Test
    public void construction_and_anitialization() {
        DummyLogHandler h = new DummyLogHandler();

        then(h.getRecords()).isEmpty();
    }

    @Test
    public void add_records() {
        DummyLogHandler h = new DummyLogHandler();
        List<LogRecord> records = h.getRecords();
        then(records).isEmpty();
        h.publish(LOG1); then(records.get(0)).isSameAs(LOG1);
        h.publish(LOG2); then(records.get(1)).isSameAs(LOG2);
        h.publish(LOG3); then(records.get(2)).isSameAs(LOG3);
    }

    @Test
    public void publish_argument() {
        DummyLogHandler h = new DummyLogHandler();

        try {
            h.publish(null);
            fail("missing null value check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("record").hasMessageContaining("cannot be null");
        }
    }

    @Test
    public void get_message() {
        DummyLogHandler h = new DummyLogHandler();

        try {
            h.getMessage(-1);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("index cannot be < 0 or >");
        }

        try {
            h.getMessage(0);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("index cannot be < 0 or >");
        }

        h.publish(LOG1);
        h.publish(LOG2);
        h.publish(LOG3);

        then(h.getMessages()).containsSequence(
            LOG1.getMessage(), LOG2.getMessage(), LOG3.getMessage()
        );

        try {
            h.getMessage(3);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("index cannot be < 0 or >");
        }
    }

    @Test
    public void size() {
         DummyLogHandler h = new DummyLogHandler();

         then(h.size()).isZero();
         h.publish(LOG1); then(h.size()).isEqualTo(1);
         h.publish(LOG2); then(h.size()).isEqualTo(2);
         h.publish(LOG3); then(h.size()).isEqualTo(3);
    }

    @Test
    public void get_messages() {
        DummyLogHandler h = new DummyLogHandler();

        List<String> messages = h.getMessages();
        then(messages).isNotNull();
        then(messages).isEmpty();

        h.publish(LOG1); messages = h.getMessages();
        then(messages).containsSequence(LOG1.getMessage());

        h.publish(LOG2);messages = h.getMessages();
        then(messages).containsSequence(LOG1.getMessage(), LOG2.getMessage());

        h.publish(LOG3); messages = h.getMessages();
        then(messages).containsSequence(LOG1.getMessage(), LOG2.getMessage(), LOG3.getMessage());
    }

    @Test
    public void get_messages_at_a_given_level() {
        DummyLogHandler h = new DummyLogHandler();

        //
        // Initially the loger shall be empty
        //
        List<String> messages = h.getMessages(Level.ALL);
        then(messages).isNotNull();
        then(messages).isEmpty();


        h.publish(LOG1); h.publish(LOG2); h.publish(LOG3);

        //
        // Use Level.ALL for all messages at any level.
        //
        then(h.getMessages(Level.ALL)).hasSize(3)
                      .containsSequence(LOG1.getMessage(), LOG2.getMessage(), LOG3.getMessage());

        //
        // Otherwise return only the messages at the given level
        //
        then(h.getMessages(Level.INFO)).hasSize(1).containsExactly(LOG1.getMessage());
        then(h.getMessages(Level.SEVERE)).hasSize(1).containsExactly(LOG2.getMessage());
        then(h.getMessages(Level.FINE)).hasSize(1).containsExactly(LOG3.getMessage());
    }

    @Test
    public void flush() {
       DummyLogHandler h = new DummyLogHandler();

       h.flush(); then(h.size()).isZero();
       h.publish(LOG1); h.publish(LOG2); h.publish(LOG3);
       h.flush(); then(h.size()).isZero();
    }

    @Test
    /**
     * records shall be discarded if the logger level is lesser then the
     * record level.
     */
    public void add_records_only_accordingly_to_log_level() {
        DummyLogHandler h = new DummyLogHandler();
        h.setLevel(Level.INFO);

        h.publish(LOG1); then(h.size()).isEqualTo(1);
        h.publish(LOG3); then(h.size()).isEqualTo(1); // not logged
        h.publish(LOG2); then(h.size()).isEqualTo(2);

        h.flush(); h.setLevel(Level.ALL);
        h.publish(LOG1); then(h.size()).isEqualTo(1);
        h.publish(LOG3); then(h.size()).isEqualTo(2);
        h.publish(LOG2); then(h.size()).isEqualTo(3);

        h.flush(); h.setLevel(Level.OFF);
        h.publish(LOG1); then(h.size()).isZero();
        h.publish(LOG3); then(h.size()).isZero();
        h.publish(LOG2); then(h.size()).isZero();

        h.flush(); h.setLevel(Level.SEVERE);
        h.publish(LOG1); then(h.size()).isZero();
        h.publish(LOG3); then(h.size()).isZero();
        h.publish(LOG2); then(h.size()).isEqualTo(1);
    }



}
