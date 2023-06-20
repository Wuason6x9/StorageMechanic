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

public class CommandManager {
    StorageMechanic core;
    CommandAPICommand command;
    public CommandManager(StorageMechanic core){
        this.core = core;
    }

    public void loadCommand(){

        command = new CommandAPICommand("StorageMechanic")
                .withAliases("sm","storagem")
                .withSubcommands(new CommandAPICommand("reload")
                        .withPermission("sm.command.reload")
                        .executes((sender, args) -> {
                            core.getManagers().getConfigManager().loadConfig();
                            sender.sendMessage(AdventureUtils.deserializeLegacy("StorageMechanic reloaded!"));
                        })
                )
                .withSubcommands(new CommandAPICommand("create")
                        .withPermission("sm.command.create")
                        .withArguments(new StringArgument("storageConfigID"))
                        .executes((sender, args) -> {
                            core.getManagers().getStorageManager().createStorage((String)args[0]);
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
                                .withArguments(new GreedyStringArgument("id"))
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
                .withSubcommands(new CommandAPICommand("test")

                        .executes((sender, args) -> {

                            Player player = (Player) sender;
                            PersistentDataContainer persistentDataContainer = player.getLocation().getChunk().getPersistentDataContainer();
                            persistentDataContainer.getKeys().forEach(namespacedKey -> persistentDataContainer.remove(namespacedKey));

                        })

                )
                .withPermission("sm.command.main");

        command.register();
    };

    public CommandAPICommand getCommand() {
        return command;
    }
}
