package dev.wuason.storagemechanic.storages;

public class WaitingInputData {
    private final Storage storage;
    private final int currentPage;

    public WaitingInputData(Storage storage, int currentPage) {
        this.storage = storage;
        this.currentPage = currentPage;
    }

    public Storage getStorage() {
        return storage;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
