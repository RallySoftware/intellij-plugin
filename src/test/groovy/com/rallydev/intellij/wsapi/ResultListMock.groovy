package com.rallydev.intellij.wsapi

import com.rallydev.intellij.wsapi.domain.DomainObject

class ResultListMock<T extends DomainObject> extends ArrayList<T> implements ResultList<T> {

    ResultListMock() { }

    ResultListMock(Collection<T> other) {
        addAll(other)
    }

    @Override
    Boolean getHasMorePages() {
        false
    }

    @Override
    void loadAllPages() {}

    @Override
    void loadNextPage() {}

}
