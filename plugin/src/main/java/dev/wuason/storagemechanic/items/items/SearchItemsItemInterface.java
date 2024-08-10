package dev.wuason.storagemechanic.items.items;

import dev.wuason.libs.invmechaniclib.events.CloseEvent;
import dev.wuason.libs.invmechaniclib.types.InvCustom;
import dev.wuason.libs.invmechaniclib.types.pages.content.anvil.InvCustomPagesAnvil;
import dev.wuason.libs.invmechaniclib.types.pages.content.anvil.events.ContentClickAnvilEvent;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.InvCustomPagesContent;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.InvCustomPagesContentManager;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.events.ContentClickEvent;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.items.NextPageItem;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.items.PreviousPageItem;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.configuration.inventories.InventoryConfig;
import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.mechanics.utils.*;
import dev.wuason.mechanics.utils.functions.QuadConsumer;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
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
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig) {

        Player player = (Player) event.getWhoClicked();

        player.closeInventory();
        if (searchType != null) {
            searchInput.open(this, player, storageInventory, searchType);
            return;
        }
        openInvSelector(storageInventory, event);
    }

    public void openInvSelector(StorageInventory storageInventory, InventoryClickEvent event) {

        //**** CHECKS ****//

        if (!core.getManagers().getInventoryConfigManager().existInventoryConfig(invId)) {
            AdventureUtils.sendMessagePluginConsole(core, String.format("<red>InventoryConfig %s not found", invId));
            return;
        }

        InventoryConfig invConfig = core.getManagers().getInventoryConfigManager().createInventoryConfig(builder -> builder.setId(invId));

        InvCustom invCustom = new InvCustom(invConfig.getCreateInventoryFunction());

        invConfig.setItemBlockedConsumer((itemInterface, itemConfig) -> {

            invCustom.registerItemInterface(itemInterface);
            invCustom.setItemInterfaceInv(itemInterface, itemConfig.getSlots());

        });

        invConfig.setOnItemLoad((inventoryConfig, configurationSection, itemConfig) -> {

            try {

                SearchType searchType = SearchType.valueOf(itemConfig.getActionId());

                dev.wuason.libs.invmechaniclib.items.ItemInterface itemInterface = invCustom.registerItemInterface(builder -> {

                    builder.setItemStack(Adapter.getItemStack(itemConfig.getItemId()));
                    builder.addData(itemConfig);
                    builder.onClick((e, inv) -> {

                        searchInput.open(this, (Player) e.getWhoClicked(), storageInventory, searchType);

                    });

                });

                invCustom.setItemInterfaceInv(itemInterface, itemConfig.getSlots());

            } catch (IllegalArgumentException ignored) {
            }

        });

        invConfig.load();

        invCustom.open((Player) event.getWhoClicked());

    }

    public void signOpen(Player player, StorageInventory storageInv, SearchType searchType) {
        NMSManager.getVersionWrapper().openSing(player, lines -> {
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                builder.append(line);
            }
            String text = builder.toString().trim();
            if (text.isEmpty()) return;
            BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    List<StorageItemDataInfo> items = searchType.search(text, storageInv.getStorage());
                    openResult(items, player, storageInv, text);
                }
            };
            bukkitRunnable.runTaskAsynchronously(core);
        });
    }

    public void openAnvil(Player player, StorageInventory storageInventory, SearchType searchType) {
        if (!core.getManagers().getInventoryConfigManager().existInventoryConfig(invAnvilId)) {
            AdventureUtils.sendMessagePluginConsole(core, String.format("<red>InventoryConfig %s not found", invAnvilId));
            return;
        }

        InventoryConfig invConfig = core.getManagers().getInventoryConfigManager().createInventoryConfig(builder -> builder.setId(invAnvilId));


        InvCustomPagesAnvil<StorageItemDataInfo> invManagerAnvil = new InvCustomPagesAnvil<>(AdventureUtils.deserializeLegacy(invConfig.getTitle()), player, Utils.configFill(invConfig.getSection().getStringList("data_slots")), new ArrayList<>(), null, null, null) {

            private Set<StorageItemDataInfo> toAddInventory = new HashSet<>();

            @Override
            public void onRenameTextAsync(String before, String now) {
                toAddInventory.clear();
                List<StorageItemDataInfo> contentList = searchType.search(now, storageInventory.getStorage());
                setSearchList(contentList);
                setActualPage(0);
                setContent(0);
                setButtonsPage(0);
            }

            @Override
            public ItemStack onContentPage(int page, int slot, StorageItemDataInfo content) {
                List<String> lore = invConfig.getSection().getStringList("result_lore");
                if (lore == null) lore = new ArrayList<>();
                ItemBuilderMechanic itemBuilder = ItemBuilderMechanic.copyOf(content.getItemStack());
                Map<String, String> placeholders = Map.of("%SLOT%", content.getSlot() + "", "%PAGE%", content.getPage() + "");
                for (String line : lore) {
                    itemBuilder.addLoreLine(AdventureUtils.deserializeLegacy(Utils.replaceVariables(line, placeholders)));
                }
                if (toAddInventory.contains(content))
                    itemBuilder.addLoreLine(AdventureUtils.deserializeLegacy(invConfig.getSection().getString("added_to_inventory", "<green>ADDED!")));
                return itemBuilder.build();
            }

            @Override
            public void onContentClick(ContentClickAnvilEvent<StorageItemDataInfo> event) {

                if(toAddInventory.contains(event.getContent())) {
                    toAddInventory.remove(event.getContent());
                    new ItemBuilderMechanic(event.getEvent().getCurrentItem()).removeLastLoreLine().build();
                    return;
                }
                toAddInventory.add(event.getContent());
                new ItemBuilderMechanic(event.getEvent().getCurrentItem()).addLoreLine(AdventureUtils.deserializeLegacy(invConfig.getSection().getString("added_to_inventory", "<green>ADDED!"))).build();

            }

            @Override
            public void onClose(CloseEvent closeEvent) {
                for (StorageItemDataInfo storageItemDataInfo : toAddInventory) {
                    if (!storageItemDataInfo.exists()) return;
                    storageItemDataInfo.removeWithRestrictions();
                    StorageUtils.addItemToInventoryOrDrop(getPlayer(), storageItemDataInfo.getItemStack());
                }
            }
        };
        invManagerAnvil.setPickupCancel(true);
        invManagerAnvil.savePlayerInventory();
        long refreshTicks = invConfig.getSection().getLong("refresh_ticks", 0L);
        if(refreshTicks>0){
            invManagerAnvil.setRenameTextListener(refreshTicks);
        }

        invConfig.setItemBlockedConsumer((itemInterface, itemConfig) -> {
            invManagerAnvil.registerItemInterface(itemInterface);
            invManagerAnvil.setItemInterfaceInvPlayer(itemInterface, itemConfig.getSlots());
        });

        invConfig.setOnItemLoad((inventoryConfig, configurationSection, itemConfig) -> {
            switch (itemConfig.getActionId()) {
                case "NEXT_PAGE" -> {
                    dev.wuason.libs.invmechaniclib.types.pages.content.anvil.items.NextPageItem nextPageItem = new dev.wuason.libs.invmechaniclib.types.pages.content.anvil.items.NextPageItem(itemConfig.getSlots()[0], Adapter.getItemStack(itemConfig.getItemId()));
                    invManagerAnvil.setItemNext(nextPageItem);
                }
                case "BACK_PAGE" -> {
                    dev.wuason.libs.invmechaniclib.types.pages.content.anvil.items.PreviousPageItem previousPageItem = new dev.wuason.libs.invmechaniclib.types.pages.content.anvil.items.PreviousPageItem(itemConfig.getSlots()[0], Adapter.getItemStack(itemConfig.getItemId()));
                    invManagerAnvil.setItemBack(previousPageItem);
                }
                case "REPAIR_ITEM" -> {
                    ItemStack itemStack = new ItemBuilderMechanic(Adapter.getItemStack(itemConfig.getItemId()), 1).buildWithVoidName();
                    invManagerAnvil.setItemStackRename(itemStack);
                }
                case "RESEARCH_ITEM" -> {

                    dev.wuason.libs.invmechaniclib.items.ItemInterface itemInterface = invManagerAnvil.registerItemInterface(builder -> {

                        builder.setItemStack(Adapter.getItemStack(itemConfig.getItemId()));
                        builder.addData(itemConfig);
                        builder.onClick((e, inv) -> {

                            BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    List<StorageItemDataInfo> items = searchType.search(invManagerAnvil.getRenameText(), storageInventory.getStorage());
                                    invManagerAnvil.setSearchList(items);
                                    invManagerAnvil.setActualPage(0);
                                    invManagerAnvil.setContent(0);
                                    invManagerAnvil.setButtonsPage(0);
                                }
                            };

                            bukkitRunnable.runTaskAsynchronously(core);

                        });

                    });

                    invManagerAnvil.registerItemInterface(itemInterface);

                    invManagerAnvil.setItemInterfaceInvPlayer(itemInterface, itemConfig.getSlots());

                }

                case "OPEN_RESULT" -> {

                    dev.wuason.libs.invmechaniclib.items.ItemInterface itemInterface = invManagerAnvil.registerItemInterface(builder -> {

                        builder.setItemStack(Adapter.getItemStack(itemConfig.getItemId()));
                        builder.addData(itemConfig);
                        builder.onClick((e, inv) -> {

                            BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    openResult(invManagerAnvil.getSearchList(), player, storageInventory, invManagerAnvil.getRenameText());
                                }
                            };

                            bukkitRunnable.runTaskAsynchronously(core);

                        });

                    });

                    invManagerAnvil.registerItemInterface(itemInterface);

                    invManagerAnvil.setItemInterfaceInvPlayer(itemInterface, itemConfig.getSlots());

                }
            }
        });

        invConfig.setOnLoad(inventoryConfig -> {
            inventoryConfig.addData("DATA_SLOTS", inventoryConfig.getSection().getStringList("data_slots"));
            inventoryConfig.addData("RESULT_LORE", inventoryConfig.getSection().getStringList("result_lore"));
            inventoryConfig.addData("REFRESH_TICKS", inventoryConfig.getSection().getLong("refresh_ticks", 20L));
        });


        invConfig.load();

        Bukkit.getScheduler().runTask(core, () -> invManagerAnvil.openSimple());
    }


    public void openResult(List<StorageItemDataInfo> list, Player player, StorageInventory storageInv, String searchText) {

        if (!core.getManagers().getInventoryConfigManager().existInventoryConfig(invResultId)) {
            AdventureUtils.sendMessagePluginConsole(core, String.format("<red>InventoryConfig %s not found", invResultId));
            return;
        }

        InventoryConfig invConfig = core.getManagers().getInventoryConfigManager().createInventoryConfig(builder -> builder.setId(invResultId));

        InvCustomPagesContentManager<StorageItemDataInfo> invManager = new InvCustomPagesContentManager<>(Utils.configFill(invConfig.getSection().getStringList("data_slots")), null, null) {

            @Override
            public ItemStack onContentPage(int page, int slot, StorageItemDataInfo content) {
                List<String> lore = invConfig.getSection().getStringList("result_lore");
                if (lore == null) lore = new ArrayList<>();
                ItemBuilderMechanic itemBuilder = ItemBuilderMechanic.copyOf(content.getItemStack());
                Map<String, String> placeholders = Map.of("%SLOT%", content.getSlot() + "", "%PAGE%", content.getPage() + "");
                for (String line : lore) {
                    itemBuilder.addLoreLine(AdventureUtils.deserializeLegacy(Utils.replaceVariables(line, placeholders)));
                }
                return itemBuilder.build();
            }

            @Override
            public void onContentClick(ContentClickEvent event) {
                StorageItemDataInfo storageItem = (StorageItemDataInfo) event.getContent();
                Player pClick = (Player) event.getEvent().getWhoClicked();
                if (!storageItem.exists()) {
                    removeContentAndUpdate(storageItem, event.getInventoryCustomPagesContent().getPage());
                    return;
                }
                if (event.getEvent().isShiftClick()) {
                    HashMap<Integer, ItemStack> map = pClick.getInventory().addItem(storageItem.getItemStack());
                    if (map.isEmpty()) {
                        storageItem.removeWithRestrictions();
                        removeContentAndUpdate(storageItem, event.getInventoryCustomPagesContent().getPage());
                        return;
                    }
                    storageItem.getItemStack().setAmount(map.get(0).getAmount());
                    setContent(event.getInventoryCustomPagesContent().getPage());
                    return;
                }
                if (event.getEvent().getCursor() != null && !event.getEvent().getCursor().getType().isAir()) return;
                pClick.setItemOnCursor(storageItem.getItemStack());
                storageItem.removeWithRestrictions();
                removeContentAndUpdate(storageItem, event.getInventoryCustomPagesContent().getPage());
            }
        };

        invManager.setContentList(list);

        invManager.setDefaultInventory((inventoryManager, page) -> {
            return new InvCustomPagesContent(invConfig.getCreateInventoryFunction(), invManager, page) {
                @Override
                public void onDrag(InventoryDragEvent event) {
                    for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
                        if (InventoryUtils.isOpenedInventory(entry.getKey(), this.getInventory())) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                @Override
                public void onClick(InventoryClickEvent event) {
                    if (event.getClickedInventory() == null) return;
                    if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                        if (event.isShiftClick()) return;
                        event.setCancelled(false);
                    }
                }
            };
        });

        invConfig.setItemBlockedConsumer((itemInterface, itemConfig) -> {
            invManager.addInventoryCustomPagesListenerCreate(invCustom -> {
                invCustom.registerItemInterface(itemInterface);
                invCustom.setItemInterfaceInv(itemInterface, itemConfig.getSlots());
            });
        });

        invConfig.setOnItemLoad((inventoryConfig, configurationSection, itemConfig) -> {
            switch (itemConfig.getActionId()) {
                case "NEXT_PAGE" -> {
                    NextPageItem nextPageItem = new NextPageItem(itemConfig.getSlots()[0], Adapter.getItemStack(itemConfig.getItemId()));
                    invManager.setNextButton(nextPageItem);
                    invManager.addInventoryCustomPagesListenerCreate(invCustom -> {
                        invCustom.registerItemInterface(nextPageItem);
                    });
                }
                case "BACK_PAGE" -> {
                    PreviousPageItem previousPageItem = new PreviousPageItem(itemConfig.getSlots()[0], Adapter.getItemStack(itemConfig.getItemId()));
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

        private SearchType(BiFunction<Storage, String, List<StorageItemDataInfo>> function) {
            this.function = function;
        }

        public List<StorageItemDataInfo> search(String input, Storage storage) {
            return function.apply(storage, input);
        }
    }

    public enum SearchInput {

        ANVIL(SearchItemsItemInterface::openAnvil),
        SIGN(SearchItemsItemInterface::signOpen);

        private final QuadConsumer<SearchItemsItemInterface, Player, StorageInventory, SearchType> consumer;

        private SearchInput(QuadConsumer<SearchItemsItemInterface, Player, StorageInventory, SearchType> consumer) {
            this.consumer = consumer;
        }

        public void open(SearchItemsItemInterface itemInterface, Player player, StorageInventory storageInventory, SearchType searchType) {
            consumer.accept(itemInterface, player, storageInventory, searchType);
        }
    }
}
