package dev.wuason.storagemechanic.actions.functions.functions;


import dev.wuason.libs.bsh.EvalError;
import dev.wuason.storagemechanic.Debug;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.config.ActionConfig;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class JavaCode extends Function {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("CODE");
        add("IMPORTS");
    }};
    public JavaCode() {
        super("JAVACODE",ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        String code = (String) objects[0];
        String iList = (String) objects[1];
        String importLine = "";
        ActionConfig actionConfig = action.getActionConfig();
        if(iList != null){
            List<String> importsList = ActionConfigUtils.getArrayListFromArg(iList);
            importLine = ActionConfigUtils.getImportsLine(importsList);
        }
        try {
            String codeLine = actionConfig.getImportsLine() + importLine + code;
            Debug.debugToPlayers(codeLine);
            action.getInterpreter().eval(codeLine);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }

    }
}
