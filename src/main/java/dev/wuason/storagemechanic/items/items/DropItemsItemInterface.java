package dev.wuason.storagemechanic.items.items;

import dev.wuason.libs.apache.lang3.function.TriConsumer;
import dev.wuason.libs.invmechaniclib.types.InvCustom;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.configuration.inventories.InventoryConfig;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.nms.wrappers.ServerNmsVersion;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.inventories.InventoryConfigManager;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageItemDataInfo;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.BiConsumer;

public class DropItemsItemInterface extends ItemInterface {

    private final StorageMechanic core = StorageMechanic.getInstance();
    private final String inventoryConfigId;
    private final DropItemsType dropItemsType;

    public DropItemsItemInterface(String item, String displayName, List<String> lore, String id, @Nullable DropItemsType dropItemsType, @Nullable String inventoryConfigId) {
        super(item, displayName, lore, id, "DROP_ITEMS");
        if(dropItemsType != null) this.dropItemsType = dropItemsType;
        else this.dropItemsType = null;
        if(inventoryConfigId != null) this.inventoryConfigId = inventoryConfigId;
        else this.inventoryConfigId = "drop-items";
    }

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig, StorageManager storageManager) {

        if(!core.getManagers().getInventoryConfigManager1().existInventoryConfig(inventoryConfigId)){

            AdventureUtils.sendMessagePluginConsole(core, String.format("<red>InventoryConfig %s not found", inventoryConfigId));

            return;
        }

        Player player = (Player) event.getWhoClicked();

        if(dropItemsType != null){
            player.closeInventory();
            dropItemsType.run(this, player, storageInventory);
            return;
        }

        InventoryConfig invConfig = core.getManagers().getInventoryConfigManager1().createInventoryConfig(invConfigBuilder -> {
            invConfigBuilder.setId(inventoryConfigId);
        });

        InvCustom inv = new InvCustom(invConfig.getCreateInventoryFunction());

        invConfig.setItemBlockedConsumer((ItemInterface, itemConfig) -> {
            inv.registerItemInterface(ItemInterface);
            inv.setItemInterfaceInv(ItemInterface, itemConfig.getSlots());
        });

        invConfig.setOnItemLoad((inventoryConfig, configurationSection, itemConfig) -> {

            try {

                DropItemsType dropItemsType = DropItemsType.valueOf(itemConfig.getActionId());

                inv.setItemInterfaceInv( inv.registerItemInterface(builder -> {

                    builder.setItemStack(Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    builder.addData(itemConfig);
                    builder.onClick((e, invC) -> {

                        dropItemsType.run(this, player, storageInventory);

                    });

                }), itemConfig.getSlots());


            }
            catch (Exception ignored){}

        });

        invConfig.load();

        inv.open(player);
    }





    public void byMaterial(Player player, StorageInventory storageInventory){
        ServerNmsVersion.getVersionWrapper().openSing(player, lines -> {

            StringBuilder stringBuilder = new StringBuilder();

            for(String line : lines){
                stringBuilder.append(line);
            }

            Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

                List<StorageItemDataInfo> storageItemDataInfos = storageInventory.getStorage().searchItemsByMaterial(stringBuilder.toString(), true);
                storageInventory.getStorage().dropItems(storageItemDataInfos, player.getLocation(), true, true);

            });

        });
    }

    public void byItemAdapter(Player player, StorageInventory storageInventory){
        ServerNmsVersion.getVersionWrapper().openSing(player, lines -> {

            StringBuilder stringBuilder = new StringBuilder();

            for(String line : lines){
                stringBuilder.append(line);
            }

            Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

                List<StorageItemDataInfo> storageItemDataInfos = storageInventory.getStorage().searchItemsByAdapterId(stringBuilder.toString(), false);
                storageInventory.getStorage().dropItems(storageItemDataInfos, player.getLocation(), true, true);

            });

        });
    }

    public void byDisplayName(Player player, StorageInventory storageInventory){
        ServerNmsVersion.getVersionWrapper().openSing(player, lines -> {

            StringBuilder stringBuilder = new StringBuilder();

            for(String line : lines){
                stringBuilder.append(line);
            }

            Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

                List<StorageItemDataInfo> storageItemDataInfos = storageInventory.getStorage().searchItemsByName(stringBuilder.toString(), false);

                storageInventory.getStorage().dropItems(storageItemDataInfos, player.getLocation(), true, true);

            });

        });
    }

    public void byActualPage(Player player, StorageInventory storageInventory){
        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

            storageInventory.getStorage().dropItemsFromPage(player.getLocation(), storageInventory.getPage());

        });
    }

    public void allPages(Player player, StorageInventory storageInventory){
        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

            storageInventory.getStorage().dropAllItems(player.getLocation());

        });
    }

    public enum DropItemsType {
        BY_MATERIAL(DropItemsItemInterface::byMaterial),
        BY_ITEM_ADAPTER(DropItemsItemInterface::byItemAdapter),
        BY_DISPLAY_NAME(DropItemsItemInterface::byDisplayName),
        BY_ACTUAL_PAGE(DropItemsItemInterface::byActualPage),
        ALL_PAGES(DropItemsItemInterface::allPages);

        private final TriConsumer<DropItemsItemInterface, Player, StorageInventory> f;

        DropItemsType(TriConsumer<DropItemsItemInterface, Player, StorageInventory> f) {
            this.f = f;
        }

        public void run(DropItemsItemInterface d, Player p, StorageInventory s) {
            f.accept(d, p, s);
        }
    }
}
