package dev.wuason.storagemechanic.storages.types.block.compatibilities.mythic;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.targeters.ISkillTargeter;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.blocks.CustomBlockItemContext;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MythicPlaceBlockSkill implements INoTargetSkill {
    private BlockStorageManager blockStorageManager;


    public MythicPlaceBlockSkill(BlockStorageManager blockStorageManager, MythicLineConfig mythicLineConfig) {
        this.blockStorageManager = blockStorageManager;
    }

    @Override
    public SkillResult cast(SkillMetadata skillMetadata) {
        Location location = BukkitAdapter.adapt(skillMetadata.getOrigin());
        Bukkit.getScheduler().runTask(StorageMechanic.getInstance(),() ->{
            Block block = location.getBlock();
            CustomBlockItemContext customBlockItemContext = MythicCrucible.inst().getItemManager().getCustomBlockManager().getBlockFromBlock(block).orElseThrow();
            Player player = (Player) BukkitAdapter.adapt(skillMetadata.getCaster().getEntity());
            ItemStack itemHand = player.getInventory().getItemInMainHand();
            blockStorageManager.onBlockPlace(block,player,itemHand);
        });
        return SkillResult.SUCCESS;
    }
}
