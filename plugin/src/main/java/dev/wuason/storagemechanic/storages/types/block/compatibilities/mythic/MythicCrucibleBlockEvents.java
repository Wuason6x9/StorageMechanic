package dev.wuason.storagemechanic.storages.types.block.compatibilities.mythic;

import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.skills.SkillTriggers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicCrucibleBlockEvents implements Listener {

    MythicPlaceBlockSkill mythicPlaceBlockSkill;
    BlockStorageManager blockStorageManager;

    public MythicCrucibleBlockEvents(BlockStorageManager blockStorageManager) {
        this.blockStorageManager = blockStorageManager;
    }

    @EventHandler
    public void onMechanicMythicLoad(MythicMechanicLoadEvent event){

        if(event.getMechanicName().equalsIgnoreCase("smBlock") && event.getContainer().getTrigger().toString().contains("BLOCKPLACE")){
            event.register(new MythicPlaceBlockSkill(blockStorageManager,event.getConfig()));
        }

    }


}
