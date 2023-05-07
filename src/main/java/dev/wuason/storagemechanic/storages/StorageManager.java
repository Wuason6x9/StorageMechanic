package dev.wuason.storagemechanic.storages;

public class StorageManager {

    public Storage StorageSerializableToStorage(StorageSerializable storageSerializable){
        return new Storage(storageSerializable.getId(),storageSerializable.getItems(),storageSerializable.getStorageIdConfig());
    }
    public StorageSerializable StorageToStorageSerializable(Storage storage){
        return new StorageSerializable(storage.getItems(),storage.getId(),storage.getStorageIdConfig());
    }


}
