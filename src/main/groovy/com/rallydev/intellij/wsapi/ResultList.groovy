package com.rallydev.intellij.wsapi

import com.rallydev.intellij.wsapi.domain.DomainObject

interface ResultList<T extends DomainObject> extends List<T> {
    static int MAX_PAGES_TO_LOAD = 20

    Boolean getHasMorePages()
    void loadAllPages()
    void loadNextPage()

}
