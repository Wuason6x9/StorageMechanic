package dev.wuason.storagemechanic.items.items;

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

import java.util.List;
import java.util.UUID;

public class DropItemsItemInterface extends ItemInterface {

    private final StorageMechanic core = StorageMechanic.getInstance();

    public DropItemsItemInterface(String item, String displayName, List<String> lore, String id) {
        super(item, displayName, lore, id, "DROP_ITEMS");
    }

    //drop items by material, by itemAdapter, by displayName, ByActualPage, AllPages

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig, StorageManager storageManager) {

        if(!core.getManagers().getInventoryConfigManager1().existInventoryConfig("drop-items")) {
            //message

            AdventureUtils.sendMessagePluginConsole(core, "<red>InventoryConfig drop-items not found");

            return;
        }

        Player player = (Player) event.getWhoClicked();

        InventoryConfig invConfig = core.getManagers().getInventoryConfigManager1().createInventoryConfig(invConfigBuilder -> {
            invConfigBuilder.setId("drop-items");
        });

        InvCustom inv = new InvCustom(invConfig.getCreateInventoryFunction());

        invConfig.setOnItemLoad((inventoryConfig, configurationSection, itemConfig) -> {

            if(itemConfig.getActionId().equalsIgnoreCase("blocked")){

                dev.wuason.libs.invmechaniclib.items.ItemInterface item = inv.registerItemInterface(builder -> {

                    builder.setId(UUID.randomUUID().toString());
                    builder.setItemStack(Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    builder.setAmount(itemConfig.getAmount());
                    builder.setSlot(0);
                    builder.addData(itemConfig);
                    builder.onClick((e, invC) -> e.setCancelled(true));

                });

                inv.setItemInterfaceInv(item, itemConfig.getSlots());

            }

            //by material
            if(itemConfig.getActionId().equalsIgnoreCase("ByMaterial")){

                dev.wuason.libs.invmechaniclib.items.ItemInterface item = inv.registerItemInterface(builder -> {

                    builder.setId(UUID.randomUUID().toString());
                    builder.setItemStack(Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    builder.setAmount(itemConfig.getAmount());
                    builder.setSlot(0);
                    builder.addData(itemConfig);
                    builder.onClick((e, invC) -> {

                        ServerNmsVersion.getVersionWrapper().openSing(player, lines -> {

                            StringBuilder stringBuilder = new StringBuilder();

                            for(String line : lines){
                                stringBuilder.append(line);
                            }

                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

                                List<StorageItemDataInfo> storageItemDataInfos = storage.searchItemsByMaterial(stringBuilder.toString(), true);
                                storage.dropItems(storageItemDataInfos, player.getLocation(), true, true);

                            });

                        });

                    });

                });

                inv.setItemInterfaceInv(item, itemConfig.getSlots());

            }

            //by itemAdapter

            if(itemConfig.getActionId().equalsIgnoreCase("ByItemAdapter")){

                dev.wuason.libs.invmechaniclib.items.ItemInterface item = inv.registerItemInterface(builder -> {

                    builder.setId(UUID.randomUUID().toString());
                    builder.setItemStack(Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    builder.setAmount(itemConfig.getAmount());
                    builder.setSlot(0);
                    builder.addData(itemConfig);
                    builder.onClick((e, invC) -> {

                        ServerNmsVersion.getVersionWrapper().openSing(player, lines -> {

                            StringBuilder stringBuilder = new StringBuilder();

                            for(String line : lines){
                                stringBuilder.append(line);
                            }

                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

                                List<StorageItemDataInfo> storageItemDataInfos = storage.searchItemsByAdapterId(stringBuilder.toString(), false);
                                storage.dropItems(storageItemDataInfos, player.getLocation(), true, true);

                            });

                        });

                    });

                });

                inv.setItemInterfaceInv(item, itemConfig.getSlots());

            }

            //by displayName

            if(itemConfig.getActionId().equalsIgnoreCase("ByDisplayName")){

                dev.wuason.libs.invmechaniclib.items.ItemInterface item = inv.registerItemInterface(builder -> {

                    builder.setId(UUID.randomUUID().toString());
                    builder.setItemStack(Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    builder.setAmount(itemConfig.getAmount());
                    builder.setSlot(0);
                    builder.addData(itemConfig);
                    builder.onClick((e, invC) -> {

                        ServerNmsVersion.getVersionWrapper().openSing(player, lines -> {

                            StringBuilder stringBuilder = new StringBuilder();

                            for(String line : lines){
                                stringBuilder.append(line);
                            }

                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

                                List<StorageItemDataInfo> storageItemDataInfos = storage.searchItemsByName(stringBuilder.toString(), false);
                                storage.dropItems(storageItemDataInfos, player.getLocation(), true, true);

                            });

                        });

                    });

                });

                inv.setItemInterfaceInv(item, itemConfig.getSlots());

            }

            //ByActualPage

            if(itemConfig.getActionId().equalsIgnoreCase("ByActualPage")){

                dev.wuason.libs.invmechaniclib.items.ItemInterface item = inv.registerItemInterface(builder -> {

                    builder.setId(UUID.randomUUID().toString());
                    builder.setItemStack(Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    builder.setAmount(itemConfig.getAmount());
                    builder.setSlot(0);
                    builder.addData(itemConfig);
                    builder.onClick((e, invC) -> {

                        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

                            storage.dropItemsFromPage(player.getLocation(), storageInventory.getPage());

                        });

                    });

                });

                inv.setItemInterfaceInv(item, itemConfig.getSlots());

            }

            //AllPages

            if(itemConfig.getActionId().equalsIgnoreCase("AllPages")){

                dev.wuason.libs.invmechaniclib.items.ItemInterface item = inv.registerItemInterface(builder -> {

                    builder.setId(UUID.randomUUID().toString());
                    builder.setItemStack(Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    builder.setAmount(itemConfig.getAmount());
                    builder.setSlot(0);
                    builder.addData(itemConfig);
                    builder.onClick((e, invC) -> {

                        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {

                            storage.dropAllItems(player.getLocation());

                        });

                    });

                });

                inv.setItemInterfaceInv(item, itemConfig.getSlots());

            }

        });

        invConfig.load();

        inv.open(player);
    }
}
