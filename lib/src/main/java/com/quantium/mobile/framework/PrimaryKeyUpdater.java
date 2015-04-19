package com.quantium.mobile.framework;

import java.io.IOException;

public interface PrimaryKeyUpdater<T> {

    public void updatePrimaryKey(T target, Object newPrimaryKey) throws IOException;

}
