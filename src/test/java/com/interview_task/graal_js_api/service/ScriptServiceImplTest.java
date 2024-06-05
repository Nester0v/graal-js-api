package com.interview_task.graal_js_api.service;

import com.interview_task.graal_js_api.model.Script;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ScriptServiceImplTest {

    private ScriptServiceImpl scriptService;

    @BeforeEach
    public void setUp() {
        scriptService = new ScriptServiceImpl();
    }

    @Test
    public void testExecuteScriptBlocking() throws ExecutionException, InterruptedException {
        String code = "print('Hello, World!');";
        Script script = scriptService.executeScript(code, true);

        assertNotNull(script.getId());
        assertEquals("COMPLETED", script.getStatus());
        assertEquals("Hello, World!\n", script.getOutput());
        assertNull(script.getError());
    }

    @Test
    public void testExecuteScriptNonBlocking() {
        String code = "while(true){}"; // Infinite loop to test non-blocking execution
        Script script = scriptService.executeScript(code, false);

        assertNotNull(script.getId());
        assertEquals("QUEUED", script.getStatus());

        // Wait for a bit to let the script start executing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Script executingScript = scriptService.getScript(script.getId());
        assertEquals("EXECUTING", executingScript.getStatus());
    }

    @Test
    public void testListScripts() {
        scriptService.executeScript("print('Script 1');", true);
        scriptService.executeScript("print('Script 2');", true);

        List<Script> scripts = scriptService.listScripts(null);
        assertEquals(2, scripts.size());
    }

    @Test
    public void testGetScript() {
        Script script = scriptService.executeScript("print('Find me');", true);

        Script foundScript = scriptService.getScript(script.getId());
        assertNotNull(foundScript);
        assertEquals(script.getId(), foundScript.getId());
    }

    @Test
    public void testStopScript() {
        String code = "while(true){}"; // Infinite loop to test stopping
        Script script = scriptService.executeScript(code, false);

        // Wait for a bit to let the script start executing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean stopped = scriptService.stopScript(script.getId());
        assertTrue(stopped);

        Script stoppedScript = scriptService.getScript(script.getId());
        assertEquals("STOPPED", stoppedScript.getStatus());
    }

    @Test
    public void testRemoveScript() {
        Script script = scriptService.executeScript("print('Removable script');", true);
        String scriptId = script.getId();

        boolean removed = scriptService.removeScript(scriptId);
        assertTrue(removed);

        Script removedScript = scriptService.getScript(scriptId);
        assertNull(removedScript);
    }

    @Test
    public void testRemoveExecutingScript() {
        String code = "while(true){}"; // Infinite loop to test removing executing script
        Script script = scriptService.executeScript(code, false);
        String scriptId = script.getId();

        // Wait a little to let the script start executing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean removed = scriptService.removeScript(scriptId);
        assertFalse(removed);

        Script executingScript = scriptService.getScript(scriptId);
        assertNotNull(executingScript);
    }
}