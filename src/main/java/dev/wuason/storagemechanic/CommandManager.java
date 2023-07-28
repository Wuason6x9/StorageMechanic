package dev.wuason.storagemechanic;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.customblocks.CustomBlock;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;

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
                            sender.sendMessage(AdventureUtils.deserializeLegacy("StorageMechanic reloaded!"));
                        })
                )
                .withSubcommands(new CommandAPICommand("open")
                        .withPermission("sm.command.open")
                        .withArguments(new IntegerArgument("page"),new StringArgument("ID").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                            String[] ids = core.getManagers().getStorageManager().getStorageMap().keySet().toArray(new String[0]);
                            return ids;
                        })))
                        .executes((sender, args) -> {

                            Storage storage = core.getManagers().getStorageManager().getStorage((String)args[1]);
                            storage.openStorage((Player)sender, (int)args[0]);

                        })
                )
                .withSubcommands(new CommandAPICommand("customblocks")
                        .withPermission("sm.command.customblocks")
                        .withSubcommands(new CommandAPICommand("get")
                                .withPermission("sm.command.customblocks.get")
                                .withArguments(new IntegerArgument("quantity"))
                                .withArguments(new GreedyStringArgument("id").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {

                                    String[] ids = new String[core.getManagers().getCustomBlockManager().getAllCustomBlocks().size()];

                                    for(int i=0;i<core.getManagers().getCustomBlockManager().getAllCustomBlocks().size();i++){

                                       ids[i] = core.getManagers().getCustomBlockManager().getAllCustomBlocks().get(i).getId();

                                    }

                                    return ids;

                                })))
                                .executes((sender, args) -> {

                                    Player player = (Player) sender;
                                    int quantity = (int) args[0];
                                    String id = (String) args[1];

                                    CustomBlock customBlock = core.getManagers().getCustomBlockManager().getCustomBlockById(id);

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
                )
                .withSubcommands(new CommandAPICommand("deleteTrash")
                        .withPermission("sm.command.deleteTrash")
                        .executes((sender, args) -> {

                            core.getManagers().getTrashSystemManager().cleanTrash();

                        })
                )
                .withSubcommands(new CommandAPICommand("test")

                        .executes((sender, args) -> {

                            Player player = (Player) sender;
                            PersistentDataContainer persistentDataContainer = player.getLocation().getChunk().getPersistentDataContainer();
                            player.sendMessage(persistentDataContainer.getKeys().stream().toList().toString());

                        })

                )
                .withPermission("sm.command.main");

        command.register();
    };

    public CommandAPICommand getCommand() {
        return command;
    }
}
