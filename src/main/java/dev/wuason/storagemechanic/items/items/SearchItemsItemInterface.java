package dev.wuason.storagemechanic.items.items;

import com.google.common.collect.ImmutableCollection;
import dev.wuason.libs.invmechaniclib.types.InvCustom;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.InvCustomPagesContent;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.InvCustomPagesContentManager;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.items.NextPageItem;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.items.PreviousPageItem;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.configuration.inventories.InventoryConfig;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.nms.wrappers.NMSManager;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageItemDataInfo;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class SearchItemsItemInterface extends ItemInterface {

    private final String invId;
    private final String invResultId;
    private final SearchType searchType;
    private final SearchInput searchInput;
    private final String invAnvilId;
    private final StorageMechanic core = StorageMechanic.getInstance();

    public SearchItemsItemInterface(String item, String displayName, List<String> lore, String id, @Nullable String invId, @Nullable String invResultId, @Nullable SearchType searchType, @NotNull SearchInput searchInput, @Nullable String invAnvilId) {
        super(item, displayName, lore, id, "SEARCH_ITEM");
        this.invId = invId;
        this.invAnvilId = invAnvilId;
        this.invResultId = invResultId;
        this.searchType = searchType;
        this.searchInput = searchInput;
    }

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig, StorageManager storageManager) {

        Player player = (Player) event.getWhoClicked();

        if(searchType == null){
            //TODO: Open search inventory
            return;
        }

        player.closeInventory();
        openInvSelector(storage, storageInventory, event, storageConfig, storageManager);

    }

    public void openInvSelector(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig, StorageManager storageManager){

        //**** CHECKS ****//

        if(!core.getManagers().getInventoryConfigManager1().existInventoryConfig(invId)){
            AdventureUtils.sendMessagePluginConsole(core, String.format("<red>InventoryConfig %s not found", invId));
            return;
        }

        InventoryConfig invConfig = core.getManagers().getInventoryConfigManager1().createInventoryConfig( builder -> builder.setId(invId));

        InvCustom invCustom = new InvCustom(invConfig.getCreateInventoryFunction());

        invConfig.setItemBlockedConsumer((itemInterface, itemConfig) -> {

            invCustom.registerItemInterface(itemInterface);
            invCustom.setItemInterfaceInv(itemInterface, itemConfig.getSlots());

        });

        invConfig.setOnItemLoad( (inventoryConfig, configurationSection, itemConfig) -> {

            switch (itemConfig.getActionId()){
                case "BY_MATERIAL" -> {

                    dev.wuason.libs.invmechaniclib.items.ItemInterface itemInterface = invCustom.registerItemInterface( builder -> {

                        builder.setItemStack(Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                        builder.addData(itemConfig);
                        builder.onClick((e, inv) -> {

                            if(this.searchInput.equals(SearchInput.SIGN)) {
                                signOpen((Player) e.getWhoClicked(), storageInventory, SearchType.BY_MATERIAL);
                            }

                        });

                    });

                    invCustom.setItemInterfaceInv(itemInterface, itemConfig.getSlots());

                }
            }

        });

        invConfig.load();

        invCustom.open((Player) event.getWhoClicked());

    }

    public void signOpen(Player player, StorageInventory storageInv, SearchType searchType){
        NMSManager.getVersionWrapper().openSing(player, lines -> {
            StringBuilder builder = new StringBuilder();
            for(String line : lines){
                builder.append(line);
            }
            String text = builder.toString().trim();
            if(text.isEmpty()) return;
            BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    List<StorageItemDataInfo> items = searchType.search(text, storageInv.getStorage());
                    openResult(items, player, storageInv);
                }
            };
            bukkitRunnable.runTaskAsynchronously(core);
        });

    }


    public void openResult(List<StorageItemDataInfo> list, Player player, StorageInventory storageInv){

        if(!core.getManagers().getInventoryConfigManager1().existInventoryConfig(invResultId)){
            AdventureUtils.sendMessagePluginConsole(core, String.format("<red>InventoryConfig %s not found", invResultId));
            return;
        }

        InventoryConfig invConfig = core.getManagers().getInventoryConfigManager1().createInventoryConfig( builder -> builder.setId(invResultId));

        InvCustomPagesContentManager<StorageItemDataInfo> invManager = new InvCustomPagesContentManager<>(Utils.configFill(invConfig.getSection().getStringList("data_slots")), null, null){

            @Override
            public ItemStack onContentPage(int page, int slot, StorageItemDataInfo content) {

            }
        };

        invManager.setContentList(list);

        invManager.setDefaultInventory((inventoryManager, page) -> {
            return new InvCustomPagesContent(invConfig.getCreateInventoryFunction(), invManager, page);
        });

        invConfig.setItemBlockedConsumer((itemInterface, itemConfig) -> {
            invManager.addInventoryCustomPagesListenerCreate(invCustom -> {
                invCustom.registerItemInterface(itemInterface);
                invCustom.setItemInterfaceInv(itemInterface, itemConfig.getSlots());
            });
        });

        invConfig.setOnItemLoad( (inventoryConfig, configurationSection, itemConfig) -> {
            switch (itemConfig.getActionId()){
                case "NEXT_PAGE" -> {
                    NextPageItem nextPageItem = new NextPageItem(itemConfig.getSlots()[0], Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    invManager.setNextButton(nextPageItem);
                    invManager.addInventoryCustomPagesListenerCreate(invCustom -> {
                        invCustom.registerItemInterface(nextPageItem);
                    });
                }
                case "BACK_PAGE" -> {
                    PreviousPageItem previousPageItem = new PreviousPageItem(itemConfig.getSlots()[0], Adapter.getInstance().getItemStack(itemConfig.getItemId()));
                    invManager.setBackButton(previousPageItem);
                    invManager.addInventoryCustomPagesListenerCreate(invCustom -> {
                        invCustom.registerItemInterface(previousPageItem);
                    });

                }
            }
        });

        invConfig.setOnLoad(inventoryConfig -> {
            inventoryConfig.addData("DATA_SLOTS", inventoryConfig.getSection().getStringList("data_slots"));
            inventoryConfig.addData("RESULT_LORE", inventoryConfig.getSection().getStringList("result_lore"));
        });

        invConfig.load();

        Bukkit.getScheduler().runTask(core, () -> invManager.open(player, 0));
    }




    public String getInvId() {
        return invId;
    }

    public String getInvResultId() {
        return invResultId;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public SearchInput getSearchInput() {
        return searchInput;
    }

    public String getInvAnvilId() {
        return invAnvilId;
    }

    public enum SearchType {
        BY_MATERIAL((storage, s) -> List.copyOf(storage.searchItemsByMaterial(s, false))),
        BY_ITEM_ADAPTER((storage, s) -> List.copyOf(storage.searchItemsByAdapterId(s, false))),
        BY_DISPLAY_NAME((storage, s) -> List.copyOf(storage.searchItemsByName(s, false)));

        public final BiFunction<Storage, String, List<StorageItemDataInfo>> function;

        private SearchType(BiFunction<Storage, String, List<StorageItemDataInfo>> function){
            this.function = function;
        }

        public List<StorageItemDataInfo> search(String input, Storage storage){
            return function.apply(storage, input);
        }
    }

    public enum SearchInput {

        ANVIL(),
        SIGN();


    }
}
