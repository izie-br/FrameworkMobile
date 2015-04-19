package com.quantium.mobile.framework;

import com.quantium.mobile.framework.utils.JSONUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class MapToObjectIterator<T extends MapSerializable> implements Iterator<T> {

    private static final String ERR_PROTOTYPE_NULL = "DAO nao pode ser null";


    StreamJsonIterator jsonIterator;
    DAO<T> dao;

    public MapToObjectIterator(Reader reader, DAO<T> dao) {
        jsonIterator = new StreamJsonIterator(reader);
        this.dao = dao;
        if (dao == null)
            throw new RuntimeException(ERR_PROTOTYPE_NULL);
    }

    @Override
    public boolean hasNext() {
        return jsonIterator.hasNext();
    }

    @Override
    public T next() {
        try {
            return dao.mapToObject(
                    JSONUtils.desserializeJsonObject(jsonIterator.next()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        jsonIterator.remove();
    }


}
