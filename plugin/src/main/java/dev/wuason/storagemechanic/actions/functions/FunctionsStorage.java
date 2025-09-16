package dev.wuason.storagemechanic.actions.functions;

import dev.wuason.libs.adapter.Adapter;
import dev.wuason.mechanics.actions.functions.Functions;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.PlayerUtils;
import dev.wuason.nms.wrappers.NMSManager;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class FunctionsStorage {
    public static void registerFunctions() {
        Functions.registerFunction(fBuilder -> {
            fBuilder.setName("ClearSlotInventory");
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(0, "slot", (s, action, objects) -> {
                            int slot = Integer.parseInt(s);
                            return slot;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });

                argBuilder.addArgument(1, "inventory", (s, action, objects) -> {
                            if (s == null) return action.getPlaceholder("$inventory$");
                            return "INVENTORY ERRROR";
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.setExecute((action, objects) -> {
                int slot = (int) objects[0];
                Inventory inventory = (Inventory) objects[1];
                inventory.clear(slot);
                return false;
            });

        });

        Functions.registerFunction(fBuilder -> {
            fBuilder.setName("SetItemInventory");
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(0, "slot", (s, action, objects) -> {
                            int slot = Integer.parseInt(s);
                            return slot;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(1, "item", (s, action, objects) -> {
                            if (s == null) return new ItemStack(Material.STONE);
                            return Adapter.getItemStack(s);
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(2, "inventory", (s, action, objects) -> {
                            if (s == null) return action.getPlaceholder("$inventory$");
                            return "INVENTORY ERRROR";
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.setExecute((action, objects) -> {
                int slot = (int) objects[0];
                Inventory inventory = (Inventory) objects[2];
                inventory.setItem(slot, (ItemStack) objects[1]);
                return false;
            });

        });

        Functions.registerFunction(fBuilder -> {
            fBuilder.setName("SetTitleInventory");
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(0, "title", (s, action, objects) -> {
                            return s;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(1, "toAll", (s, action, objects) -> {
                            if (s == null) return false;
                            return Boolean.parseBoolean(s);
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(2, "inventory", (s, action, objects) -> {
                            if (s == null) return action.getPlaceholder("$inventory$");
                            return "INVENTORY ERRROR";
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(3, "player", (s, action, objects) -> {
                            if (s == null) return action.getPlaceholder("$player$");
                            return PlayerUtils.getPlayer(s);
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.setExecute((action, objects) -> {
                String title = (String) objects[0];
                boolean toAll = (boolean) objects[1];
                Inventory inventory = (Inventory) objects[2];
                if (toAll == true) {
                    for (HumanEntity player : inventory.getViewers()) {
                        NMSManager.getVersionWrapper().updateCurrentInventoryTitle(AdventureUtils.deserializeJson(title, (Player) player), (Player) player);
                    }
                    return false;
                }
                Player player = (Player) objects[3];
                NMSManager.getVersionWrapper().updateCurrentInventoryTitle(AdventureUtils.deserializeJson(title, player), player);
                return false;
            });
        });

        Functions.registerFunction(fBuilder -> {
            fBuilder.setName("SetItemStorage");
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(0, "item", (s, action, objects) -> {
                            if (s == null) return new ItemStack(Material.STONE);
                            return Adapter.getItemStack(s);
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(1, "slot", (s, action, objects) -> {
                            int slot = Integer.parseInt(s);
                            return slot;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(2, "page", (s, action, objects) -> {
                            int page = Integer.parseInt(s);
                            return page;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(3, "storage", (s, action, objects) -> {
                            if (s == null) return action.getPlaceholder("$STORAGE$");
                            return StorageMechanic.getInstance().getManagers().getStorageManager().getStorage(s);
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.setExecute((action, objects) -> {
                ItemStack item = (ItemStack) objects[0];
                int slot = (int) objects[1];
                int page = (int) objects[2];
                Storage storage = (Storage) objects[3];
                storage.setItemInSlotPage(page, slot, item);
                return false;
            });
        });

        Functions.registerFunction(fBuilder -> {
            fBuilder.setName("ClearSlotPageStorage");
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(0, "slot", (s, action, objects) -> {
                            int slot = Integer.parseInt(s);
                            return slot;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(1, "page", (s, action, objects) -> {
                            int page = Integer.parseInt(s);
                            return page;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(2, "storage", (s, action, objects) -> {
                            if (s == null) return action.getPlaceholder("$STORAGE$");
                            return StorageMechanic.getInstance().getManagers().getStorageManager().getStorage(s);
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.setExecute((action, objects) -> {
                int slot = (int) objects[0];
                int page = (int) objects[1];
                Storage storage = (Storage) objects[2];
                storage.clearSlotPage(page, slot);
                return false;
            });
        });

        Functions.registerFunction(fBuilder -> {
            fBuilder.setName("ClearSlotPageStorageR");
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(0, "slot", (s, action, objects) -> {
                            int slot = Integer.parseInt(s);
                            return slot;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(1, "page", (s, action, objects) -> {
                            int page = Integer.parseInt(s);
                            return page;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(2, "storage", (s, action, objects) -> {
                            if (s == null) return action.getPlaceholder("$STORAGE$");
                            return StorageMechanic.getInstance().getManagers().getStorageManager().getStorage(s);
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.setExecute((action, objects) -> {
                int slot = (int) objects[0];
                int page = (int) objects[1];
                Storage storage = (Storage) objects[2];
                storage.clearSlotWithRestrictions(page, slot);
                return false;
            });
        });

        Functions.registerFunction(fBuilder -> {
            fBuilder.setName("SetStage");
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(0, "stage", (s, action, objects) -> {
                            return s;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(1, "page", (s, action, objects) -> {
                            int page = Integer.parseInt(s);
                            return page;
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(true);
                        });
            });
            fBuilder.addArguments(argBuilder -> {
                argBuilder.addArgument(2, "storage", (s, action, objects) -> {
                            if (s == null) return action.getPlaceholder("$STORAGE$");
                            return StorageMechanic.getInstance().getManagers().getStorageManager().getStorage(s);
                        },
                        propertiesBuilder -> {
                            propertiesBuilder.setAutoGetPlaceholder(true);
                            propertiesBuilder.setRequired(false);
                        });
            });
            fBuilder.setExecute((action, objects) -> {
                String stage = (String) objects[0];
                int page = (int) objects[1];
                Storage storage = (Storage) objects[2];
                if (!storage.existsStorageInventory(page)) return false;
                StorageInventory storageInventory = storage.getStorageInventory(page);
                storageInventory.setStage(stage);
                return false;
            });
        });


    }
}
