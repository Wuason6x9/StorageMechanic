package dev.wuason.storagemechanic.actions.functions;

import dev.wuason.storagemechanic.actions.functions.functions.*;
import dev.wuason.storagemechanic.actions.functions.functions.inventory.ClearSlotInventory;
import dev.wuason.storagemechanic.actions.functions.functions.inventory.SetItemInventory;
import dev.wuason.storagemechanic.actions.functions.functions.inventory.SetTitleInventory;
import dev.wuason.storagemechanic.actions.functions.functions.storage.ClearSlotPageStorage;
import dev.wuason.storagemechanic.actions.functions.functions.storage.ClearSlotPageStorageR;
import dev.wuason.storagemechanic.actions.functions.functions.storage.SetItemStorage;
import dev.wuason.storagemechanic.actions.functions.functions.storage.SetStage;
import dev.wuason.storagemechanic.actions.functions.functions.vanilla.PlaySound;
import dev.wuason.storagemechanic.actions.functions.functions.vars.RefreshLocalVar;
import dev.wuason.storagemechanic.actions.functions.functions.vars.RemoveGlobalVar;
import dev.wuason.storagemechanic.actions.functions.functions.vars.SetValueGlobalVar;
import dev.wuason.storagemechanic.actions.functions.functions.vars.SetValueVarLocal;

import java.util.HashMap;

public class Functions {
    public final static HashMap<String,Function> functionHashMap = new HashMap<>();
    public final static JavaCode JAVA_CODE_FUNCTION = new JavaCode();
    public final static SetTitleInventory SET_TITLE_INVENTORY_FUNCTION = new SetTitleInventory();
    public final static ExecuteCommand EXECUTE_COMMAND_FUNCTION = new ExecuteCommand();
    public final static ExecuteFunctions EXECUTE_FUNCTIONS_FUNCTION = new ExecuteFunctions();
    public final static ExecuteAction EXECUTE_ACTION_FUNCTION = new ExecuteAction();
    public final static RemoveGlobalVar REMOVE_GLOBAL_VAR_FUNCTION = new RemoveGlobalVar();
    public final static SetValueGlobalVar SET_VALUE_GLOBAL_VAR_FUNCTION = new SetValueGlobalVar();
    public final static SetValueVarLocal SET_VALUE_VAR_LOCAL_FUNCTION = new SetValueVarLocal();
    public final static PlaySound PLAY_SOUND_FUNCTION = new PlaySound();
    public final static RefreshLocalVar REFRESH_LOCAL_VAR = new RefreshLocalVar();
    public final static SetItemInventory SET_ITEM_INVENTORY = new SetItemInventory();
    public final static SetStage SET_STAGE = new SetStage();
    public final static SetItemStorage SET_ITEM_STORAGE = new SetItemStorage();
    public final static ClearSlotPageStorage CLEAR_SLOT_PAGE_STORAGE = new ClearSlotPageStorage();
    public final static ClearSlotPageStorageR CLEAR_SLOT_PAGE_STORAGE_R = new ClearSlotPageStorageR();
    public final static ClearSlotInventory CLEAR_SLOT_INVENTORY = new ClearSlotInventory();

    static {
        functionHashMap.put(JAVA_CODE_FUNCTION.getId(),JAVA_CODE_FUNCTION);
        functionHashMap.put(SET_TITLE_INVENTORY_FUNCTION.getId(),SET_TITLE_INVENTORY_FUNCTION);
        functionHashMap.put(EXECUTE_COMMAND_FUNCTION.getId(),EXECUTE_COMMAND_FUNCTION);
        functionHashMap.put(EXECUTE_FUNCTIONS_FUNCTION.getId(), EXECUTE_FUNCTIONS_FUNCTION);
        functionHashMap.put(EXECUTE_ACTION_FUNCTION.getId(), EXECUTE_ACTION_FUNCTION);
        functionHashMap.put(REMOVE_GLOBAL_VAR_FUNCTION.getId(), REMOVE_GLOBAL_VAR_FUNCTION);
        functionHashMap.put(SET_VALUE_GLOBAL_VAR_FUNCTION.getId(), SET_VALUE_GLOBAL_VAR_FUNCTION);
        functionHashMap.put(PLAY_SOUND_FUNCTION.getId(), PLAY_SOUND_FUNCTION);
        functionHashMap.put(SET_VALUE_VAR_LOCAL_FUNCTION.getId(), SET_VALUE_VAR_LOCAL_FUNCTION);
        functionHashMap.put(REFRESH_LOCAL_VAR.getId(), REFRESH_LOCAL_VAR);
        functionHashMap.put(SET_ITEM_INVENTORY.getId(), SET_ITEM_INVENTORY);
        functionHashMap.put(SET_STAGE.getId(), SET_STAGE);
        functionHashMap.put(SET_ITEM_STORAGE.getId(), SET_ITEM_STORAGE);
        functionHashMap.put(CLEAR_SLOT_PAGE_STORAGE.getId(), CLEAR_SLOT_PAGE_STORAGE);
        functionHashMap.put(CLEAR_SLOT_PAGE_STORAGE_R.getId(), CLEAR_SLOT_PAGE_STORAGE_R);
        functionHashMap.put(CLEAR_SLOT_INVENTORY.getId(), CLEAR_SLOT_INVENTORY);
    }
}
