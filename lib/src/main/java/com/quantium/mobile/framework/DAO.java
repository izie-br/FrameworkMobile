package com.quantium.mobile.framework;

import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.ValidationError;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface DAO<T> extends Serializable {
    QuerySet<T> query();

    QuerySet<T> query(Q q);

    boolean save(T obj) throws IOException;

    boolean save(T obj, int flags) throws IOException;

    boolean delete(T obj) throws IOException;

    ToManyDAO with(T obj);

    T mapToObject(Map<String, Object> map) throws IOException;

    void updateWithMap(T target, Map<String, Object> map) throws IOException;

    Collection<ValidationError> validate(T obj) throws IOException;

    T get(Object id) throws IOException;

    Table getTable();
}
