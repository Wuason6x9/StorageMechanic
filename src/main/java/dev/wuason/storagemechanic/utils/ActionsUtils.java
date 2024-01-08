package dev.wuason.storagemechanic.utils;


import dev.wuason.libs.bsh.EvalError;
import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.storagemechanic.Debug;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.args.Arg;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.actions.types.ArgType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionsUtils {
    private static final Pattern PATTERN_PERCENT = Pattern.compile("%(.*?)%");
    private static final Pattern PATTERN_DOLLAR = Pattern.compile("\\$(.*?)\\$");
    private static final Pattern PATTERN_KEY = Pattern.compile("\\{(.*?)\\}");

    public static Object processArg(String arg, Action action) throws EvalError {

        //%%
        Matcher matcher = PATTERN_PERCENT.matcher(arg);
        List<String> placeholdersFound = new ArrayList<>();
        while (matcher.find()) {
            String pl = matcher.group(1);
            if(!placeholdersFound.contains(pl)) placeholdersFound.add(pl);
        }
        for(String p : placeholdersFound){
            String a = "%" + p + "%";
            if(action.getPlaceholders().containsKey(a.toUpperCase(Locale.ENGLISH).trim().intern())){
                Debug.debug(a.toUpperCase(Locale.ENGLISH).trim().intern());
                arg = arg.replace(a,action.getPlaceholders().get(a.toUpperCase(Locale.ENGLISH).trim().intern()).toString());
            }
        }

        //{}
        Matcher matcher2 = PATTERN_KEY.matcher(arg);
        List<String> placeholdersFound2 = new ArrayList<>();
        while (matcher2.find()) {
            String pl = matcher2.group(1);
            if(!placeholdersFound2.contains(pl)) placeholdersFound2.add(pl);
        }
        for(String p : placeholdersFound2){
            String a = "{" + p + "}";
            if(action.getPlaceholders().containsKey(a.toUpperCase(Locale.ENGLISH).trim().intern())){
                Debug.debug(a.toUpperCase(Locale.ENGLISH).trim().intern());
                arg = arg.replace(a, a.toUpperCase(Locale.ENGLISH).trim());
            }
        }

        //$$
        Matcher matcher1 = PATTERN_DOLLAR.matcher(arg);
        List<String> placeholdersFound1 = new ArrayList<>();
        while (matcher1.find()) {
            String pl = matcher1.group(1);
            if(!placeholdersFound1.contains(pl)) placeholdersFound1.add(pl);
        }
        for(String p : placeholdersFound1){
            String a = "$" + p + "$";
            if(action.getPlaceholders().containsKey(a.toUpperCase(Locale.ENGLISH).intern())){
                if(!action.getPlaceholdersRegistered().contains(a.toUpperCase(Locale.ENGLISH).intern())) {
                    action.getInterpreter().set(a.toUpperCase(Locale.ENGLISH).intern(), action.getPlaceholders().get(a.toUpperCase(Locale.ENGLISH).intern()));
                    action.getPlaceholdersRegistered().add(a.toUpperCase(Locale.ENGLISH).intern());
                }
                arg = arg.replace(a,a.toUpperCase(Locale.ENGLISH));
            }
        }

        return arg.trim().intern();
    }

    public static Object processArgSearchArg(ArgType atype, String arg, Action action) throws EvalError {
        if(atype != null && atype.equals(ArgType.JAVA)) return arg;
        List<String> placeholdersFound3 = findNestedPatterns(arg);
        for (String p : placeholdersFound3) {
            if (!p.contains("=")) continue;
            String[] argString = ActionConfigUtils.getArg(p);
            if (!Arrays.stream(ArgType.values()).map(argType -> argType.toString().intern()).toList().contains(argString[0].intern())) continue;
            String a = "<" + p + ">";
            int randomGen = MathUtils.randomNumber(1000, 10000000);
            String varGen = ("$" + randomGen + "$").intern();
            Arg argObj = ActionConfigUtils.getArg(ArgType.valueOf(argString[0].toUpperCase(Locale.ENGLISH)), argString[1]);
            Object obj = argObj.getObject(action);
            action.getInterpreter().set(varGen, obj);
            action.getPlaceholders().put(varGen, obj);
            for(int i=0;i<placeholdersFound3.size();i++){
                if(placeholdersFound3.get(i).contains(a)){
                    placeholdersFound3.set(i, placeholdersFound3.get(i).replace(a, varGen));
                }
            }
            arg = arg.replace(a, varGen);
        }

        return arg;
    }

    public static List<String> findNestedPatterns(String input) {
        List<String> results = new ArrayList<>();
        findNestedPatterns(input, 0, 0, results);
        return results;
    }

    private static void findNestedPatterns(String input, int start, int level, List<String> results) {
        int openCount = 0;
        int lastOpenIndex = -1;
        for (int i = start; i < input.length(); i++) {
            if (input.charAt(i) == '<') {
                openCount++;
                if (openCount == 1) {
                    lastOpenIndex = i;
                }
            } else if (input.charAt(i) == '>') {
                openCount--;
                if (openCount == 0) {
                    String substring = input.substring(lastOpenIndex + 1, i);
                    if (!substring.contains("<")) {
                        results.add(substring);
                    } else {
                        findNestedPatterns(substring, 0, level + 1, results);
                        results.add(substring);
                    }
                }
            }
        }
    }

    public static Object[] orderArgumentsAndGet(HashMap<String,Object> args, Function function){
        Object[] objs = new Object[function.getArgs().size()];
        for(Map.Entry<String,Object> argEntry : args.entrySet()){
            if(function.getArgs().contains(argEntry.getKey())){
                int index = function.getArgs().indexOf(argEntry.getKey());
                if(index != -1) objs[index] = argEntry.getValue();
            }
        }
        return objs;
    }
}
