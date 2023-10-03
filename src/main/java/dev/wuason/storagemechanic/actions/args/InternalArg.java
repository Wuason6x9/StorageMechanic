package dev.wuason.storagemechanic.actions.args;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.config.InternalConfigContent;
import dev.wuason.storagemechanic.actions.expressions.internal.InternalExpr;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class InternalArg extends Arg{
    private Class<InternalExpr> internalExprClass = InternalExpr.class;
    private InternalConfigContent internalConfigContent;
    public InternalArg(String line) {
        super(line);
        reload();
    }
    @Override
    public void setLine(String line) {
        super.setLine(line);
        reload();
    }

    @Override
    public Object getObject(Action action) {
        try {
            HashMap<String, Object> objectHashMap = getObjectHashMap(action, internalConfigContent);
            Class<?> classMethod = InternalExpr.CLASSES_METHODS_HASHMAP.get(internalConfigContent.getMethod());
            InternalExpr internalExpr = InternalExpr.CLASSES_HASHMAP.get(classMethod).get(internalConfigContent.getMethod());
            Method method = internalExpr.getClass().getMethod(internalExpr.getMethod(), Object[].class);
            Object[] args = orderAndGet(objectHashMap,internalConfigContent.getMethod());
            return method.invoke(internalExpr, new Object[]{ args });
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        internalConfigContent = ActionConfigUtils.getInternalConfigContent(getLine());
    }

    public static HashMap<String, Object> getObjectHashMap(Action action, InternalConfigContent internalConfigContent){
        HashMap<String, Object> objectHashMap = new HashMap<>();
        for(Map.Entry<String, String> argEntry : internalConfigContent.getArgs().entrySet()){

            String firstVar = getFirstVar(argEntry.getValue());
            if(firstVar == null) {
                objectHashMap.put(argEntry.getKey(), argEntry.getValue().trim().intern());
                continue;
            }
            else  {
                Object placeholderObj = action.getPlaceholders().getOrDefault(firstVar,"INVALID");
                objectHashMap.put(argEntry.getKey(), placeholderObj);
                continue;
            }
            //CODIGO PARA EJECUTAR EXPRESSIONES DENTRO DE EXPRESSION
        }
        return objectHashMap;
    }

    public static String getFirstVar(String line){
        int charFirstObj = line.indexOf('$');
        int charLastObj = line.lastIndexOf('$');
        if(charLastObj == -1 || charFirstObj == -1) return null;
        String var = line.substring(charFirstObj,charLastObj + 1);
        return var.trim().intern();
    }

    public static Object[] orderAndGet(HashMap<String, Object> objectHashMap, String method){
        Class<?> classMethod = InternalExpr.CLASSES_METHODS_HASHMAP.get(method);
        InternalExpr internalExpr = InternalExpr.CLASSES_HASHMAP.get(classMethod).get(method);
        ArrayList<String> args = internalExpr.getArgs();
        Object[] argObjReturn = new Object[args.size()];
        for(Map.Entry<String, Object> objectEntry : objectHashMap.entrySet()){
            int locOfObj = args.indexOf(objectEntry.getKey());
            if(locOfObj != -1) argObjReturn[locOfObj] = objectEntry.getValue();
        }
        return argObjReturn;
    }
}
