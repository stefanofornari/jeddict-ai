package io.github.jeddict.ai.components;

import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class AssistantChatTest {

    @BeforeAll
    public static void initJFX() throws InterruptedException {
        // Initialize JavaFX environment
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Platform already started
            latch.countDown();
        }
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX Platform failed to start");
        }
    }

    @Test
    public void testAssistantChatViewInitialization() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                AssistantChatView view = new AssistantChatView();
                assertNotNull(view);

                view.appendUserMessage("Hello");
                view.appendAssistantMessage("Hi");

                // Verify no exceptions thrown
            } catch (Exception e) {
                fail("Exception during initialization: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    public void testAppendHtmlMessage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                AssistantChatView view = new AssistantChatView();
                view.appendHtmlMessage("<html><body>Test</body></html>", null);
            } catch (Exception e) {
                fail("Exception during HTML message append: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
    }
}
