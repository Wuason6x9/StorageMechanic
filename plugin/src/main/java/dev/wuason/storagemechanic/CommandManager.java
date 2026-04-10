package dev.wuason.storagemechanic;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
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
import dev.wuason.mechanics.items.ItemBuilder;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.mechanics.utils.StorageUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.customitems.CustomItem;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.data.player.PlayerData;
import dev.wuason.storagemechanic.data.storage.StorageManagerData;
import dev.wuason.storagemechanic.data.storage.type.api.StorageApiData;
import dev.wuason.storagemechanic.data.storage.type.api.StorageApiManagerData;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.types.api.StorageApi;
import dev.wuason.storagemechanic.storages.types.api.StorageApiManager;
import dev.wuason.storagemechanic.storages.types.api.StorageApiType;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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

    public CommandManager(StorageMechanic core) {
        this.core = core;
    }

    private static final String[] COLORS = {"dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "blue", "green", "aqua", "red", "light_purple", "yellow"};

    public void loadCommand() {
        core.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("StorageMechanic")
                    .requires(source -> source.getSender().hasPermission("sm.command"));

            // ACTIONS
            LiteralArgumentBuilder<CommandSourceStack> actions = Commands.literal("actions")
                    .requires(source -> source.getSender().hasPermission("sm.command.actions"))
                    .then(Commands.literal("power")
                            .requires(source -> source.getSender().hasPermission("sm.command.actions.disableActionYML"))
                            .executes(ctx -> {
                                core.getManagers().getActionManager().setEnabled(!core.getManagers().getActionManager().isEnabled());
                                AdventureUtils.sendMessage(ctx.getSource().getSender(), "Power now: " + core.getManagers().getActionManager().isEnabled());
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .then(Commands.literal("status")
                            .requires(source -> source.getSender().hasPermission("sm.command.actions.status"))
                            .executes(ctx -> {
                                AdventureUtils.sendMessage(ctx.getSource().getSender(), "Power: " + core.getManagers().getActionManager().isEnabled());
                                return Command.SINGLE_SUCCESS;
                            })
                    );
            root.then(actions);

            // API
            LiteralArgumentBuilder<CommandSourceStack> api = Commands.literal("api")
                    .requires(source -> source.getSender().hasPermission("sm.command.api"));

            api.then(Commands.literal("create")
                    .requires(source -> source.getSender().hasPermission("sm.command.api.create"))
                    .then(Commands.argument("id", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                builder.suggest("MyCustomId_" + UUID.randomUUID().toString().split("-")[1]);
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("storageConfigId", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        core.getManagers().getStorageConfigManager().getStoragesConfigMap().keySet().forEach(builder::suggest);
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        String id = StringArgumentType.getString(ctx, "id");
                                        String storageConfigId = StringArgumentType.getString(ctx, "storageConfigId");
                                        core.getManagers().getStorageApiManager().createStorageApi(id, StorageApiType.COMMAND, storageConfigId);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            );

            api.then(Commands.literal("open")
                    .requires(source -> source.getSender().hasPermission("sm.command.api.open"))
                    .then(Commands.argument("id", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                core.getManagers().getStorageApiManager().getAllStoragesId().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("page", IntegerArgumentType.integer(0))
                                    .suggests((ctx, builder) -> {
                                        String storageApiId = StringArgumentType.getString(ctx, "id");
                                        if (!core.getManagers().getStorageApiManager().existStorageApi(storageApiId)) {
                                            builder.suggest("0");
                                            return builder.buildFuture();
                                        }
                                        StorageApi storageApi = core.getManagers().getStorageApiManager().getStorageApi(storageApiId);
                                        if (storageApi != null && storageApi.getStorage() != null) {
                                            IntStream.range(0, storageApi.getStorage().getTotalPages()).forEach(i -> builder.suggest(String.valueOf(i)));
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        String id = StringArgumentType.getString(ctx, "id");
                                        int page = IntegerArgumentType.getInteger(ctx, "page");
                                        Player player = (Player) ctx.getSource().getSender();
                                        Storage storage = core.getManagers().getStorageApiManager().getStorageApi(id).getStorage();
                                        if (storage != null) storage.openStorageR(player, page);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .executes(ctx -> {
                                                String id = StringArgumentType.getString(ctx, "id");
                                                int page = IntegerArgumentType.getInteger(ctx, "page");
                                                Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).get(0);
                                                Storage storage = core.getManagers().getStorageApiManager().getStorageApi(id).getStorage();
                                                if (storage != null) storage.openStorageR(player, page);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                    )
            );

            api.then(Commands.literal("delete")
                    .requires(source -> source.getSender().hasPermission("sm.command.api.delete"))
                    .then(Commands.argument("id", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                core.getManagers().getStorageApiManager().getAllStoragesId().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                core.getManagers().getStorageApiManager().removeStorageApi(id);
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );

            api.then(Commands.literal("list")
                    .requires(source -> source.getSender().hasPermission("sm.command.api.list"))
                    .executes(ctx -> {
                        StringBuilder list = new StringBuilder();
                        for (String id : core.getManagers().getStorageApiManager().getAllStoragesId()) {
                            list.append("<").append(COLORS[MathUtils.randomNumber(0, COLORS.length - 1)]).append("><click:run_command:/sm api open ").append(id).append(" 0>").append(id).append("</click><reset>, ");
                        }
                        AdventureUtils.sendMessage(ctx.getSource().getSender(), list.toString());
                        return Command.SINGLE_SUCCESS;
                    })
            );

            api.then(Commands.literal("exist")
                    .requires(source -> source.getSender().hasPermission("sm.command.api.exist"))
                    .then(Commands.argument("id", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                core.getManagers().getStorageApiManager().getAllStoragesId().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                AdventureUtils.sendMessage(ctx.getSource().getSender(), String.valueOf(core.getManagers().getStorageApiManager().existStorageApi(id)));
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );

            api.then(Commands.literal("createIfNotExistAndOpen")
                    .requires(source -> source.getSender().hasPermission("sm.command.api.createIfNotExistAndOpen"))
                    .then(Commands.argument("id", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                builder.suggest("MyCustomId_" + UUID.randomUUID().toString().split("-")[1]);
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("storageConfigId", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        core.getManagers().getStorageConfigManager().getStoragesConfigMap().keySet().forEach(builder::suggest);
                                        return builder.buildFuture();
                                    })
                                    .then(Commands.argument("page", IntegerArgumentType.integer(0))
                                            .suggests((ctx, builder) -> {
                                                String storageConfigId = StringArgumentType.getString(ctx, "storageConfigId");
                                                if (!core.getManagers().getStorageConfigManager().existsStorageConfig(storageConfigId)) {
                                                    builder.suggest("0");
                                                    return builder.buildFuture();
                                                }
                                                StorageConfig storageConfig = core.getManagers().getStorageConfigManager().getStorageConfigById(storageConfigId);
                                                IntStream.range(0, storageConfig.getPages()).forEach(i -> builder.suggest(String.valueOf(i)));
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                String id = StringArgumentType.getString(ctx, "id");
                                                String storageConfigId = StringArgumentType.getString(ctx, "storageConfigId");
                                                int page = IntegerArgumentType.getInteger(ctx, "page");
                                                Player player = (Player) ctx.getSource().getSender();
                                                Storage storage = null;
                                                if (!core.getManagers().getStorageApiManager().existStorageApi(id)) {
                                                    storage = core.getManagers().getStorageApiManager().createStorageApi(id, StorageApiType.COMMAND, storageConfigId).getStorage();
                                                }
                                                if (storage == null) {
                                                    storage = core.getManagers().getStorageApiManager().getStorageApi(id).getStorage();
                                                }
                                                if (storage != null) storage.openStorageR(player, page);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .executes(ctx -> {
                                                        String id = StringArgumentType.getString(ctx, "id");
                                                        String storageConfigId = StringArgumentType.getString(ctx, "storageConfigId");
                                                        int page = IntegerArgumentType.getInteger(ctx, "page");
                                                        Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).get(0);
                                                        Storage storage = null;
                                                        if (!core.getManagers().getStorageApiManager().existStorageApi(id)) {
                                                            storage = core.getManagers().getStorageApiManager().createStorageApi(id, StorageApiType.COMMAND, storageConfigId).getStorage();
                                                        }
                                                        if (storage == null) {
                                                            storage = core.getManagers().getStorageApiManager().getStorageApi(id).getStorage();
                                                        }
                                                        if (storage != null) storage.openStorageR(player, page);
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                    )
            );

            api.then(Commands.literal("saveAndUnload")
                    .requires(source -> source.getSender().hasPermission("sm.command.api.saveAndUnload"))
                    .then(Commands.argument("id", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                core.getManagers().getStorageApiManager().getStorageApis().keySet().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                core.getManagers().getStorageApiManager().saveStorageApi(id, SaveCause.NORMAL_SAVE);
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );

            api.then(Commands.literal("panel")
                    .requires(source -> source.getSender().hasPermission("sm.command.api.panel"))
                    .executes(ctx -> {
                        Player player = (Player) ctx.getSource().getSender();

                        StorageManager storageManager = core.getManagers().getStorageManager();
                        StorageManagerData storageManagerData = core.getManagers().getDataManager().getStorageManagerData();
                        StorageApiManager storageApiManager = core.getManagers().getStorageApiManager();
                        StorageApiManagerData storageApiManagerData = core.getManagers().getDataManager().getStorageManagerData().getStorageApiManager();

                        InvCustomPagesContentManager<String> invManager = new InvCustomPagesContentManager<String>(
                                IntStream.rangeClosed(0, 44).boxed().toList(),
                                new PreviousPageItem(45, new ItemBuilder(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("api.panel.previous_page", "INCORRECT")).build()),
                                new NextPageItem(53, new ItemBuilder(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("api.panel.next_page", "INCORRECT")).build()),
                                (inv, page) -> {
                                    HashMap<String, String> replaces = new HashMap<>() {{
                                        put("%page%", page + 1 + "");
                                        put("%max_page%", inv.getMaxPage() + 1 + "");
                                    }};

                                    return new InvCustomPagesContent(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("api.panel.title", "untitled"), replaces), player), 54, inv, page) {
                                        @Override
                                        public void onClick(InventoryClickEvent event) {
                                            event.setCancelled(true);
                                        }

                                        @Override
                                        public void onDrag(InventoryDragEvent event) {
                                            event.setCancelled(true);
                                        }
                                    };
                                },
                                core.getManagers().getStorageApiManager().getAllStoragesId().stream().toList()
                        ) {

                            @Override
                            public ItemStack onContentPage(int page, int slot, String content) {
                                StorageApiData storageApiData = null;
                                if (storageApiManager.getStorageApis().containsKey(content)) {
                                    storageApiData = storageApiManagerData.StorageApiToStorageApiData(storageApiManager.getStorageApis().get(content));
                                } else {
                                    storageApiData = storageApiManagerData.getStorageApiData(content);
                                }
                                Storage storage = null;
                                if (storageManager.getStorageMap().containsKey(storageApiData.getStorageId())) {
                                    storage = storageManager.getStorageMap().get(storageApiData.getStorageId());
                                } else {
                                    storage = storageManagerData.loadStorageData(storageApiData.getStorageId());
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


                                return new ItemBuilder(Material.CHEST).setName(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("api.panel.item_name", "untitled"), replaces), player))
                                        .setLore(AdventureUtils.deserializeLegacyList(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getStringList("api.panel.item_lore", new ArrayList<>()), replaces), player))
                                        .build();
                            }

                            @Override
                            public void onContentClick(ContentClickEvent event) {
                                if (event.getEvent().isShiftClick()) {
                                    if (event.getEvent().isLeftClick()) {
                                        setContent(event.getInventoryCustomPagesContent().getPage());
                                        event.getInventoryCustomPagesContent().setSimpleItems(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName());
                                    }
                                    if (event.getEvent().isRightClick()) {
                                        core.getManagers().getStorageApiManager().removeStorageApi((String) event.getContent());
                                        removeContent((String) event.getContent());
                                        setContent(event.getInventoryCustomPagesContent().getPage());
                                        event.getInventoryCustomPagesContent().setSimpleItems(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName());
                                    }
                                } else {
                                    if (event.getEvent().isRightClick()) {
                                        if (core.getManagers().getStorageApiManager().getStorageApis().containsKey((String) event.getContent())) {
                                            core.getManagers().getStorageApiManager().saveStorageApi((String) event.getContent(), SaveCause.NORMAL_SAVE);
                                            setContent(event.getInventoryCustomPagesContent().getPage());
                                            event.getInventoryCustomPagesContent().setSimpleItems(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName());
                                        }
                                    } else if (event.getEvent().isLeftClick()) {
                                        core.getManagers().getStorageApiManager().getStorageApi((String) event.getContent()).getStorage().openStorageR(player, 0);
                                    }
                                }
                            }

                            @Override
                            public void onOpenPage(OpenPageEvent event) {
                                event.getInventoryCustomPagesContent().setItem(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName(), e -> e.setCancelled(true));
                            }
                        };

                        invManager.open(player, 0);
                        return Command.SINGLE_SUCCESS;
                    })
            );

            root.then(api);

            // DEBUG
            LiteralArgumentBuilder<CommandSourceStack> debug = Commands.literal("debug")
                    .requires(source -> source.getSender().hasPermission("sm.command.debug"));

            debug.then(Commands.literal("test")
                    .executes(ctx -> {
                        Player player = (Player) ctx.getSource().getSender();
                        InvCustomPagesAnvil<CustomStack> invCustomPagesAnvil = new InvCustomPagesAnvil<CustomStack>(
                                "Test",
                                player,
                                IntStream.rangeClosed(9, 35).boxed().toList(),
                                ItemsAdder.getAllItems(),
                                new dev.wuason.libs.invmechaniclib.types.pages.content.anvil.items.PreviousPageItem(0, new ItemBuilder(Material.ARROW).setNameWithMiniMessage("<red><< Previous page").build()),
                                new dev.wuason.libs.invmechaniclib.types.pages.content.anvil.items.NextPageItem(8, new ItemBuilder(Material.ARROW).setNameWithMiniMessage("<red>Next page >>").build()),
                                new ItemStack(Material.BARRIER)
                        ) {
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
                                ItemInterface itemInterface = new ItemInterface(1, new ItemBuilder(Material.COMPASS).setNameWithMiniMessage("<red>Search").build()) {

                                    @Override
                                    public void onClick(InventoryClickEvent event, InvCustom inventoryCustom) {
                                        setActualPage(0);
                                        search();
                                        setContent(getActualPage());
                                        setButtonsPage(getActualPage());
                                    }
                                };

                                regAndSetInvItemInterface(itemInterface);

                            }

                        };

                        invCustomPagesAnvil.openSimple();
                        return Command.SINGLE_SUCCESS;
                    })
            );

            debug.then(Commands.literal("ActiveHopperHashMap")
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        AdventureUtils.sendMessage(sender, "<blue>" + BlockMechanicManager.HOPPER_BLOCK_MECHANIC.getHopperActiveHashMap().toString());
                        AdventureUtils.sendMessage(sender, "<red>--------------------------------------------------");
                        AdventureUtils.sendMessage(sender, "<green>" + BlockMechanicManager.HOPPER_BLOCK_MECHANIC.getHopperUUIDCurrentActiveHashMap().toString());
                        AdventureUtils.sendMessage(sender, "<red>--------------------------------------------------");
                        AdventureUtils.sendMessage(sender, "<blue>Size: " + BlockMechanicManager.HOPPER_BLOCK_MECHANIC.getHopperActiveHashMap().size());
                        AdventureUtils.sendMessage(sender, "<gold>Size: " + BlockMechanicManager.HOPPER_BLOCK_MECHANIC.getHopperUUIDCurrentActiveHashMap().size());
                        return Command.SINGLE_SUCCESS;
                    })
            );

            debug.then(Commands.literal("getActiveBlockStorages")
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        AdventureUtils.sendMessage(sender, "<blue>" + core.getManagers().getBlockStorageManager().getAllBlockStorages());
                        AdventureUtils.sendMessage(sender, "<red>--------------------------------------------------");
                        AdventureUtils.sendMessage(sender, "<blue>Size: " + core.getManagers().getBlockStorageManager().getAllBlockStorages().size());
                        return Command.SINGLE_SUCCESS;
                    })
            );

            debug.then(Commands.literal("saveAllData")
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
                            AdventureUtils.sendMessage(sender, "<red>Saving all data!");
                            core.getManagers().saveAllData();
                        });
                        return Command.SINGLE_SUCCESS;
                    })
            );

            debug.then(Commands.literal("enable")
                    .executes(ctx -> {
                        Player player = (Player) ctx.getSource().getSender();
                        core.getDebug().enableDebugMode(player);
                        return Command.SINGLE_SUCCESS;
                    })
            );

            debug.then(Commands.literal("disable")
                    .executes(ctx -> {
                        Player player = (Player) ctx.getSource().getSender();
                        core.getDebug().disableDebugMode(player);
                        return Command.SINGLE_SUCCESS;
                    })
            );

            debug.then(Commands.literal("open")
                    .then(Commands.argument("page", IntegerArgumentType.integer(0))
                            .then(Commands.argument("ID", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        core.getManagers().getStorageManager().getStorageMap().keySet().forEach(builder::suggest);
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        int page = IntegerArgumentType.getInteger(ctx, "page");
                                        String id = StringArgumentType.getString(ctx, "ID");
                                        Storage storage = core.getManagers().getStorageManager().getStorage(id);
                                        if (storage != null) {
                                            storage.openStorageR((Player) ctx.getSource().getSender(), page);
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            );

            debug.then(Commands.literal("deleteTrash")
                    .executes(ctx -> {
                        core.getManagers().getTrashSystemManager().cleanTrash();
                        return Command.SINGLE_SUCCESS;
                    })
            );

            root.then(debug);

            // INFO
            LiteralArgumentBuilder<CommandSourceStack> info = Commands.literal("info");

            info.then(Commands.literal("players")
                    .requires(source -> source.getSender().hasPermission("sm.command.info.players"))
                    .executes(ctx -> {
                        Player player = (Player) ctx.getSource().getSender();

                        StorageManager storageManager = core.getManagers().getStorageManager();
                        StorageManagerData storageManagerData = core.getManagers().getDataManager().getStorageManagerData();

                        PageCustomInfo<Player> pagePlayers = new PageCustomInfo<Player>(Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39),
                                new dev.wuason.libs.invmechaniclib.types.pages.content.multiple.items.PreviousPageItem(45, new ItemBuilder(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.previous_page", "INCORRECT")).build()),
                                new dev.wuason.libs.invmechaniclib.types.pages.content.multiple.items.NextPageItem(48, new ItemBuilder(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.next_page", "INCORRECT")).build())
                        );

                        pagePlayers.setContentList(new ArrayList<>(Bukkit.getOnlinePlayers()));

                        PageCustomInfo<String> pageStorages = new PageCustomInfo<String>(Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44),
                                new dev.wuason.libs.invmechaniclib.types.pages.content.multiple.items.PreviousPageItem(50, new ItemBuilder(Material.ARROW).setNameWithMiniMessage("<red>Previous page").build()),
                                new dev.wuason.libs.invmechaniclib.types.pages.content.multiple.items.NextPageItem(53, new ItemBuilder(Material.ARROW).setNameWithMiniMessage("<red>Next page").build())
                        );

                        InvCustomPagesContentMultiple invMultiple = new InvCustomPagesContentMultiple(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.title", "UNTITLED"), player), 54) {

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

                                if (content.getPageCustomInfo().equals(pagePlayers)) {

                                    PlayerData objectiveData = core.getManagers().getDataManager().getPlayerDataManager().getPlayerData(((Player) content.getContent()).getUniqueId());
                                    Map<String, String> replaces = new HashMap<>();
                                    replaces.put("%player%", ((Player) content.getContent()).getName());
                                    if (objectiveData != null) {
                                        replaces.put("%total_storages%", objectiveData.getStorages().size() + "");
                                        replaces.put("%total_block_storages%", objectiveData.getBlockStorages().size() + "");
                                        replaces.put("%total_furniture_storages%", objectiveData.getFurnitureStorages().size() + "");
                                    }
                                    return new ItemBuilder(Material.PLAYER_HEAD).setNameWithMiniMessage(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.item_name", "INCORRECT"), replaces)).setSkullOwner((Player) content.getContent()).setLore(AdventureUtils.deserializeLegacyList(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getStringList("info.players.players.item_lore", new ArrayList<>()), replaces), (Player) content.getContent())).build();

                                }

                                if (content.getPageCustomInfo().equals(pageStorages)) {

                                    String storageId = (String) content.getContent();

                                    Storage storage = null;
                                    if (storageManager.getStorageMap().containsKey(storageId)) {
                                        storage = storageManager.getStorageMap().get(storageId);
                                    } else {
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

                                    return new ItemBuilder(Material.CHEST)
                                            .setName(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.storages.item_name", "INCORRECT"), replaces), selectedPlayer))
                                            .setLore(AdventureUtils.deserializeLegacyList(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getStringList("info.players.storages.item_lore", new ArrayList<>()), replaces), selectedPlayer))
                                            .build();
                                }

                                return null;
                            }

                            @Override
                            public void onContentClick(ContentMultipleClickEvent event) {

                                if (event.getPageCustomInfo().equals(pagePlayers)) {
                                    Player p = (Player) event.getContent();
                                    PlayerData playerData = core.getManagers().getDataManager().getPlayerDataManager().getPlayerData(p.getUniqueId());
                                    if (playerData == null) return;
                                    List<String> storages = playerData.getStorages().keySet().stream().toList();
                                    pageStorages.setContentList(storages);
                                    setItemsSelected();
                                    selectedPlayer = p;
                                    setContentAndButtons(pageStorages);

                                }
                                if (event.getPageCustomInfo().equals(pageStorages)) {
                                    if (event.getEvent().isShiftClick()) {
                                        if (event.getEvent().isRightClick()) {
                                            core.getManagers().getStorageManager().getStorage((String) event.getContent()).removeAllItems();
                                            setContentPage(event.getPageCustomInfo());
                                        }
                                    } else {
                                        if (event.getEvent().isRightClick()) {
                                            if (core.getManagers().getStorageManager().getStorageMap().containsKey((String) event.getContent())) {
                                                core.getManagers().getStorageManager().saveStorage(core.getManagers().getStorageManager().getStorageMap().get((String) event.getContent()), SaveCause.NORMAL_SAVE);
                                                setContentPage(event.getPageCustomInfo());
                                            }
                                        } else if (event.getEvent().isLeftClick()) {
                                            core.getManagers().getStorageManager().getStorage((String) event.getContent()).openStorageR(player, 0);
                                        }
                                    }
                                }
                            }

                            public Player getSelectedPlayer() {
                                return selectedPlayer;
                            }

                            public void setItemsNotSelected() {
                                setSimpleItems(new int[]{4, 13, 22, 31, 40, 49}, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).buildWithVoidName());
                            }

                            public void setItemsSelected() {
                                setSimpleItems(new int[]{4, 13, 22, 31, 40, 49}, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName());
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
                                setItem(46, new ItemBuilder(Material.COMPASS).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.refresh_item_name", "INCORRECT")).build(), e -> {
                                    pagePlayers.firstPage();
                                    setContentAndButtons(pagePlayers);
                                    if (selectedPlayer != null) setItemsSelected();
                                    else setItemsNotSelected();
                                });
                                setItem(51, new ItemBuilder(Material.COMPASS).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.storages.refresh_item_name", "INCORRECT")).build(), e -> {
                                    if (selectedPlayer == null) return;
                                    setItemsSelected();
                                    pageStorages.firstPage();
                                    setContentAndButtons(pageStorages);
                                });
                                setItem(47, new ItemBuilder(Material.BARRIER).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.players.players.remove_selection_item_name", "INCORRECT")).build(), e -> {
                                    if (selectedPlayer == null) return;
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

                        return Command.SINGLE_SUCCESS;
                    })
            );

            info.then(Commands.literal("player")
                    .requires(source -> source.getSender().hasPermission("sm.command.info.player"))
                    .then(Commands.argument("player", ArgumentTypes.player())
                            .executes(ctx -> {
                                Player objective = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).get(0);
                                Player player = (Player) ctx.getSource().getSender();

                                PlayerData playerData = core.getManagers().getDataManager().getPlayerDataManager().getPlayerData(objective.getUniqueId());
                                if (playerData == null) return Command.SINGLE_SUCCESS;

                                Set<String> storages = playerData.getStorages().keySet();
                                if (storages == null) return Command.SINGLE_SUCCESS;

                                InvCustomPagesContentManager<String> invManager = new InvCustomPagesContentManager<String>(IntStream.rangeClosed(0, 44).boxed().toList(), new PreviousPageItem(45, new ItemBuilder(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.player.previous_page", "INCORRECT")).build()), new NextPageItem(53, new ItemBuilder(Material.ARROW).setNameWithMiniMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.player.next_page", "INCORRECT")).build()), (inv, page) ->
                                {
                                    HashMap<String, String> replaces = new HashMap<>() {{
                                        put("%player%", objective.getName());
                                        put("%page%", page + 1 + "");
                                        put("%max_page%", inv.getMaxPage() + 1 + "");
                                    }};

                                    return new InvCustomPagesContent(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.player.title", "untitled"), replaces), objective), 54, inv, page);

                                }, storages.stream().toList()) {


                                    @Override
                                    public ItemStack onContentPage(int page, int slot, String content) {
                                        StorageManager storageManager = core.getManagers().getStorageManager();
                                        StorageManagerData storageManagerData = core.getManagers().getDataManager().getStorageManagerData();

                                        Storage storage = null;
                                        if (storageManager.getStorageMap().containsKey(content)) {
                                            storage = storageManager.getStorageMap().get(content);
                                        } else {
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


                                        return new ItemBuilder(Material.CHEST).setName(AdventureUtils.deserializeLegacy(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getString("info.player.item_name", "untitled"), replaces), objective))
                                                .setLore(AdventureUtils.deserializeLegacyList(Utils.replaceVariablesInsensitive(core.getManagers().getConfigManager().getLangDocumentYaml().getStringList("info.player.lore", new ArrayList<>()), replaces), objective))
                                                .build();
                                    }

                                    @Override
                                    public void onContentClick(ContentClickEvent event) {

                                        if (event.getEvent().isShiftClick()) {
                                            if (event.getEvent().isLeftClick()) {
                                                setContent(event.getInventoryCustomPagesContent().getPage());
                                                event.getInventoryCustomPagesContent().setItem(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName(), e -> e.setCancelled(true));
                                            }
                                            if (event.getEvent().isRightClick()) {
                                                core.getManagers().getStorageManager().getStorage((String) event.getContent()).removeAllItems();
                                                setContent(event.getInventoryCustomPagesContent().getPage());
                                            }
                                        } else {
                                            if (event.getEvent().isRightClick()) {
                                                if (core.getManagers().getStorageManager().getStorageMap().containsKey((String) event.getContent())) {
                                                    core.getManagers().getStorageManager().saveStorage(core.getManagers().getStorageManager().getStorageMap().get((String) event.getContent()), SaveCause.NORMAL_SAVE);
                                                    setContent(event.getInventoryCustomPagesContent().getPage());
                                                    event.getInventoryCustomPagesContent().setItem(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName(), e -> e.setCancelled(true));
                                                }
                                            } else if (event.getEvent().isLeftClick()) {
                                                core.getManagers().getStorageManager().getStorage((String) event.getContent()).openStorageR(player, 0);
                                            }
                                        }

                                    }

                                    @Override
                                    public void onOpenPage(OpenPageEvent event) {
                                        event.getInventoryCustomPagesContent().setItem(event.getInventoryCustomPagesContent().getSlotsFree(), new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).buildWithVoidName(), e -> e.setCancelled(true));
                                    }
                                };
                                invManager.open(player, 0);

                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );

            root.then(info);

            // RELOAD
            root.then(Commands.literal("reload")
                    .requires(source -> source.getSender().hasPermission("sm.command.reload"))
                    .executes(ctx -> {
                        try {
                            core.getManagers().getConfigManager().loadConfig();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        ctx.getSource().getSender().sendMessage(AdventureUtils.deserializeLegacy("StorageMechanic reloaded!", null));
                        return Command.SINGLE_SUCCESS;
                    })
            );

            // CUSTOMITEMS
            LiteralArgumentBuilder<CommandSourceStack> customItems = Commands.literal("customItems");

            customItems.then(Commands.literal("get")
                    .requires(source -> source.getSender().hasPermission("sm.command.customitems.get"))
                    .then(Commands.argument("id", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                core.getManagers().getCustomItemsManager().getAllCustomItems().forEach(item -> builder.suggest(item.getId()));
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                executeCustomItemsGet((Player) ctx.getSource().getSender(), id, 1);
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(ctx -> {
                                        String id = StringArgumentType.getString(ctx, "id");
                                        int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                        executeCustomItemsGet((Player) ctx.getSource().getSender(), id, amount);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            );

            customItems.then(Commands.literal("give")
                    .requires(source -> source.getSender().hasPermission("sm.command.customitems.give"))
                    .then(Commands.argument("player", ArgumentTypes.players())
                            .then(Commands.argument("id", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        core.getManagers().getCustomItemsManager().getAllCustomItems().forEach(item -> builder.suggest(item.getId()));
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        String id = StringArgumentType.getString(ctx, "id");
                                        Collection<Player> players = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                                        executeCustomItemsGive(ctx.getSource().getSender(), players, id, 1);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {
                                                String id = StringArgumentType.getString(ctx, "id");
                                                int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                Collection<Player> players = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                                                executeCustomItemsGive(ctx.getSource().getSender(), players, id, amount);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                    )
            );

            root.then(customItems);


            // REGISTER
            commands.register(root.build(), "StorageMechanic Command", List.of("sm", "storagem"));
        });
    }

    private void executeCustomItemsGet(Player player, String id, int quantity) {
        CustomItem customItem = core.getManagers().getCustomItemsManager().getCustomItemById(id);
        if (quantity < 1 || quantity > 64) quantity = 64;
        if (customItem != null) {
            ItemStack itemStack = customItem.getItemStack();
            itemStack.setAmount(quantity);
            StorageUtils.addItemToInventoryOrDrop(player, itemStack);
            AdventureUtils.playerMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.commands.custom_blocks.get.valid_id", "you have received: " + id).replace("%id%", id), player);
            return;
        }
        AdventureUtils.playerMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.commands.custom_blocks.get.invalid_id", "Invalid ID").replace("%id%", id), player);
    }

    private void executeCustomItemsGive(CommandSender sender, Collection<Player> players, String id, int quantity) {
        if (players.isEmpty()) return;
        CustomItem customItem = core.getManagers().getCustomItemsManager().getCustomItemById(id);
        if (quantity < 1 || quantity > 64) quantity = 64;
        if (customItem != null) {
            ItemStack itemStack = customItem.getItemStack();
            itemStack.setAmount(quantity);
            for (Player player : players) {
                StorageUtils.addItemToInventoryOrDrop(player, itemStack);
            }
            sender.sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.commands.custom_blocks.get.valid_id", "you have received: " + id).replace("%id%", id), null));
            return;
        }
        sender.sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.commands.custom_blocks.get.invalid_id", "Invalid ID").replace("%id%", id), null));
    }
}
