package dev.wuason.storagemechanic.actions.config;

import java.util.HashMap;

public class ConditionConfig {
    private HashMap<String, String> replacements;
    private String line;
    private String replacement;

    public ConditionConfig(HashMap<String, String> replacements, String line, String replacement) {
        this.replacements = replacements;
        this.line = line;
        this.replacement = replacement;
    }

    public HashMap<String, String> getReplacements() {
        return replacements;
    }

    public String getLine() {
        return line;
    }

    public String getReplacement() {
        return replacement;
    }
}
