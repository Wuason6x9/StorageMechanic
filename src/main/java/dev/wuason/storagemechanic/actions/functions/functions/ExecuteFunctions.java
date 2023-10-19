package dev.wuason.storagemechanic.actions.functions.functions;

import dev.wuason.bsh.EvalError;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.args.Arg;
import dev.wuason.storagemechanic.actions.config.ActionConfig;
import dev.wuason.storagemechanic.actions.config.ConditionConfig;
import dev.wuason.storagemechanic.actions.config.FunctionConfig;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.actions.functions.Functions;
import dev.wuason.storagemechanic.actions.types.ArgType;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;
import dev.wuason.storagemechanic.utils.ActionsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExecuteFunctions extends Function {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("functions".toUpperCase(Locale.ENGLISH).intern());
        add("conditions".toUpperCase(Locale.ENGLISH).intern());
        add("async".toUpperCase(Locale.ENGLISH).intern());
    }};

    public ExecuteFunctions() {
        super("executeFunctions".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        ArrayList<String> listStringFunctions = ActionConfigUtils.getArrayListFromArg((String)objects[0]);
        ArrayList<String> listStringConditions = new ArrayList<>();
        if(objects[1] != null){
            listStringConditions.addAll(ActionConfigUtils.getArrayListFromArg((String)objects[1]));
        }
        Object asyncStr = objects[2];
        Object asyncObj = false;
        if(asyncStr != null && Utils.isBool((String) asyncStr)){
            asyncObj = Boolean.parseBoolean((String) asyncStr);
        }
        if((boolean)asyncObj){
            Bukkit.getScheduler().runTaskAsynchronously(StorageMechanic.getInstance(), () -> {
                run(listStringConditions,listStringFunctions,action,player);
            });
            return;
        }
        run(listStringConditions,listStringFunctions,action,player);
    }

    public void run(ArrayList<String> listStringConditions, ArrayList<String> listStringFunctions, Action action, Player player){
        ArrayList<FunctionConfig> listFunctionsConfig = new ArrayList<>();
        for(String f : listStringFunctions){
            FunctionConfig functionConfig = ActionConfigUtils.getFunction(f);
            if(functionConfig == null){
                AdventureUtils.sendMessagePluginConsole(StorageMechanic.getInstance(), "<red>Error executing function! " + "Function_line: " + f);
                AdventureUtils.sendMessagePluginConsole(StorageMechanic.getInstance(), "<red>Error: Function is null or invalid!");
                continue;
            }
            listFunctionsConfig.add(functionConfig);
        }
        ArrayList<ConditionConfig> listConditionsConfig = new ArrayList<>();
        for(String c : listStringConditions){
            ConditionConfig conditionConfig = ActionConfigUtils.getCondition(c);
            if(conditionConfig == null){
                AdventureUtils.sendMessagePluginConsole(StorageMechanic.getInstance(), "<red>Error executing function! " + "Condition_line: " + c);
                AdventureUtils.sendMessagePluginConsole(StorageMechanic.getInstance(), "<red>Error: Condition is null or invalid!");
                continue;
            }
            listConditionsConfig.add(conditionConfig);
        }
        try {
            if(checkConditions(action,listConditionsConfig)){
                for(FunctionConfig functionConfigEntry : listFunctionsConfig){
                    Function function = Functions.functionHashMap.get(functionConfigEntry.getFunction().trim().toUpperCase(Locale.ENGLISH));
                    HashMap<String,Object> argsComputed = new HashMap<>();
                    for(Map.Entry<String, String> args : functionConfigEntry.getArgs().entrySet()){
                        if(function.getId().equals("executeFunctions".toUpperCase(Locale.ENGLISH).intern())) break;
                        Object objComputed = ActionsUtils.processArg(args.getValue(), action);
                        if(!args.getKey().equals("CODE")){
                            objComputed = ActionsUtils.processArgSearchArg(null,(String) objComputed, action);
                        }
                        argsComputed.put(args.getKey(), objComputed);
                    }
                    Object[] argsOrdered = ActionsUtils.orderArgumentsAndGet(argsComputed,function);
                    function.execute(action,player,argsOrdered);
                }
            }
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkConditions(Action action, ArrayList<ConditionConfig> listConditionsConfig) throws EvalError {
        boolean result = true;
        for(ConditionConfig c : listConditionsConfig){
            ArrayList<String> placeholdersRemove = new ArrayList<>();
            for(Map.Entry<String,String> entry : c.getReplacements().entrySet()){
                String content = ((String) ActionsUtils.processArg(entry.getValue(),action));
                String[] argStringRaw = ActionConfigUtils.getArg(entry.getValue());
                content = ((String) ActionsUtils.processArgSearchArg(ArgType.valueOf(argStringRaw[0]), content, action));
                String[] argString = ActionConfigUtils.getArg(content);
                Arg arg = ActionConfigUtils.getArg(ArgType.valueOf(argString[0].toUpperCase(Locale.ENGLISH)), argString[1]);
                Object obj = arg.getObject(action);
                action.getInterpreter().set(entry.getKey().intern(), obj);
                placeholdersRemove.add(entry.getKey().intern());
            }
            result = (boolean) action.getInterpreter().eval(c.getReplacement());
            for(String p : placeholdersRemove){
                action.getInterpreter().unset(p);
            }
            if(!result) break;
        }
        return result;
    }
}
