package dev.wuason.storagemechanic.actions.types;

import dev.wuason.storagemechanic.actions.args.*;

public enum ArgType {
    JAVA(JavaArg.class),
    INTERNAL(InternalArg.class),
    TEXT(TextArg.class),
    NUMBER(NumberArg.class),
    EVENT(EventArg.class),
    ADAPTER(AdapterArg.class),
    VAR(VarArg.class),
    INVENTORY(InventoryArg.class),
    FLOAT(FloatArg.class),
    BOOLEAN(BooleanArg.class)
    ;

    private Class<?> argClass;

    private ArgType(Class<?> argClass) {
        this.argClass = argClass;
    }

    public Class<?> getArgClass() {
        return argClass;
    }

    public static ArgType getTypeByClass(Class<?> classType){
        for(ArgType type : ArgType.values()){
            if(classType.equals(type.getArgClass())) return type;
        }
        return null;
    }
}
