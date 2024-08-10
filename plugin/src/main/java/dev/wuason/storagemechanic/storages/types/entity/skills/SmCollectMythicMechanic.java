package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SmCollectMythicMechanic implements ITargetedEntitySkill {

    private StorageMechanic core;
    private String storageConfigId;
    public SmCollectMythicMechanic(StorageMechanic core) {
        this.core = core;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {

        SkillCaster caster = skillMetadata.getCaster();
        String id = "";
        if(Compatibilities.isMythicCrucibleLoaded()){
            if(caster instanceof Furniture) {
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
            }
        }
        if(caster instanceof ActiveMob) {
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
        }
        if(id == "") return SkillResult.SUCCESS;
        Entity entity = abstractEntity.getBukkitEntity();
        if(!entity.getType().equals(EntityType.DROPPED_ITEM)) return SkillResult.SUCCESS;

        StorageManager storageManager = core.getManagers().getStorageManager();

        if(!storageManager.storageExists(id)) return SkillResult.SUCCESS;
        Storage storage = storageManager.getStorage(id);
        Item item = (Item) entity;
        Bukkit.getScheduler().runTask(core, () ->{
            item.remove();
            Bukkit.getScheduler().runTaskAsynchronously(core, ()->{
                List<ItemStack> noAddedItems = storage.addItemStackToAllPagesWithRestrictions(item.getItemStack());
                for(ItemStack itemReturn : noAddedItems){
                    Bukkit.getScheduler().runTask(core,() -> item.getWorld().dropItem(item.getLocation(), itemReturn));
                }
            });
        });
        return SkillResult.SUCCESS;
    }
}
