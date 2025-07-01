/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
