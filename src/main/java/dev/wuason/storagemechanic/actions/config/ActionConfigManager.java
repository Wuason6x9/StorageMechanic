package dev.wuason.storagemechanic.actions.config;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.events.EventEnum;
import dev.wuason.storagemechanic.actions.functions.Functions;
import dev.wuason.storagemechanic.actions.types.ArgType;
import dev.wuason.storagemechanic.actions.types.Executator;
import dev.wuason.storagemechanic.actions.types.Run;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ActionConfigManager {

    private HashMap<String, ActionConfig> actionConfigHashMap = new HashMap<>();
    private HashMap<EventEnum, Set<String>> eventsConfig = new HashMap<>();
    private StorageMechanic core;

    public ActionConfigManager(StorageMechanic core) {
        this.core = core;
    }

    public void loadEvents(){
        eventsConfig = new HashMap<>();
        for(EventEnum eventEnum : EventEnum.values()){
            eventsConfig.put(eventEnum, new HashSet<>());
        }
    }

    public void loadActions(){
        loadEvents();
        actionConfigHashMap = new HashMap<>();

        File base = new File(Mechanics.getInstance().getManager().getMechanicsManager().getMechanic(core).getDirConfig().getPath() + "/Actions/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionActions = config.getConfigurationSection("actions");

            if(sectionActions != null){
                for(Object key : sectionActions.getKeys(false).toArray()){

                    ConfigurationSection actionSection = sectionActions.getConfigurationSection((String)key);
                    if(actionSection == null) continue;
                    // EVENT CONFIG
                    String eventStr = actionSection.getString("event", ".").toUpperCase(Locale.ENGLISH);
                    EventEnum event = null;
                    if(eventStr != null && !eventStr.equals(".")){
                        try {
                            event = EventEnum.valueOf(eventStr);
                            eventsConfig.get(event).add((String)key);
                        }
                        catch (Exception e){
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Action Config! action_id: " + key +  " in file: " + file.getName());
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Event is invalid");
                            continue;
                        }
                    }
                    //RUN CONFIG
                    String runStr = actionSection.getString("run", "sync");
                    Run run = null;
                    try {
                        run = Run.valueOf(runStr);
                    }
                    catch (Exception e){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Action Config! action_id: " + key +  " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: run method is invalid");
                        continue;
                    }
                    //EXECUTATOR CONFIG
                    String executatorStr = actionSection.getString("execute_as", "storage").toUpperCase(Locale.ENGLISH);
                    Executator executator = null;
                    try {
                        executator = Executator.valueOf(executatorStr);
                    }
                    catch (Exception e){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Action Config! action_id: " + key +  " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Executator method is invalid");
                        continue;
                    }
                    //VAR LIST CONFIG
                    ArrayList<VarListConfig> varListComputed = new ArrayList<>();
                    ConfigurationSection varsList = actionSection.getConfigurationSection("vars_list");
                    if(varsList != null){

                        for(String varListkey : varsList.getKeys(false)){

                            ConfigurationSection varList = varsList.getConfigurationSection(varListkey);

                            String var = varList.getString("var");
                            if(var == null){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Var List Config! var_list_id: " + varListkey + " action_id: " + key +  " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Var is null or invalid!");
                                continue;
                            }
                            ArgType argType = null;
                            try {
                                argType = ArgType.valueOf(varList.getString("type").toUpperCase(Locale.ENGLISH));
                            }
                            catch (Exception e){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Var List Config! var_list_id: " + varListkey + " action_id: " + key +  " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Type is null or invalid!");
                                continue;
                            }
                            List<String> objList = varList.getStringList("list");
                            if(objList == null){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Var List Config! var_list_id: " + varListkey + " action_id: " + key +  " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: List is null or invalid!");
                                continue;
                            }

                            VarListConfig varListConfig = new VarListConfig(varListkey,argType,var,objList);
                            varListComputed.add(varListConfig);
                        }

                    }
                    //VARS CONFIG
                    ArrayList<VarConfig> varsComputed = new ArrayList<>();

                    List<String> vars = actionSection.getStringList("vars");
                    if(vars != null){
                        for(String v : vars){
                            VarConfig varConfig = ActionConfigUtils.getVar(v);
                            if(varConfig==null){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Var Config! var: " + v + " action_id: " + key +  " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Var is null or invalid!");
                                continue;
                            }
                            varsComputed.add(varConfig);
                        }
                    }
                    //FUNCTIONS CONFIG
                    ArrayList<FunctionConfig> functionsComputed = new ArrayList<>();
                    List<String> functions = actionSection.getStringList("functions");
                    if(functions != null){
                        for(String f : functions){
                            FunctionConfig functionConfig = ActionConfigUtils.getFunction(f);
                            if(functionConfig == null || !Functions.functionHashMap.containsKey(functionConfig.getFunction())){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Function Config! function: " + f + " action_id: " + key +  " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Function is null or invalid!");
                                continue;
                            }
                            functionsComputed.add(functionConfig);
                        }
                    }
                    //CONDITIONS CONFIG
                    ArrayList<ConditionConfig> conditionsList = new ArrayList<>();
                    List<String> conditions = actionSection.getStringList("conditions");
                    if(conditions != null){
                        for(String c : conditions){
                            ConditionConfig conditionConfig = ActionConfigUtils.getCondition(c);
                            if(conditionConfig == null){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Function Config! condition: " + c + " action_id: " + key +  " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Condition is null or invalid!");
                                continue;
                            }
                            conditionsList.add(conditionConfig);
                        }
                    }
                    List<String> importsList = actionSection.getStringList("java_imports");
                    if(importsList == null) importsList = new ArrayList<>();
                    ActionConfig actionConfig = new ActionConfig(event,run,executator,varListComputed,varsComputed,functionsComputed,conditionsList,importsList);
                    actionConfigHashMap.put((String)key,actionConfig);
                }
            }
        }
        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Actions loaded: <yellow>" + actionConfigHashMap.size());

    }

    public HashMap<EventEnum, Set<String>> getEventsConfig() {
        return eventsConfig;
    }

    public HashMap<String, ActionConfig> getActionConfigHashMap() {
        return actionConfigHashMap;
    }
}
