package com.quantium.mobile.framework.libandroidtest;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.ToManyDAO;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.validation.ValidationError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class UserMapDAO implements DAO<User> {

    @Override
    public boolean save(User target) throws IOException {
        return save(target, Save.INSERT_IF_NULL_PK);
    }

    @Override
    public boolean save(User target, int flags) throws IOException {
        throw new UnsupportedOperationException();
    }

    public QuerySet<User> query() {
        return query(null);
    }

    public QuerySet<User> query(Q q) {
        throw new UnsupportedOperationException();
    }

    public boolean delete(User target) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public User get(Object id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User mapToObject(Map<String, Object> map)
            throws ClassCastException {
        CamelCaseUtils.AnyCamelMap<Object> mapAnyCamelCase =
                new CamelCaseUtils.AnyCamelMap<Object>();
        mapAnyCamelCase.putAll(map);
        Object temp;
        temp = mapAnyCamelCase.get("id");
        long _id = ((temp != null) ? ((Number) temp).longValue() : 0);
        temp = mapAnyCamelCase.get("active");
        boolean _active = ((temp != null) ? ((Boolean) temp) : false);
        temp = mapAnyCamelCase.get("name");
        String _name = ((temp != null) ? ((String) temp) : null);
        temp = mapAnyCamelCase.get("created_at");
        Date _createdAt = ((temp != null) ? ((Date) temp) : null);

        User target = new User(
                _id,
                _active,
                _name,
                _createdAt
        );
        return target;
    }

    @Override
    public void updateWithMap(User target, Map<String, Object> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ValidationError> validate(User user) {
        return new ArrayList<ValidationError>();
    }

    public ToManyDAO with(User obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Table getTable() {
        return null;
    }

}

