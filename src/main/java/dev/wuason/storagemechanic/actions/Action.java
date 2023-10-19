package dev.wuason.storagemechanic.actions;

import dev.wuason.bsh.EvalError;
import dev.wuason.bsh.Interpreter;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.args.Arg;
import dev.wuason.storagemechanic.actions.config.*;
import dev.wuason.storagemechanic.actions.events.EventAction;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.actions.functions.Functions;
import dev.wuason.storagemechanic.actions.functions.functions.ExecuteFunctions;
import dev.wuason.storagemechanic.actions.types.ArgType;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;
import dev.wuason.storagemechanic.utils.ActionsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Action {
    private String id;
    private String actionConfigId;
    private Interpreter interpreter;
    private HashMap<String, Object> placeholders = new HashMap<>();
    private ArrayList<String> placeholdersRegistered = new ArrayList<>();
    private StorageMechanic core;
    private ActionManager actionManager;
    private boolean active = false;
    private Player player;
    private EventAction eventAction;

    public Action(StorageMechanic core, HashMap<String, Object> initPlaceholders, String actionConfigId, ActionManager actionManager, Player player, EventAction eventAction) {
        this.id = UUID.randomUUID().toString();
        this.core = core;
        this.player = player;
        this.eventAction = eventAction;
        this.actionManager = actionManager;
        placeholders.putAll(initPlaceholders);
        this.actionConfigId = actionConfigId;
        interpreter = new Interpreter(); //BEANSHELL
        try {
            load();
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }
    public void load() throws EvalError {
        ActionConfig actionConfig = getActionConfig();
        for(Map.Entry<String, Object> placeholderEntry : placeholders.entrySet()){
            if(placeholderEntry.getKey().contains("$")) {
                interpreter.set(placeholderEntry.getKey(), placeholderEntry.getValue());
                placeholdersRegistered.add(placeholderEntry.getKey());
            }
        }
        //REGISTER ACTION
        placeholders.put("$ACTION$",this);
        interpreter.set("$ACTION$", this);
        placeholdersRegistered.add("$ACTION$");
        //Register methods internals
        registerMethods();

        //VARLIST
        for(VarListConfig varListEntry : actionConfig.getVarList()){
            List<Object> objList = new ArrayList<>();
            for(Arg arg : varListEntry.getArrayList()){
                Arg argCloned = arg.clone();
                argCloned.setLine((String) ActionsUtils.processArg(argCloned.getLine(),this));
                arg.setLine((String) ActionsUtils.processArgSearchArg(varListEntry.getArgType(),arg.getLine(),this));
                Object obj = argCloned.getObject(this);
                if(obj instanceof ArrayList<?>) {
                    objList.addAll((ArrayList<?>) obj);
                    continue;
                }
                objList.add(obj);
            }
            if(varListEntry.getVar().contains("{") && varListEntry.getVar().contains("}")){
                if(!actionManager.getVarsGlobal().containsKey(varListEntry.getVar().trim().toUpperCase(Locale.ENGLISH).intern())) actionManager.setValueGlobalVar(((Storage)placeholders.get("$STORAGE$")).getId(), varListEntry.getVar().trim().toUpperCase(Locale.ENGLISH).intern(), objList);
                continue;
            }
            placeholders.put(varListEntry.getVar().trim().toUpperCase(Locale.ENGLISH).intern(), objList);
        }
        //VARS
        for(VarConfig varEntry : actionConfig.getVars()){
            //COMPUTE
            Arg arg = varEntry.getArg().clone();

            arg.setLine((String) ActionsUtils.processArg(arg.getLine(),this));
            arg.setLine((String) ActionsUtils.processArgSearchArg(varEntry.getArgType(), arg.getLine(), this));
            Object obj = arg.getObject(this);
            String key = varEntry.getVar().trim().toUpperCase(Locale.ENGLISH);
            if(key.contains("{") && key.contains("}")){
                String id = ((Storage)placeholders.get("$STORAGE$")).getId();
                if(!actionManager.getVarsGlobal().get( id ).containsKey( key )) actionManager.setValueGlobalVar(id, key, obj);
                continue;
            }
            placeholders.put(key.intern(), obj);
        }
    }
    public void execute(){
        ActionConfig actionConfig = getActionConfig();
        switch (actionConfig.getRun()){
            case SYNC -> {
                try {
                    run();
                }
                catch (EvalError e) {
                    throw new RuntimeException(e);
                }
            }
            case ASYNC -> {runAsync();}
        }
        active = true;
    }
    public void runAsync(){
        Bukkit.getScheduler().runTaskAsynchronously(core, (task) -> {
            try {
                run();
            } catch (EvalError e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void run() throws EvalError {
        ActionConfig actionConfig = getActionConfig();
        if(checkConditions(actionConfig)){
            for(FunctionConfig functionConfigEntry : actionConfig.getFunctions()){
                Function function = Functions.functionHashMap.get(functionConfigEntry.getFunction().trim().toUpperCase(Locale.ENGLISH));
                HashMap<String,Object> argsComputed = new HashMap<>();
                for(Map.Entry<String, String> args : functionConfigEntry.getArgs().entrySet()){
                    if(function.getId().equals("executeFunctions".toUpperCase(Locale.ENGLISH).intern())) break;
                    Object objComputed = ActionsUtils.processArg(args.getValue(), this);
                    if(!args.getKey().equals("CODE")){
                        objComputed = ActionsUtils.processArgSearchArg(null,(String) objComputed, this);
                    }
                    argsComputed.put(args.getKey(),objComputed);
                }
                Object[] argsOrdered = ActionsUtils.orderArgumentsAndGet(argsComputed,function);
                function.execute(this,player,argsOrdered);
            }
        }
        finish();
    }

    public boolean checkConditions(ActionConfig actionConfig) throws EvalError {
        boolean result = true;
        for(ConditionConfig c : actionConfig.getConditions()){
            ArrayList<String> placeholdersRemove = new ArrayList<>();
            for(Map.Entry<String,String> entry : c.getReplacements().entrySet()){
                String[] argStringRaw = ActionConfigUtils.getArg(entry.getValue());
                String content = ((String) ActionsUtils.processArg(entry.getValue(),this));
                content = (String) ActionsUtils.processArgSearchArg(ArgType.valueOf(argStringRaw[0]),content,this);
                String[] argString = ActionConfigUtils.getArg(content);
                Arg arg = ActionConfigUtils.getArg(ArgType.valueOf(argString[0].toUpperCase(Locale.ENGLISH)), argString[1]);
                Object obj = arg.getObject(this);
                interpreter.set(entry.getKey().intern(), obj);
                placeholdersRemove.add(entry.getKey().intern());
            }
            try {
                result = (boolean) interpreter.eval(c.getReplacement());
            } catch (EvalError e) {
                throw new RuntimeException(e);
            }
            for(String p : placeholdersRemove){
                try {
                    getInterpreter().unset(p);
                } catch (EvalError e) {
                    throw new RuntimeException(e);
                }
            }
            if(!result) break;
        }
        return result;
    }

    public void finish(){
        active = false;
        interpreter = null;
        placeholders = null;
        actionManager.getActionsActive().remove(id);
    }


    public void registerMethods() throws EvalError {

        interpreter.eval("import dev.wuason.storagemechanic.utils.ArgUtils; Object runArg(String argType, String value){ return ArgUtils.runArg($ACTION$, argType, value); }");

    }



    public ActionConfig getActionConfig(){
        return core.getManagers().getActionConfigManager().getActionConfigHashMap().get(actionConfigId);
    }

    public String getId() {
        return id;
    }

    public String getActionConfigId() {
        return actionConfigId;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public HashMap<String, Object> getPlaceholders() {
        return placeholders;
    }
    public boolean isActive() {
        return active;
    }

    public EventAction getEventAction() {
        return eventAction;
    }

    public ArrayList<String> getPlaceholdersRegistered() {
        return placeholdersRegistered;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }
}
