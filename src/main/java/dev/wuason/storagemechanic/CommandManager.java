package dev.wuason.storagemechanic;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.storages.Storage;
import org.bukkit.entity.Player;

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
                        .withArguments(new StringArgument("storageConfigID"))
                        .executes((sender, args) -> {
                            core.getManagers().getStorageManager().createStorage((String)args[0]);
                        })
                )
                .withSubcommands(new CommandAPICommand("open")
                        .withArguments(new IntegerArgument("page"),new StringArgument("ID").replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                            String[] ids = core.getManagers().getStorageManager().getStorageMap().keySet().toArray(new String[0]);
                            return ids;
                        })))
                        .executes((sender, args) -> {

                            Storage storage = core.getManagers().getStorageManager().getStorage((String)args[1]);
                            storage.openStorage((Player)sender, (int)args[0]);

                        })
                )
                .withPermission("sm.command.main");

        command.register();
    };

    public CommandAPICommand getCommand() {
        return command;
    }
}
