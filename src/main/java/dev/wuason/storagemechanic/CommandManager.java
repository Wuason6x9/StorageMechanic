package dev.wuason.storagemechanic;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import dev.wuason.libs.commandapi.CommandAPICommand;
import dev.wuason.libs.commandapi.arguments.*;
import dev.wuason.libs.invmechaniclib.events.CloseEvent;
import dev.wuason.libs.invmechaniclib.items.ItemInterface;
import dev.wuason.libs.invmechaniclib.types.InvCustom;
import dev.wuason.libs.invmechaniclib.types.pages.content.anvil.InvCustomPagesAnvil;
import dev.wuason.libs.invmechaniclib.types.pages.content.multiple.Content;
import dev.wuason.libs.invmechaniclib.types.pages.content.multiple.InvCustomPagesContentMultiple;
import dev.wuason.libs.invmechaniclib.types.pages.content.multiple.PageCustomInfo;
import dev.wuason.libs.invmechaniclib.types.pages.content.multiple.events.ContentMultipleClickEvent;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.InvCustomPagesContent;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.InvCustomPagesContentManager;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.events.ContentClickEvent;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.events.OpenPageEvent;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.items.NextPageItem;
import dev.wuason.libs.invmechaniclib.types.pages.content.normal.items.PreviousPageItem;
import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.mechanics.utils.StorageUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.customblocks.CustomBlock;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.data.player.PlayerData;
import dev.wuason.storagemechanic.data.storage.StorageManagerData;
import dev.wuason.storagemechanic.data.storage.type.api.StorageApiData;
import dev.wuason.storagemechanic.data.storage.type.api.StorageApiManagerData;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.api.StorageApiManager;
import dev.wuason.storagemechanic.storages.types.api.StorageApiType;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class CommandManager {
    StorageMechanic core;
    public CommandManager(StorageMechanic core){
        this.core = core;
    }
    private static final String[] COLORS = {"dark_blue","dark_green","dark_aqua","dark_red","dark_purple","gold","blue","green","aqua","red","light_purple","yellow"};

    public void loadCommand(){

        new CommandAPICommand("StorageMechanic")
                .withPermission("sm.command")
                .withAliases("sm","storagem")
                .withSubcommand(new CommandAPICommand("api")
                        .withPermission("sm.command.api")
                        .withSubcommand(new CommandAPICommand("create")
                                .withPermission("sm.command.api.create")
                                .withArguments(new TextArgument("id"), new StringArgument("storageConfigId").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                                    return core.getManagers().getStorageConfigManager().getStoragesConfigMap().keySet().toArray(new String[0]);
                                })))
                                .executes((sender, args) -> {
                                    String id = (String) args.get(0);
                                    String storageConfigId = (String) args.get(1);
                                    core.getManagers().getStorageApiManager().createStorageApi(id, StorageApiType.COMMAND, storageConfigId);
                                })
                        )
                        .withSubcommand(new CommandAPICommand("open")
                                .withPermission("sm.command.api.open")
                                .withArguments(new TextArgument("id").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                                    return core.getManagers().getStorageApiManager().getAllStoragesId().toArray(new String[0]);
                                })))
                                .withArguments(new IntegerArgument("page"), new EntitySelectorArgument.OnePlayer("player").setOptional(true))
                                .executes((sender, args) -> {
                                    String id = (String) args.get(0);
                                    int page = (int) args.get(1);
                                    Player player = (args.get(2) == null) ? (Player) sender : (Player) args.get(2);
                                    Storage storage = core.getManagers().getStorageApiManager().getStorageApi(id).getStorage();
                                    if(storage == null) return;
                                    storage.openStorage(player, page);
                                })
                        )
                        .withSubcommand(new CommandAPICommand("delete")
                                .withPermission("sm.command.api.delete")
                                .withArguments(new TextArgument("id").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                                    return core.getManagers().getStorageApiManager().getAllStoragesId().toArray(new String[0]);
                                })))
                                .executes((sender, args) -> {
                                    String id = (String) args.get(0);
                                    core.getManagers().getStorageApiManager().removeStorageApi(id);
                                })
                        )
                        .withSubcommand(new CommandAPICommand("list")
                                .withPermission("sm.command.api.list")
                                .executes((sender, args) -> {
                                    String list = "";
                                    for(String id : core.getManagers().getStorageApiManager().getAllStoragesId()){
                                        list += "<" + COLORS[MathUtils.randomNumber(0, COLORS.length - 1)] + "><click:run_command:/sm api open " + id + " 0>" + id + "</click><reset>, ";
                                    }
                                    AdventureUtils.sendMessage(sender, list);
                                })
                        )
                        .withSubcommand(new CommandAPICommand("exist")
                                .withPermission("sm.command.api.exist")
                                .withArguments(new TextArgument("id").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                                    return core.getManagers().getStorageApiManager().getAllStoragesId().toArray(new String[0]);
                                })))
                                .executes((sender, args) -> {
                                    String id = (String) args.get(0);
                                    AdventureUtils.sendMessage(sender, core.getManagers().getStorageApiManager().existStorageApi(id) + "");
                                })
                        )
                        .withSubcommand(new CommandAPICommand("createIfNotExistAndOpen")
                                .withPermission("sm.command.api.createIfNotExistAndOpen")
                                .withArguments(new TextArgument("id"), new StringArgument("storageConfigId").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                                    return core.getManagers().getStorageConfigManager().getStoragesConfigMap().keySet().toArray(new String[0]);
                                })))
                                .withArguments(new IntegerArgument("page"),new EntitySelectorArgument.OnePlayer("player").setOptional(true))
                                .executes((sender, args) -> {
                                    String id = (String) args.get(0);
                                    String storageConfigId = (String) args.get(1);
                                    int page = (int) args.get(2);
                                    Player player = (args.get(3) == null) ? (Player) sender : (Player) args.get(3);
                                    Storage storage = null;
                                    if(!core.getManagers().getStorageApiManager().existStorageApi(id)){
                                        storage = core.getManagers().getStorageApiManager().createStorageApi(id, StorageApiType.COMMAND, storageConfigId).getStorage();
                                    }
                                    if(storage == null){
                                        storage = core.getManagers().getStorageApiManager().getStorageApi(id).getStorage();
                                    }
                                    if(storage == null) return;
                                    storage.openStorage(player, page);
                                })
                        )
                        .withSubcommand(new CommandAPICommand("saveAndUnload")
                                .withPermission("sm.command.api.saveAndUnload")
                                .withArguments(new TextArgument("id").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                                    return core.getManagers().getStorageApiManager().getStorageApis().keySet().toArray(new String[0]);
                                })))
                                .executes((sender, args) -> {
                                    String id = (String) args.get(0);
                                    core.getManagers().getStorageApiManager().saveStorageApi(id);
                                })
                        )
                        .withSubcommand(new CommandAPICommand("panel")
                                .withPermission("sm.command.api.panel")
                                .executes((sender, args) -> {
                                    Player player = (Player) sender;

                                    StorageManager storageManager = core.getManagers().getStorageManager();
                                    StorageManagerData storageManagerData = core.getManagers().getDataManager().getStorageManagerData();
                                    StorageApiManager storageApiManager = core.getManagers().getStorageApiManager();
                                    StorageApiManagerData storageApiManagerData = core.getManagers().getDataManager().getStorageManagerData().getStorageApiManager();

                                    InvCustomPagesContentManager<String> invManager = new InvCustomPagesContentManager<String>(
                                            IntStream.rangeClosed(0,44).boxed().toList(),
                                            new PreviousPageItem(45, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("api.panel.previous_page", "INCORRECT")).build()),
                                            new NextPageItem(53, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("api.panel.next_page","INCORRECT")).build()),
                                            (inv, page) -> {

                                                HashMap<String, String> replaces = new HashMap<>(){{
                                                    put("%page%", page + 1 + "");
                                                    put("%max_page%", inv.getMaxPage() + 1 + "");
                                                }};

                                                InvCustomPagesContent pagesContent = new InvCustomPagesContent(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("api.panel.title", "untitled"),replaces), player), 54, inv, page){
                                                    @Override
                                                    public void onClick(InventoryClickEvent event) {
                                                        event.setCancelled(true);
                                                    }

                                                    @Override
                                                    public void onDrag(InventoryDragEvent event) {
                                                        event.setCancelled(true);
                                                    }
                                                };
                                                return pagesContent;

                                            },
                                            core.getManagers().getStorageApiManager().getAllStoragesId().stream().toList()
                                    ){

                                        @Override
                                        public ItemStack onContentPage(int page, int slot, String content) {
                                            StorageApiData storageApiData = null;
                                            if(storageApiManager.getStorageApis().containsKey(content)){
                                                storageApiData = storageApiManagerData.StorageApiToStorageApiData(storageApiManager.getStorageApis().get(content));
                                            }
                                            else {
                                                storageApiData = storageApiManagerData.getStorageApiData(content);
                                            }
                                            Storage storage = null;
                                            if(storageManager.getStorageMap().containsKey(storageApiData.getStorageId())){
                                                storage = storageManager.getStorageMap().get(storageApiData.getStorageId());
                                            }
                                            else {
                                                storage = storageManagerData.loadStorageData(storageApiData.getStorageId()); //ALL VARIABLES: %loaded%, %total_items%, %creation_date%, %last_open%, %storage_config_id%, %storage_origin_context%, %storage_max_pages%, %storage_viewers%
                                            }
                                            //Vars
                                            HashMap<String, String> replaces = new HashMap<>();
                                            replaces.put("%loaded_storage_api%", storageApiManager.getStorageApis().containsKey(content) + "");
                                            replaces.put("%loaded_storage%", storageManager.getStorageMap().containsKey(storageApiData.getStorageId()) + "");
                                            replaces.put("%total_items%", storage.getAllItems().size() + "");
                                            replaces.put("%creation_date%", storage.getCreationDate().toString());
                                            replaces.put("%last_open%", storage.getLastOpen().toString());
                                            replaces.put("%storage_config_id%", storage.getStorageIdConfig());
                                            replaces.put("%storage_origin_context%", storage.getStorageOriginContext().getContext().name());
                                            replaces.put("%storage_max_pages%", storage.getTotalPages() + "");
                                            replaces.put("%storage_viewers%", storage.getAllViewers().stream().map(HumanEntity::getName).toList().toString());
                                            replaces.put("%storage_id%", storageApiData.getStorageId());
                                            replaces.put("%storage_api_id%", content);


                                            ItemStack item = new ItemBuilderMechanic(Material.CHEST).setName(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("api.panel.item_name", "untitled"),replaces), player))
                                                    .setLore(AdventureUtils.deserializeLegacyList(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getStringList("api.panel.item_lore", new ArrayList<>()),replaces), player))
                                                    .build();
                                            return item;
                                        }

                                        @Override
                                        public void onContentClick(ContentClickEvent event) {

                                            if(event.getEvent().isShiftClick()){
                                                if(event.getEvent().isLeftClick()){
                                                    setContent(event.getInventoryCustomPagesContent().getPage());
                                                    event.getInventoryCustomPagesContent().setSimpleItems(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilderMechanic(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName());
                                                }
                                                if(event.getEvent().isRightClick()){
                                                    core.getManagers().getStorageApiManager().removeStorageApi((String) event.getContent());
                                                    removeContent((String) event.getContent());
                                                    setContent(event.getInventoryCustomPagesContent().getPage());
                                                    event.getInventoryCustomPagesContent().setSimpleItems(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilderMechanic(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName());
                                                }
                                            }
                                            else {
                                                if(event.getEvent().isRightClick()){
                                                    if(core.getManagers().getStorageApiManager().getStorageApis().containsKey((String) event.getContent())){
                                                        core.getManagers().getStorageApiManager().saveStorageApi((String) event.getContent());
                                                        setContent(event.getInventoryCustomPagesContent().getPage());
                                                        event.getInventoryCustomPagesContent().setSimpleItems(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilderMechanic(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName());
                                                    }
                                                }
                                                else if (event.getEvent().isLeftClick()){
                                                    core.getManagers().getStorageApiManager().getStorageApi((String) event.getContent()).getStorage().openStorage(player, 0);
                                                }
                                            }

                                        }

                                        @Override
                                        public void onOpenPage(OpenPageEvent event) {
                                            event.getInventoryCustomPagesContent().setItem(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilderMechanic(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName() , e -> e.setCancelled(true));
                                        }
                                    };

                                    invManager.open(player, 0);

                                })

                        )
                )
                .withSubcommands(new CommandAPICommand("debug")
                        .withPermission("sm.command.debug")
                        .withSubcommands(new CommandAPICommand("test")
                                .executes((sender, args) -> {
                                    Player player = (Player) sender;


                                    InvCustomPagesAnvil<CustomStack> invCustomPagesAnvil = new InvCustomPagesAnvil<CustomStack>(
                                            "Test",
                                            player,
                                            IntStream.rangeClosed(9,35).boxed().toList(),
                                            ItemsAdder.getAllItems(),
                                            new dev.wuason.libs.invmechaniclib.types.pages.content.anvil.items.PreviousPageItem(0, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage("<red><< Previous page").build()),
                                            new dev.wuason.libs.invmechaniclib.types.pages.content.anvil.items.NextPageItem(8, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage("<red>Next page >>").build()),
                                            new ItemStack(Material.BARRIER)
                                    ){
                                        @Override
                                        public ItemStack onContentPage(int page, int slot, CustomStack content) {
                                            return content.getItemStack();
                                        }

                                        @Override
                                        public void onRenameTextAsync(String before, String now) {
                                            setActualPage(0);
                                            search(now);
                                            setContent(getActualPage());
                                            setButtonsPage(getActualPage());
                                        }

                                        @Override
                                        public CustomStack onContentSearch(String search, CustomStack content) {
                                            return content.getDisplayName().toLowerCase(Locale.ENGLISH).contains(search.toLowerCase(Locale.ENGLISH)) ? content : null;
                                        }


                                        {
                                            savePlayerInventory();
                                            setRenameTextListener(1L);
                                            ItemInterface itemInterface = new ItemInterface(1, new ItemBuilderMechanic(Material.COMPASS).setNameWithMiniMessage("<red>Search").build()){

                                                @Override
                                                public void onClick(InventoryClickEvent event, InvCustom inventoryCustom) {
                                                    setActualPage(0);
                                                    search();
                                                    setContent(getActualPage());
                                                    setButtonsPage(getActualPage());
                                                }
                                            };
                                            getPlayer().getInventory().setItem(1, addItemInterface(itemInterface).getItemModified());

                                        }

                                    };
                                    invCustomPagesAnvil.openSimple();

                                    System.out.println(invCustomPagesAnvil.getHolder() instanceof InvCustom);

                                })
                        )
                        .withSubcommands(new CommandAPICommand("ActiveHopperHashMap")
                                .executes((sender, args) -> {
                                    AdventureUtils.sendMessage(sender, "<blue>" + BlockMechanicManager.HOPPER_BLOCK_MECHANIC.getHopperActiveHashMap().toString());
                                    AdventureUtils.sendMessage(sender, "<red>--------------------------------------------------");
                                    AdventureUtils.sendMessage(sender, "<green>" + BlockMechanicManager.HOPPER_BLOCK_MECHANIC.getHopperUUIDCurrentActiveHashMap().toString());
                                    AdventureUtils.sendMessage(sender, "<red>--------------------------------------------------");
                                    AdventureUtils.sendMessage(sender, "<blue>Size: " + BlockMechanicManager.HOPPER_BLOCK_MECHANIC.getHopperActiveHashMap().size());
                                    AdventureUtils.sendMessage(sender, "<gold>Size: " + BlockMechanicManager.HOPPER_BLOCK_MECHANIC.getHopperUUIDCurrentActiveHashMap().size());
                                })
                        )
                        .withSubcommands(new CommandAPICommand("getActiveBlockStorages")
                                .executes((sender, args) -> {
                                    AdventureUtils.sendMessage(sender, "<blue>" + core.getManagers().getBlockStorageManager().getAllBlockStorages());
                                    AdventureUtils.sendMessage(sender, "<red>--------------------------------------------------");
                                    AdventureUtils.sendMessage(sender, "<blue>Size: " + core.getManagers().getBlockStorageManager().getAllBlockStorages().size());
                                })
                        )
                        .withSubcommands(new CommandAPICommand("saveAllData")
                                .executes((sender, args) -> {
                                    Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
                                        AdventureUtils.sendMessage(sender, "<red>Saving all data!");
                                        core.getManagers().saveAllData();

                                    });
                                })
                        )
                        .withSubcommands(new CommandAPICommand("enable")
                                .executes((sender, args) -> {
                                    Player player = (Player) sender;
                                    core.getDebug().enableDebugMode(player);
                                })
                        )
                        .withSubcommands(new CommandAPICommand("disable")
                                .executes((sender, args) -> {
                                    Player player = (Player) sender;
                                    core.getDebug().disableDebugMode(player);
                                })
                        )
                        .withSubcommands(new CommandAPICommand("open")
                                .withArguments(new IntegerArgument("page"),new StringArgument("ID").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                                    String[] ids = core.getManagers().getStorageManager().getStorageMap().keySet().toArray(new String[0]);
                                    return ids;
                                })))
                                .executes((sender, args) -> {

                                    Storage storage = core.getManagers().getStorageManager().getStorage((String)args.get(1));
                                    storage.openStorage((Player)sender, (int)args.get(0));

                                })
                        )
                        .withSubcommands(new CommandAPICommand("deleteTrash")
                                .executes((sender, args) -> {

                                    core.getManagers().getTrashSystemManager().cleanTrash();

                                })
                        )

                )
                .withSubcommands(new CommandAPICommand("info")
                        .withSubcommands(new CommandAPICommand("players")
                                .withPermission("sm.command.info.players")
                                .executes((sender, args) -> {
                                    Player player = (Player) sender;

                                    StorageManager storageManager = core.getManagers().getStorageManager();
                                    StorageManagerData storageManagerData = core.getManagers().getDataManager().getStorageManagerData();

                                    PageCustomInfo<Player> pagePlayers = new PageCustomInfo<Player>(Arrays.asList(0,1,2,3,9,10,11,12,18,19,20,21,27,28,29,30,36,37,38,39),
                                            new dev.wuason.libs.invmechaniclib.types.pages.content.multiple.items.PreviousPageItem(45, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.previous_page", "INCORRECT")).build()),
                                            new dev.wuason.libs.invmechaniclib.types.pages.content.multiple.items.NextPageItem(48, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.next_page", "INCORRECT")).build())
                                            );

                                    pagePlayers.setContentList(Bukkit.getOnlinePlayers().stream().map(p -> p.getPlayer()).toList());

                                    PageCustomInfo<String> pageStorages = new PageCustomInfo<String>(Arrays.asList(5,6,7,8,14,15,16,17,23,24,25,26,32,33,34,35,41,42,43,44),
                                            new dev.wuason.libs.invmechaniclib.types.pages.content.multiple.items.PreviousPageItem(50, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage("<red>Previous page").build()),
                                            new dev.wuason.libs.invmechaniclib.types.pages.content.multiple.items.NextPageItem(53, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage("<red>Next page").build())
                                    );

                                    InvCustomPagesContentMultiple invMultiple = new InvCustomPagesContentMultiple(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.title", "UNTITLED"),player),54){

                                        private Player selectedPlayer = null;

                                        @Override
                                        public void onDrag(InventoryDragEvent event) {
                                            event.setCancelled(true);
                                        }
                                        @Override
                                        public void onClick(InventoryClickEvent event) {
                                            event.setCancelled(true);
                                        }

                                        @Override
                                        public ItemStack onContentPage(Content content) {

                                            if(content.getPageCustomInfo().equals(pagePlayers)){

                                                PlayerData objectiveData = core.getManagers().getDataManager().getPlayerDataManager().getPlayerData(((Player) content.getContent()).getUniqueId());
                                                Map<String, String> replaces = new HashMap<>();
                                                replaces.put("%player%", ((Player) content.getContent()).getName());
                                                if(objectiveData != null){
                                                    replaces.put("%total_storages%", objectiveData.getStorages().size() + "");
                                                    replaces.put("%total_block_storages%", objectiveData.getBlockStorages().size() + "");
                                                    replaces.put("%total_furniture_storages%", objectiveData.getFurnitureStorages().size() + "");
                                                }
                                                return new ItemBuilderMechanic(Material.PLAYER_HEAD).setNameWithMiniMessage(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.item_name", "INCORRECT"), replaces)).setSkullOwner((Player) content.getContent()).setLore(AdventureUtils.deserializeLegacyList(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getStringList("info.players.players.item_lore", new ArrayList<>()),replaces), (Player) content.getContent())).build();

                                            }

                                            if(content.getPageCustomInfo().equals(pageStorages)){

                                                String storageId = (String) content.getContent();

                                                Storage storage = null;
                                                if(storageManager.getStorageMap().containsKey(storageId)){
                                                    storage = storageManager.getStorageMap().get(storageId);
                                                }
                                                else {
                                                    storage = storageManagerData.loadStorageData(storageId); //ALL VARIABLES: %loaded%, %total_items%, %creation_date%, %last_open%, %storage_config_id%, %storage_origin_context%, %storage_max_pages%, %storage_viewers%
                                                }

                                                //Vars
                                                HashMap<String, String> replaces = new HashMap<>();
                                                replaces.put("%loaded%", storageManager.getStorageMap().containsKey(storageId) + "");
                                                replaces.put("%total_items%", storage.getAllItems().size() + "");
                                                replaces.put("%creation_date%", storage.getCreationDate().toString());
                                                replaces.put("%last_open%", storage.getLastOpen().toString());
                                                replaces.put("%storage_config_id%", storage.getStorageIdConfig());
                                                replaces.put("%storage_origin_context%", storage.getStorageOriginContext().getContext().name());
                                                replaces.put("%storage_max_pages%", storage.getTotalPages() + "");
                                                replaces.put("%storage_viewers%", storage.getAllViewers().stream().map(HumanEntity::getName).toList().toString());
                                                replaces.put("%player%", selectedPlayer.getName());
                                                replaces.put("%storage_id%", storageId);

                                                ItemStack item = new ItemBuilderMechanic(Material.CHEST)
                                                        .setName(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.storages.item_name", "INCORRECT"),replaces),selectedPlayer))
                                                        .setLore(AdventureUtils.deserializeLegacyList(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getStringList("info.players.storages.item_lore", new ArrayList<>()),replaces),selectedPlayer))
                                                        .build();
                                                return item;
                                            }

                                            return null;
                                        }

                                        @Override
                                        public void onContentClick(ContentMultipleClickEvent event) {

                                            if(event.getPageCustomInfo().equals(pagePlayers)){
                                                Player player = (Player) event.getContent();
                                                PlayerData playerData = core.getManagers().getDataManager().getPlayerDataManager().getPlayerData(player.getUniqueId());
                                                if(playerData == null) return;
                                                List<String> storages = playerData.getStorages().keySet().stream().toList();
                                                pageStorages.setContentList(storages);
                                                setItemsSelected();
                                                selectedPlayer = player;
                                                setContentAndButtons(pageStorages);

                                            }
                                            if(event.getPageCustomInfo().equals(pageStorages)){
                                                if(event.getEvent().isShiftClick()){
                                                    if(event.getEvent().isRightClick()){
                                                        core.getManagers().getStorageManager().getStorage((String) event.getContent()).removeAllItems();
                                                        setContentPage(event.getPageCustomInfo());
                                                    }
                                                }
                                                else{
                                                    if(event.getEvent().isRightClick()){
                                                        if(core.getManagers().getStorageManager().getStorageMap().containsKey((String) event.getContent())){
                                                            core.getManagers().getStorageManager().saveStorage(core.getManagers().getStorageManager().getStorageMap().get((String) event.getContent()), SaveCause.NORMAL_SAVE);
                                                            setContentPage(event.getPageCustomInfo());
                                                        }
                                                    }
                                                    else if (event.getEvent().isLeftClick()){
                                                        core.getManagers().getStorageManager().getStorage((String) event.getContent()).openStorage(player, 0);
                                                    }
                                                }
                                            }
                                        }

                                        public Player getSelectedPlayer(){
                                            return selectedPlayer;
                                        }

                                        public void setItemsNotSelected(){
                                            setSimpleItems(new int[]{4,13,22,31,40,49}, new ItemBuilderMechanic(Material.RED_STAINED_GLASS_PANE).buildWithVoidName());
                                        }
                                        public void setItemsSelected(){
                                            setSimpleItems(new int[]{4,13,22,31,40,49}, new ItemBuilderMechanic(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName());
                                        }



                                        {

                                            setItemsNotSelected();
                                            //register items
                                            pageStorages.registerItems(this);
                                            pagePlayers.registerItems(this);
                                            //register pages
                                            addCustomPage(pagePlayers);
                                            addCustomPage(pageStorages);

                                            //set item refresh
                                            setItem(46, new ItemBuilderMechanic(Material.COMPASS).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.refresh_item_name", "INCORRECT")).build(), e -> {
                                                pagePlayers.firstPage();
                                                setContentAndButtons(pagePlayers);
                                                if(selectedPlayer != null) setItemsSelected();
                                                else setItemsNotSelected();
                                            });
                                            setItem(51, new ItemBuilderMechanic(Material.COMPASS).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.storages.refresh_item_name", "INCORRECT")).build(), e -> {
                                                if(selectedPlayer == null) return;
                                                setItemsSelected();
                                                pageStorages.firstPage();
                                                setContentAndButtons(pageStorages);
                                            });
                                            setItem(47, new ItemBuilderMechanic(Material.BARRIER).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.remove_selection_item_name", "INCORRECT")).build(), e -> {
                                                if(selectedPlayer == null) return;
                                                selectedPlayer = null;
                                                setItemsNotSelected();
                                                pageStorages.clearDataSlots(this);
                                                pageStorages.setContentList(new ArrayList<>());
                                                pageStorages.firstPage();
                                                pageStorages.removeButtonsPage(this);
                                            });



                                        }

                                    };
                                    invMultiple.setContentAndButtons(pagePlayers);
                                    invMultiple.open(player);


                                })
                        )
                        .withSubcommands(new CommandAPICommand("player")
                                .withPermission("sm.command.info.player")
                                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                                .executes((sender, args) -> {
                                    Player objective = (Player) args.get(0);
                                    Player player = (Player) sender;

                                    PlayerData playerData = core.getManagers().getDataManager().getPlayerDataManager().getPlayerData(objective.getUniqueId());
                                    if(playerData == null) return;

                                    Set<String> storages = playerData.getStorages().keySet();
                                    if(storages  == null) return;

                                    InvCustomPagesContentManager<String> invManager = new InvCustomPagesContentManager<String>(IntStream.rangeClosed(0,44).boxed().toList(), new PreviousPageItem(45, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.player.previous_page", "INCORRECT")).build()), new NextPageItem(53, new ItemBuilderMechanic(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.player.next_page","INCORRECT")).build()), (inv, page) ->
                                    {
                                        HashMap<String, String> replaces = new HashMap<>(){{
                                            put("%player%", objective.getName());
                                            put("%page%", page + 1 + "");
                                            put("%max_page%", inv.getMaxPage() + 1 + "");
                                        }};

                                        InvCustomPagesContent pagesContent = new InvCustomPagesContent(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.player.title", "untitled"),replaces), objective), 54, inv, page);
                                        return pagesContent;

                                    }, storages.stream().toList()){


                                        @Override
                                        public ItemStack onContentPage(int page, int slot, String content) {
                                            StorageManager storageManager = core.getManagers().getStorageManager();
                                            StorageManagerData storageManagerData = core.getManagers().getDataManager().getStorageManagerData();

                                            Storage storage = null;
                                            if(storageManager.getStorageMap().containsKey(content)){
                                                storage = storageManager.getStorageMap().get(content);
                                            }
                                            else {
                                                storage = storageManagerData.loadStorageData(content); //ALL VARIABLES: %loaded%, %total_items%, %creation_date%, %last_open%, %storage_config_id%, %storage_origin_context%, %storage_max_pages%, %storage_viewers%
                                            }

                                            //Vars
                                            HashMap<String, String> replaces = new HashMap<>();
                                            replaces.put("%loaded%", storageManager.getStorageMap().containsKey(content) + "");
                                            replaces.put("%total_items%", storage.getAllItems().size() + "");
                                            replaces.put("%creation_date%", storage.getCreationDate().toString());
                                            replaces.put("%last_open%", storage.getLastOpen().toString());
                                            replaces.put("%storage_config_id%", storage.getStorageIdConfig());
                                            replaces.put("%storage_origin_context%", storage.getStorageOriginContext().getContext().name());
                                            replaces.put("%storage_max_pages%", storage.getTotalPages() + "");
                                            replaces.put("%storage_viewers%", storage.getAllViewers().stream().map(HumanEntity::getName).toList().toString());
                                            replaces.put("%player%", objective.getName());
                                            replaces.put("%storage_id%", content);


                                            ItemStack item = new ItemBuilderMechanic(Material.CHEST).setName(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.player.item_name", "untitled"),replaces), objective))
                                                    .setLore(AdventureUtils.deserializeLegacyList(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getStringList("info.player.lore", new ArrayList<>()),replaces), objective))
                                                    .build();
                                            return item;
                                        }

                                        @Override
                                        public void onContentClick(ContentClickEvent event) {

                                            if(event.getEvent().isShiftClick()){
                                                if(event.getEvent().isLeftClick()){
                                                    setContent(event.getInventoryCustomPagesContent().getPage());
                                                    event.getInventoryCustomPagesContent().setItem(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilderMechanic(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName() , e -> e.setCancelled(true));
                                                }
                                                if(event.getEvent().isRightClick()){
                                                    core.getManagers().getStorageManager().getStorage((String) event.getContent()).removeAllItems();
                                                    setContent(event.getInventoryCustomPagesContent().getPage());
                                                }
                                            }
                                            else {
                                                if(event.getEvent().isRightClick()){
                                                    if(core.getManagers().getStorageManager().getStorageMap().containsKey((String) event.getContent())){
                                                        core.getManagers().getStorageManager().saveStorage(core.getManagers().getStorageManager().getStorageMap().get((String) event.getContent()), SaveCause.NORMAL_SAVE);
                                                        setContent(event.getInventoryCustomPagesContent().getPage());
                                                        event.getInventoryCustomPagesContent().setItem(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilderMechanic(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName() , e -> e.setCancelled(true));
                                                    }
                                                }
                                                else if (event.getEvent().isLeftClick()){
                                                    core.getManagers().getStorageManager().getStorage((String) event.getContent()).openStorage(player, 0);
                                                }
                                            }

                                        }

                                        @Override
                                        public void onOpenPage(OpenPageEvent event) {
                                            event.getInventoryCustomPagesContent().setItem(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilderMechanic(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName() , e -> e.setCancelled(true));
                                        }
                                    };
                                    invManager.open(player, 0);

                                })
                        )
                )
                .withSubcommands(new CommandAPICommand("reload")
                        .withPermission("sm.command.reload")
                        .executes((sender, args) -> {
                            try {
                                core.getManagers().getConfigManager().loadConfig();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            sender.sendMessage(AdventureUtils.deserializeLegacy("StorageMechanic reloaded!",null));
                        })
                )
                .withSubcommands(new CommandAPICommand("customblocks")
                        .withSubcommands(new CommandAPICommand("get")
                                .withPermission("sm.command.customblocks.get")
                                .withArguments(new StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {

                                    String[] ids = new String[core.getManagers().getCustomBlockManager().getAllCustomBlocks().size()];

                                    for(int i=0;i<core.getManagers().getCustomBlockManager().getAllCustomBlocks().size();i++){

                                       ids[i] = core.getManagers().getCustomBlockManager().getAllCustomBlocks().get(i).getId();

                                    }

                                    return ids;

                                })))
                                .withArguments(new IntegerArgument("amount"))
                                .executes((sender, args) -> {

                                    Player player = (Player) sender;
                                    int quantity = (int) args.get(1);
                                    String id = (String) args.get(0);
                                    CustomBlock customBlock = core.getManagers().getCustomBlockManager().getCustomBlockById(id);
                                    if(quantity<1||quantity>64) quantity = 64;
                                    if(customBlock != null){
                                        ItemStack itemStack = customBlock.getItemStack();
                                        itemStack.setAmount(quantity);
                                        StorageUtils.addItemToInventoryOrDrop(player,itemStack);
                                        AdventureUtils.playerMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.commands.custom_blocks.get.valid_id","you have received: " + id).replace("%id%",id), player);
                                        return;
                                    }
                                    AdventureUtils.playerMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.commands.custom_blocks.get.invalid_id","Invalid ID").replace("%id%",id), player);
                                })
                        )
                        .withSubcommands(new CommandAPICommand("give")
                                .withPermission("sm.command.customblocks.give")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
                                .withArguments(new StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {

                                    String[] ids = new String[core.getManagers().getCustomBlockManager().getAllCustomBlocks().size()];

                                    for(int i=0;i<core.getManagers().getCustomBlockManager().getAllCustomBlocks().size();i++){

                                        ids[i] = core.getManagers().getCustomBlockManager().getAllCustomBlocks().get(i).getId();

                                    }

                                    return ids;

                                })))
                                .withArguments(new IntegerArgument("amount"))
                                .executes((sender, args) -> {
                                    Collection<Player> players = (Collection<Player>) args.get(0);
                                    if(players.isEmpty()) return;
                                    int quantity = (int) args.get(2);
                                    String id = (String) args.get(1);
                                    CustomBlock customBlock = core.getManagers().getCustomBlockManager().getCustomBlockById(id);
                                    if(quantity<1||quantity>64) quantity = 64;
                                    if(customBlock != null){
                                        ItemStack itemStack = customBlock.getItemStack();
                                        itemStack.setAmount(quantity);
                                        for(Player player : players){
                                            StorageUtils.addItemToInventoryOrDrop(player,itemStack);
                                        }
                                        sender.sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.commands.custom_blocks.get.valid_id","you have received: " + id).replace("%id%",id),null));
                                        return;
                                    }

                                    sender.sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.commands.custom_blocks.get.invalid_id","Invalid ID").replace("%id%",id),null));
                                })
                        )
                )
                .withPermission("sm.command.main")
                .register();
    };

}
