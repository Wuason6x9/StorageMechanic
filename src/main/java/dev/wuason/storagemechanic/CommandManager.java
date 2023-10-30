package dev.wuason.storagemechanic;

import dev.wuason.commandapi.CommandAPICommand;
import dev.wuason.commandapi.arguments.*;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.customblocks.CustomBlock;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public class CommandManager {
    StorageMechanic core;
    CommandAPICommand command;
    public CommandManager(StorageMechanic core){
        this.core = core;
    }

    public void loadCommand(){

        command = new CommandAPICommand("StorageMechanic")
                .withPermission("sm.command")
                .withAliases("sm","storagem")
                .withSubcommands(new CommandAPICommand("debug")
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

                )
                .withSubcommands(new CommandAPICommand("reload")
                        .withPermission("sm.command.reload")
                        .executes((sender, args) -> {
                            core.getManagers().getConfigManager().loadConfig();
                            sender.sendMessage(AdventureUtils.deserializeLegacy("StorageMechanic reloaded!",null));
                        })
                )
                .withSubcommands(new CommandAPICommand("open")
                        .withPermission("sm.command.open")
                        .withArguments(new IntegerArgument("page"),new StringArgument("ID").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                            String[] ids = core.getManagers().getStorageManager().getStorageMap().keySet().toArray(new String[0]);
                            return ids;
                        })))
                        .executes((sender, args) -> {

                            Storage storage = core.getManagers().getStorageManager().getStorage((String)args.get(1));
                            storage.openStorage((Player)sender, (int)args.get(0));

                        })
                )
                .withSubcommands(new CommandAPICommand("customblocks")
                        .withPermission("sm.command.customblocks")
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
                                        AdventureUtils.playerMessage(core.getConfigDocumentYaml().getString("messages.commands.custom_blocks.get.valid_id","you have received: " + id).replace("%id%",id), player);
                                        return;
                                    }
                                    AdventureUtils.playerMessage(core.getConfigDocumentYaml().getString("messages.commands.custom_blocks.get.invalid_id","Invalid ID").replace("%id%",id), player);
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
                                        sender.sendMessage(AdventureUtils.deserializeLegacy(core.getConfigDocumentYaml().getString("messages.commands.custom_blocks.get.valid_id","you have received: " + id).replace("%id%",id),null));
                                        return;
                                    }

                                    sender.sendMessage(AdventureUtils.deserializeLegacy(core.getConfigDocumentYaml().getString("messages.commands.custom_blocks.get.invalid_id","Invalid ID").replace("%id%",id),null));
                                })
                        )
                )
                .withSubcommands(new CommandAPICommand("deleteTrash")
                        .withPermission("sm.command.deleteTrash")
                        .executes((sender, args) -> {

                            core.getManagers().getTrashSystemManager().cleanTrash();

                        })
                )
                .withPermission("sm.command.main");

        command.register();
    };

    public CommandAPICommand getCommand() {
        return command;
    }
}
