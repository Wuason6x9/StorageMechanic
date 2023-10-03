package dev.wuason.storagemechanic.actions.config;

import dev.wuason.storagemechanic.actions.events.EventEnum;
import dev.wuason.storagemechanic.actions.types.Executator;
import dev.wuason.storagemechanic.actions.types.Run;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;
import dev.wuason.storagemechanic.utils.ActionsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionConfig {
    private EventEnum event;
    private Run run;
    private Executator executator;
    private ArrayList<VarListConfig> varList;
    private ArrayList<VarConfig> vars;
    private ArrayList<FunctionConfig> functions;
    private ArrayList<ConditionConfig> conditions;
    private List<String> importsList;

    public ActionConfig(EventEnum event, Run run, Executator executator, ArrayList<VarListConfig> varList, ArrayList<VarConfig> vars, ArrayList<FunctionConfig> functions, ArrayList<ConditionConfig> conditions, List<String> importsList) {
        this.event = event;
        this.run = run;
        this.executator = executator;
        this.varList = varList;
        this.vars = vars;
        this.functions = functions;
        this.conditions = conditions;
        this.importsList = importsList;
    }

    public EventEnum getEvent() {
        return event;
    }

    public Run getRun() {
        return run;
    }

    public Executator getExecutator() {
        return executator;
    }

    public ArrayList<VarListConfig> getVarList() {
        return varList;
    }

    public ArrayList<VarConfig> getVars() {
        return vars;
    }

    public ArrayList<FunctionConfig> getFunctions() {
        return functions;
    }

    public ArrayList<ConditionConfig> getConditions() {
        return conditions;
    }

    public List<String> getImportsList() {
        return importsList;
    }
    public String getImportsLine(){
        return ActionConfigUtils.getImportsLine(getImportsList());
    }
}
