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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 */
public class DummyLogHandler extends Handler {
    /**
     * The list of collected log records
     */
    private final List<LogRecord> records;

    public DummyLogHandler() {
        records = new ArrayList<>();
    }

    /**
     * Returns the registered records
     *
     * @return the registered records
     */
    public synchronized List<LogRecord> getRecords() {
        return records;
    }

    /**
     * Publishes the given log record
     *
     * @param record the log record - NOT NULL
     *
     * @throws IllegalArgumentException if record is null
     */
    @Override
    public synchronized void publish(LogRecord record) throws IllegalArgumentException {
        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");
        }
        if (isLoggable(record)) {
            records.add(record);
        }
    }

    @Override
    public synchronized void flush() {
        records.clear();
    }

    @Override
    public void close() throws SecurityException {
        //
        // Nothing to do
        //
    }

    /**
     * Returns the message given the index
     *
     * @param index the index in the list
     *
     * @return the <i>index</i>th message
     *
     * @throws IllegalArgumentException if index is out of the valid range
     */
    public synchronized String getMessage(int index) throws IllegalArgumentException {
        if ((index < 0) || (index >= records.size())) {
            throw new IllegalArgumentException(
                String.format("index cannot be < 0 or > %d (it was %d)", records.size(), index)
            );
        }
        return records.get(index).getMessage();
    }

    /**
     * Returns the number of records logged
     *
     * @return number of records logged
     */
    public synchronized int size() {
        return records.size();
    }

    /**
     * Returns the record messages as a List<String>
     *
     * @return the record messages as a List<String>
     */
    public synchronized List<String> getMessages() {
        return getMessages(Level.ALL);
    }

    /**
     * Returns the record messages logged at the given level as a List<String>
     *
     * @param level
     * @return the record messages logged at the given level as a List<String>
     */
    public synchronized List<String> getMessages(Level level) {
        List<String> messages = new ArrayList<>();

        for (LogRecord r: records) {
            if ((level == Level.ALL) || (level == r.getLevel())) {
                messages.add(r.getMessage());
            }
        }

        return messages;
    }
}
