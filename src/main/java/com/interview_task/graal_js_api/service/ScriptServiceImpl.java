package com.interview_task.graal_js_api.service;

import com.interview_task.graal_js_api.model.Script;
import org.graalvm.polyglot.Context;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class ScriptServiceImpl implements ScriptService {
    // A thread pool for executing scripts concurrently.
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // List to store all created scripts.
    private final List<Script> scripts = new ArrayList<>();

    // ConcurrentMap to track running scripts with their corresponding Future objects.
    private final ConcurrentMap<String, Future<?>> runningScripts = new ConcurrentHashMap<>();

    /**
     * Executes a provided JavaScript code snippet.
     *
     * @param code The JavaScript code to execute.
     * @param blocking Whether to wait for the script execution to finish or return immediately.
     * @return The executed Script object containing details like status, output, and error.
     */
    @Override
    public Script executeScript(String code, boolean blocking) {
        Script script = new Script(); // Create a new Script object

        // Generate a unique ID for the script
        script.setId(UUID.randomUUID().toString());
        script.setCode(code);
        script.setStatus("QUEUED");
        script.setScheduledTime(LocalDateTime.now());
        scripts.add(script);

        // Submit the script execution as a task to the thread pool
        Future<Void> future = executorService.submit(() -> {
            script.setStatus("EXECUTING");
            script.setExecutionTime(LocalDateTime.now());

            // Capture standard output and error streams
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            try (Context context = Context.newBuilder("js")
                    .out(outputStream)
                    .err(errorStream)
                    .build()) {
                context.eval("js", code);
                script.setOutput(outputStream.toString());
                script.setStatus("COMPLETED");
            } catch (Exception e) {
                script.setError(errorStream.toString());
                script.setStatus("FAILED");
            }

            // Clean up by removing the script from running scripts map after execution
            runningScripts.remove(script.getId());
            return null;
        });

        runningScripts.put(script.getId(), future);

        if (blocking) {
            try {
                future.get(); // Wait for script execution to finish if blocking is requested
            } catch (InterruptedException | ExecutionException e) {
                script.setError(e.getMessage());
                script.setStatus("FAILED");
            }
        }

        return script;
    }

    /**
     * Lists all scripts or filters them by a provided status.
     *
     * @param status The optional status to filter scripts by (e.g., "QUEUED", "EXECUTING", "COMPLETED", "FAILED").
     * @return A list of Script objects matching the criteria.
     */
    @Override
    public List<Script> listScripts(String status) {
        return (status == null) ? scripts : scripts.stream().filter(script -> script.getStatus().equals(status)).toList();
    }

    /**
     * Retrieves a specific Script object by its ID.
     *
     * @param id The ID of the script to retrieve.
     * @return The Script object with the matching ID, or null if not found.
     */
    @Override
    public Script getScript(String id) {
        return scripts.stream().filter(script -> script.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Attempts to stop a running script identified by its ID.
     *
     * @param id The ID of the script to stop.
     * @return True if the script was successfully stopped, false otherwise.
     */
    @Override
    public boolean stopScript(String id) {
        Future<?> future = runningScripts.get(id);
        if (future != null) {
            boolean cancelled = future.cancel(true); // Attempt to cancel the script execution
            if (cancelled) {
                Script script = getScript(id);
                if (script != null) {
                    script.setStatus("STOPPED");
                }
                runningScripts.remove(id);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a script from the list if it's not currently executing.
     *
     * @param id The ID of the script to remove.
     * @return True if the script was successfully removed, false otherwise.
     */
    @Override
    public boolean removeScript(String id) {
        return scripts.removeIf(script -> script.getId().equals(id) && !"EXECUTING".equals(script.getStatus()));
    }
}
