package com.interview_task.graal_js_api.controller;

import com.interview_task.graal_js_api.model.Script;
import com.interview_task.graal_js_api.service.ScriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scripts")
public class ScriptController {

    // Reference to the ScriptService for script execution and management
    private final ScriptService scriptService;

    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    /**
     * REST API endpoint to execute a provided JavaScript code snippet.
     *
     * @param code The JavaScript code to execute (provided in request body).
     * @param blocking A boolean parameter (default false) indicating whether to wait for script execution to finish or return immediately.
     * @return A ResponseEntity object containing the executed Script object with details like status, output, and error.
     */
    @PostMapping
    public ResponseEntity<Script> executeScript(@RequestBody String code, @RequestParam(defaultValue = "false") boolean blocking) {
        Script script = scriptService.executeScript(code, blocking);
        return ResponseEntity.ok(script);
    }

    /**
     * REST API endpoint to retrieve a list of all scripts or filtered by an optional status parameter.
     *
     * @param status An optional query parameter for filtering scripts by status (e.g., "QUEUED", "EXECUTING", "COMPLETED", "FAILED").
     * @return A ResponseEntity object containing a List of Script objects matching the criteria.
     */
    @GetMapping
    public ResponseEntity<List<Script>> listScripts(@RequestParam(required = false) String status) {
        List<Script> scripts = scriptService.listScripts(status);
        return ResponseEntity.ok(scripts);
    }

    /**
     * REST API endpoint to retrieve a specific Script object by its ID.
     *
     * @param id The path variable representing the ID of the script to retrieve.
     * @return A ResponseEntity object containing the Script object with the matching ID, or a NotFound status code if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Script> getScript(@PathVariable String id) {
        Script script = scriptService.getScript(id);
        if (script == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(script);
    }

    /**
     * REST API endpoint to remove a script from the list.
     *
     * @param id The path variable representing the ID of the script to remove.
     * @return A ResponseEntity object with appropriate status code:
     *         - No Content (204) if the script was successfully removed.
     *         - Bad Request (400) if the script removal failed (e.g., script not found or currently executing).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeScript(@PathVariable String id) {
        boolean removed = scriptService.removeScript(id);
        if (!removed) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * REST API endpoint to attempt stopping a running script.
     *
     * @param id The path variable representing the ID of the script to stop.
     * @return A ResponseEntity object with appropriate status code:
     *         - No Content (204) if the script was successfully stopped.
     *         - Bad Request (400) if the script stop failed (e.g., script not found or not currently running).
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopScript(@PathVariable String id) {
        boolean stopped = scriptService.stopScript(id);
        if (!stopped) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }
}
