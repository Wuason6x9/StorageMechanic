package dev.wuason.storagemechanic;

import dev.wuason.commandapi.CommandAPICommand;
import dev.wuason.commandapi.arguments.*;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.customblocks.CustomBlock;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

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
                                        AdventureUtils.playerMessage(core.getConfig().getString("messages.commands.custom_blocks.get.valid_id","you have received: " + id).replace("%id%",id), player);
                                        return;
                                    }
                                    AdventureUtils.playerMessage(core.getConfig().getString("messages.commands.custom_blocks.get.invalid_id","Invalid ID").replace("%id%",id), player);
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
                                        sender.sendMessage(AdventureUtils.deserializeLegacy(core.getConfig().getString("messages.commands.custom_blocks.get.valid_id","you have received: " + id).replace("%id%",id),null));
                                        return;
                                    }

                                    sender.sendMessage(AdventureUtils.deserializeLegacy(core.getConfig().getString("messages.commands.custom_blocks.get.invalid_id","Invalid ID").replace("%id%",id),null));
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
