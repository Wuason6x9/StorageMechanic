package dev.wuason.storagemechanic.items.properties;

import dev.wuason.storagemechanic.inventory.inventories.SearchItem.SearchType;

public class SearchItemProperties extends Properties {
    private String invId;
    private String invResultId;
    private SearchType searchType;

    public SearchItemProperties(String invId, String invResultId, SearchType searchType) {
        this.invId = invId;
        this.invResultId = invResultId;
        this.searchType = searchType;
    }

    public String getInvId() {
        return invId;
    }

    public String getInvResultId() {
        return invResultId;
    }

    public SearchType getSearchType() {
        return searchType;
    }
}
