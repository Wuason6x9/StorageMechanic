package dev.wuason.storagemechanic.actions.executors;

import dev.wuason.mechanics.actions.Action;
import dev.wuason.mechanics.actions.executators.Executor;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.types.api.StorageApiType;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class StorageExecutor extends Executor {
    public StorageExecutor() {
        super("storage", Storage.class);
    }

    @Override
    public void registerPlaceholders(Action action) {
        Storage storage = (Storage) action.getArgs()[0];

        action.registerPlaceholder("$storage$", storage);
        //storage id
        action.registerPlaceholder("$storageId$", storage.getId());
        //storage config id
        action.registerPlaceholder("$storageConfigId$", storage.getStorageConfig().getId());

        StorageOriginContext storageOriginContext = storage.getStorageOriginContext();

        switch (storageOriginContext.getContext()) {
            case BLOCK_STORAGE -> {
                BlockStorage blockStorage = ((StorageMechanic) action.getCore()).getManagers().getBlockStorageManager().getBlockStorage(storageOriginContext.getData().get(1));
                action.registerPlaceholder("$blockStorage$", blockStorage);
                action.registerPlaceholder("$blockStorageId$", blockStorage.getId());
                action.registerPlaceholder("$blockStorageConfigId$", storageOriginContext.getData().get(0));
            }
            case FURNITURE_STORAGE -> {
                FurnitureStorage furnitureStorage = ((StorageMechanic) action.getCore()).getManagers().getFurnitureStorageManager().getFurnitureStorage(storageOriginContext.getData().get(1));
                action.registerPlaceholder("$furnitureStorage$", furnitureStorage);
                action.registerPlaceholder("$furnitureStorageId$", furnitureStorage.getId());
                action.registerPlaceholder("$furnitureStorageConfigId$", storageOriginContext.getData().get(0));
            }
            case ITEM_STORAGE -> {
                action.registerPlaceholder("$itemStorageConfigId$", storageOriginContext.getData().get(0));
                action.registerPlaceholder("$itemStorageOwnerUUID$", storageOriginContext.getData().get(1));
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(storageOriginContext.getData().get(1)));
                action.registerPlaceholder("$itemStorageOwner$", offlinePlayer);
                action.registerPlaceholder("$itemStorageOwnerName$", offlinePlayer.getName());
            }
            case API -> {
                action.registerPlaceholder("apiStorageType", StorageApiType.valueOf(storageOriginContext.getData().get(2)));
                action.registerPlaceholder("$apiStorageId$", storageOriginContext.getData().get(0));
            }
        }

    }
}
