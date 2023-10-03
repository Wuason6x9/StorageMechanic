package dev.wuason.storagemechanic.actions;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.config.ActionConfig;
import dev.wuason.storagemechanic.actions.config.ActionConfigManager;
import dev.wuason.storagemechanic.actions.events.EventAction;
import dev.wuason.storagemechanic.actions.events.EventEnum;
import dev.wuason.storagemechanic.actions.types.Executator;
import dev.wuason.storagemechanic.actions.vars.GlobalVar;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class ActionManager {
    private HashMap<String,Action> actionsActive = new HashMap<>();
    private HashMap<String, HashMap<String, GlobalVar>> varsGlobal = new HashMap<>();
    private StorageMechanic core;

    public ActionManager(StorageMechanic core) {
        this.core = core;
    }



    public Action createAction(Storage storage, String actionConfigId, Player player, EventAction eventAction){
        ActionConfig actionConfig = core.getManagers().getActionConfigManager().getActionConfigHashMap().get(actionConfigId);
        if(actionConfig == null) return null;
        Executator executator = actionConfig.getExecutator();
        HashMap<String,Object> placeholders = new HashMap<>();
        if(!varsGlobal.containsKey(storage.getId())) varsGlobal.put(storage.getId(),new HashMap<>());
        StorageOriginContext storageOriginContext = storage.getStorageOriginContext();
        if(executator == null) executator = Executator.valueOf(storageOriginContext.getContext().toString());
        switch (executator){
            case BLOCK_STORAGE -> {
                BlockStorage blockStorage = core.getManagers().getBlockStorageManager().getBlockStorage((String)storageOriginContext.getData().get(1));
                placeholders.put("$block_storage$".toUpperCase().intern(), blockStorage);
                placeholders.put("$block_storage_id$".toUpperCase().intern(), blockStorage.getId().intern());
                placeholders.put("$block_storage_config_id$".toUpperCase().intern(), blockStorage.getBlockStorageConfigID().intern());
            }
            case FURNITURE_STORAGE -> {
                FurnitureStorage furnitureStorage = core.getManagers().getFurnitureStorageManager().getFurnitureStorage((String) storageOriginContext.getData().get(1));
                placeholders.put("$furniture_storage$".toUpperCase().intern(), furnitureStorage);
                placeholders.put("$furniture_storage_id$".toUpperCase().intern(), furnitureStorage.getId().intern());
                placeholders.put("$furniture_storage_config_id$".toUpperCase().intern(), furnitureStorage.getFurnitureStorageConfigID().intern());
            }
            case ITEM_STORAGE -> {
                placeholders.put("$item_storage_config_id$".toUpperCase().intern(), ((String) storageOriginContext.getData().get(0)).intern() );
                placeholders.put("$item_storage_owner$".toUpperCase().intern(), Bukkit.getOfflinePlayer(UUID.fromString((String) storageOriginContext.getData().get(1))));
                placeholders.put("$item_storage_owner_name$".toUpperCase().intern(), Bukkit.getOfflinePlayer(UUID.fromString((String) storageOriginContext.getData().get(1))).getName().intern());
            }
        }
        //INIT DEFAULT VARS
        if(storage != null){
            placeholders.put("$storage$".toUpperCase().intern(), storage);
            placeholders.put("$storage_id$".toUpperCase().intern(), storage.getId().intern());
        }
        //INIT DEFAULT VARS
        if(player != null){
            placeholders.put("$player$".toUpperCase().intern(), player);
            placeholders.put("$player_name$".toUpperCase().intern(), player.getName().intern());
        }

        placeholders.put("%slash_char%".toUpperCase().intern(), "/");
        if(eventAction.getEvent() != null){
            placeholders.put("$event$".toUpperCase().intern(), eventAction.getEvent());
        }
        placeholders.put("$event_id$".toUpperCase().intern(), eventAction.getId().intern());
        eventAction.registerPlaceholders(placeholders);
        Action action = new Action(core,placeholders,actionConfigId,this,player, eventAction);
        actionsActive.put(action.getId(),action);
        return action;
    }
    public Action createAction(Storage storage, String actionConfigId, Player player, EventAction eventAction, HashMap<String, Object> placeholdersDef){
        ActionConfig actionConfig = core.getManagers().getActionConfigManager().getActionConfigHashMap().get(actionConfigId);
        Executator executator = actionConfig.getExecutator();
        HashMap<String,Object> placeholders = new HashMap<>();
        if(!varsGlobal.containsKey(storage.getId())) varsGlobal.put(storage.getId(),new HashMap<>());
        placeholders.putAll(placeholdersDef);
        StorageOriginContext storageOriginContext = storage.getStorageOriginContext();
        if(executator == null) executator = Executator.valueOf(storageOriginContext.getContext().toString());
        switch (executator){
            case BLOCK_STORAGE -> {
                BlockStorage blockStorage = core.getManagers().getBlockStorageManager().getBlockStorage((String)storageOriginContext.getData().get(1));
                placeholders.put("$block_storage$".toUpperCase().intern(), blockStorage);
                placeholders.put("$block_storage_id$".toUpperCase().intern(), blockStorage.getId().intern());
                placeholders.put("$block_storage_config_id$".toUpperCase().intern(), blockStorage.getBlockStorageConfigID().intern());
            }
            case FURNITURE_STORAGE -> {
                FurnitureStorage furnitureStorage = core.getManagers().getFurnitureStorageManager().getFurnitureStorage((String) storageOriginContext.getData().get(1));
                placeholders.put("$furniture_storage$".toUpperCase().intern(), furnitureStorage);
                placeholders.put("$furniture_storage_id$".toUpperCase().intern(), furnitureStorage.getId().intern());
                placeholders.put("$furniture_storage_config_id$".toUpperCase().intern(), furnitureStorage.getFurnitureStorageConfigID().intern());
            }
            case ITEM_STORAGE -> {
                placeholders.put("$item_storage_config_id$".toUpperCase().intern(), ((String) storageOriginContext.getData().get(0)).intern() );
                placeholders.put("$item_storage_owner$".toUpperCase().intern(), Bukkit.getOfflinePlayer(UUID.fromString((String) storageOriginContext.getData().get(1))));
                placeholders.put("$item_storage_owner_name$".toUpperCase().intern(), Bukkit.getOfflinePlayer(UUID.fromString((String) storageOriginContext.getData().get(1))).getName().intern());
            }
        }
        if(storage != null){
            placeholders.put("$storage$".toUpperCase().intern(), storage);
            placeholders.put("$storage_id$".toUpperCase().intern(), storage.getId().intern());
        }
        //INIT DEFAULT VARS
        if(player != null){
            placeholders.put("$player$".toUpperCase().intern(), player);
            placeholders.put("$player_name$".toUpperCase().intern(), player.getName().intern());
        }

        placeholders.put("%slash_char%".toUpperCase().intern(), "/");
        if(eventAction.getEvent() != null){
            placeholders.put("$event$".toUpperCase().intern(), eventAction.getEvent());
        }
        placeholders.put("$event_id$".toUpperCase().intern(), eventAction.getId().intern());
        eventAction.registerPlaceholders(placeholders);
        Action action = new Action(core,placeholders,actionConfigId,this,player, eventAction);
        actionsActive.put(action.getId(),action);
        return action;
    }

    public void setValueGlobalVar(String storageId, String var, Object object){
        if(varsGlobal.get(storageId).containsKey(storageId)){
            GlobalVar globalVar = varsGlobal.get(storageId).get(var);
            globalVar.setData(object);
            return;
        }
        GlobalVar globalVar = new GlobalVar(object);
        varsGlobal.get(storageId).put(var, globalVar);
    }
    public GlobalVar getGlobalVar(String var, String storageId){
        return varsGlobal.get(storageId).getOrDefault(var,null);
    }
    public void removeGlobalVar(String storageId, String var){
        if(varsGlobal.get(storageId).containsKey(var)) varsGlobal.get(storageId).remove(var);
    }

    public HashMap<String, Action> getActionsActive() {
        return actionsActive;
    }

    public HashMap<String, HashMap<String, GlobalVar>> getVarsGlobal() {
        return varsGlobal;
    }

    public void callActionsEvent(EventEnum eventEnum, Storage storage, Player player, Object... objects){
        ActionConfigManager actionConfigManager = core.getManagers().getActionConfigManager();
        Set<String> configs = actionConfigManager.getEventsConfig().get(eventEnum);
        if(configs.isEmpty()) return;
        EventAction eventAction = null;
        try {
            eventAction = (EventAction) eventEnum.getEventActionClass().getConstructors()[0].newInstance(objects);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        for(String configId : configs){
            createAction(storage, configId, player,eventAction).execute();
        }

    }
}
