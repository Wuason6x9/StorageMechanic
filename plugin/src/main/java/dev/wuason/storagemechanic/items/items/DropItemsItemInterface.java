package dev.wuason.storagemechanic.items.items;

import dev.wuason.libs.adapter.Adapter;
import dev.wuason.libs.apache.commons.lang3.function.TriConsumer;
import dev.wuason.libs.invmechaniclib.types.InvCustom;
import dev.wuason.mechanics.configuration.inventories.InventoryConfig;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.nms.wrappers.NMSManager;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageItemDataInfo;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig) {

        if(!core.getManagers().getInventoryConfigManager().existInventoryConfig(inventoryConfigId)){

            AdventureUtils.sendMessagePluginConsole(core, String.format("<red>InventoryConfig %s not found", inventoryConfigId));

            return;
        }

        Player player = (Player) event.getWhoClicked();

        if(dropItemsType != null){
            if(dropItemsType.isPlayerCloseInventory()) player.closeInventory();
            dropItemsType.run(this, player, storageInventory);
            return;
        }

        InventoryConfig invConfig = core.getManagers().getInventoryConfigManager().createInventoryConfig(invConfigBuilder -> {
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

                    builder.setItemStack(Adapter.getItemStack(itemConfig.getItemId()));
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
        NMSManager.getVersionWrapper().openSing(player, lines -> {

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
        NMSManager.getVersionWrapper().openSing(player, lines -> {

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
        NMSManager.getVersionWrapper().openSing(player, lines -> {

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
        storageInventory.getStorage().dropItemsFromPage(player.getLocation(), storageInventory.getPage());
    }

    public void allPages(Player player, StorageInventory storageInventory){
        storageInventory.getStorage().dropAllItems(player.getLocation());
    }

    public enum DropItemsType {
        BY_MATERIAL(DropItemsItemInterface::byMaterial, true),
        BY_ITEM_ADAPTER(DropItemsItemInterface::byItemAdapter, true),
        BY_DISPLAY_NAME(DropItemsItemInterface::byDisplayName, true),
        BY_ACTUAL_PAGE(DropItemsItemInterface::byActualPage, false),
        ALL_PAGES(DropItemsItemInterface::allPages, false);

        private final TriConsumer<DropItemsItemInterface, Player, StorageInventory> f;
        private final boolean playerCloseInventory;

        private DropItemsType(TriConsumer<DropItemsItemInterface, Player, StorageInventory> f, boolean playerCloseInventory) {
            this.f = f;
            this.playerCloseInventory = playerCloseInventory;
        }

        public void run(DropItemsItemInterface d, Player p, StorageInventory s) {
            f.accept(d, p, s);
        }

        public boolean isPlayerCloseInventory() {
            return playerCloseInventory;
        }
    }
}
