package com.interview_task.graal_js_api.service;

import com.interview_task.graal_js_api.model.Script;

import java.util.List;

public interface ScriptService {
    Script executeScript(String code, boolean blocking);
    List<Script> listScripts(String status);
    Script getScript(String id);
    boolean stopScript(String id);
    boolean removeScript(String id);
}
