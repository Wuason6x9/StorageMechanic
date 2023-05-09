package dev.wuason.storagemechanic;

import dev.jorel.commandapi.CommandAPICommand;
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
                .withSubcommands(new CommandAPICommand("open")
                        .executes((sender, args) -> {

                            Storage storage = new Storage("storage1");
                            storage.openStorage((Player)sender,0);

                        })
                )
                .withPermission("sm.command.main");

        command.register();
    };

    public CommandAPICommand getCommand() {
        return command;
    }
}
