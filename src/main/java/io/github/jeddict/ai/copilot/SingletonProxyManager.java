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

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.Timer;
import java.util.TimerTask;

public class SingletonProxyManager {

    private FileLock lock;
    private FileChannel channel;
    private Path lockFile;
    private Timer retryTimer;

    public SingletonProxyManager() {
        String tmp = System.getProperty("java.io.tmpdir");
        lockFile = Paths.get(tmp, "copilot-proxy.lock");
    }

    public boolean tryAcquireLock() {
        try {
            channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            lock = channel.tryLock();
            return lock != null;
        } catch (IOException ex) {
            return false;
        }
    }

    public void releaseLock() {
        try {
            if (lock != null && lock.isValid()) {
                lock.release();
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            Files.deleteIfExists(lockFile);
        } catch (IOException ex) {
            // Ignorovať
        }
    }

    public void waitForLockAndRun(Runnable onLockAcquired) {
        if (tryAcquireLock()) {
            onLockAcquired.run();
        } else {
            retryTimer = new Timer(true);
            retryTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (tryAcquireLock()) {
                        retryTimer.cancel();
                        onLockAcquired.run();
                    }
                }
            }, 2000, 2000); // každé 2 sekundy skúša získať lock
        }
    }
}
