package com.quantium.mobile.framework;

import java.io.IOException;

public interface ToManyDAO {
    boolean add(Object obj) throws IOException;

    boolean remove(Object obj) throws IOException;
}
