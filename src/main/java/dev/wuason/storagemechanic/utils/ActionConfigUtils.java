package dev.wuason.storagemechanic.utils;

import dev.wuason.storagemechanic.actions.args.Arg;
import dev.wuason.storagemechanic.actions.config.*;
import dev.wuason.storagemechanic.actions.types.ArgType;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ActionConfigUtils {
    public static VarConfig getVar(String str){
        int indexOf = str.indexOf("|");
        if(indexOf == -1) return null;
        String varContent = str.substring(indexOf + 1);
        String var = str.substring(0,indexOf).replace(" ", "").toUpperCase(Locale.ENGLISH);
        if(var == "" || varContent == "") return null;
        int resultChar = varContent.indexOf("=");
        String argTypeStr = varContent.substring(0, resultChar).replace(" ", "").toUpperCase(Locale.ENGLISH);
        String content = varContent.substring(resultChar + 1 );
        ArgType argType = null;
        try {
            argType = ArgType.valueOf(argTypeStr);
        }
        catch (Exception e){
            return null;
        }
        VarConfig varConfig = new VarConfig(var,content,argType);
        return varConfig;
    }
    public static FunctionConfig getFunction(String line){
        int indexOfFirst = line.indexOf("{");
        int indexOfLast = line.lastIndexOf("}");
        if(indexOfFirst == -1 || indexOfLast == -1) return null;
        String argsLine = line.substring(indexOfFirst + 1, indexOfLast);
        HashMap<String,String> args = getArgs(argsLine);
        String action = line.substring(0, indexOfFirst).replace(" ", "").toUpperCase(Locale.ENGLISH);
        if(action.equals("")) return null;
        FunctionConfig functionConfig = new FunctionConfig(action,args);
        return functionConfig;
    }



    public static ArrayList<String> getArrayListFromArg(String argContent) {
        ArrayList<String> list = new ArrayList<>();
        String input = argContent.replace("[", "").replace("]", "").trim();
        String[] segments = input.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)(?![^\\{]*\\})");
        for(String segment : segments) {
            if (!segment.trim().equals("")) {
                list.add(segment.trim());
            }
        }
        return list;
    }


    public static ConditionConfig getCondition(String line){
        String replacementPlaceHolder = "$%$";
        String replacement = line;
        int currentCount = 1;
        int charFirstResult = -1;
        int charLastResult = -1;
        boolean a = false;
        if(!line.contains("{") || !line.contains("}") || line.trim().equals("")) return null;
        HashMap<String, String> replacements = new HashMap<>();
        while(!a){
            charFirstResult = line.indexOf("{", charFirstResult + 1);
            charLastResult = line.indexOf("}", charLastResult + 1);
            if(charFirstResult == -1 || charLastResult == -1) break;
            String placeHolder = replacementPlaceHolder.replace("%","" + currentCount);
            replacements.put(placeHolder, line.substring(charFirstResult + 1,charLastResult));
            //list.add(line.substring(charFirstResult,charLastResult + 1));
            replacement = replacement.replace(line.substring(charFirstResult,charLastResult + 1),placeHolder);
            currentCount++;
        }
        ConditionConfig conditionConfig = new ConditionConfig(replacements,line,replacement);
        return conditionConfig;
    }

    public static String getImportsLine(List<String> imports){
        String line = "";
        for(String i : imports){
            line = line + "import " + i.trim() + ";";
        }
        return line;
    }

    public static Arg getArg(ArgType argType, String content){
        Arg arg = null;
        try {
            arg = (Arg) argType.getArgClass().getConstructors()[0].newInstance(content);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return arg;
    }
    public static InternalConfigContent getInternalConfigContent(String line){
        int firstChar = line.indexOf("(");
        int lastChar = line.lastIndexOf(")");
        String method = line.substring(0,firstChar).trim().toUpperCase(Locale.ENGLISH).intern();
        String content = line.substring(firstChar+1,lastChar);
        return new InternalConfigContent(method,getArgs(content));
    }

    public static HashMap<String,String> getArgs(String content){
        HashMap<String,String> hashMap = new HashMap<>();
        String[] s = content.split(" /(?![^\\[\\]]*\\])(?![^{}]*\\})(?![^()]*\\))");
        for(String split : s){
            int charResult = split.indexOf("=");
            String keyArg = split.substring(0,charResult).replaceAll("\\s*","").toUpperCase(Locale.ENGLISH).intern();
            String valueArg = split.substring(charResult + 1).trim();
            hashMap.put(keyArg,valueArg);
        }
        return hashMap;
    }
    public static String[] getArg(String content){
        String[] arg = new String[2];
        int charResult = content.indexOf("=");
        arg[0] = content.substring(0,charResult).replaceAll("\\s*","").toUpperCase(Locale.ENGLISH).intern();
        arg[1] = content.substring(charResult + 1).trim();
        return arg;
    }
}
