package com.quantium.mobile.framework.libandroidtest;

import com.quantium.mobile.framework.MapSerializable;

import java.util.Date;
import java.util.Map;

public class User extends GenericBean implements MapSerializable {

    private final static long serialVersionUID = 7870516583328777653L;
    long id = 0;
    boolean active;
    String name;
    Date createdAt;

    public User() {
    }

    public User(
            long id,
            boolean active,
            String name,
            Date createdAt
    ) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        triggerObserver("id");
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        triggerObserver("active");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        triggerObserver("name");
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        triggerObserver("created_at");
    }

    @Override
    public void toMap(Map<String, Object> map) {
        if (id != 0) {
            map.put("id", id);
        }
        map.put("active", active);
        map.put("name", name);
        map.put("created_at", createdAt);
    }

    @Override
    public int hashCodeImpl() {
        int value = 1;
        value += (int) id;
        value += (active) ? 1 : 0;
        value *= (name == null) ? 1 : name.hashCode();
        value *= (createdAt == null) ? 1 : createdAt.hashCode();
        return value;
    }

    @Override
    public boolean equalsImpl(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        User other = ((User) obj);
        if (id != other.getId())
            return false;
        if (active != other.isActive())
            return false;
        if ((name == null) ? (other.getName() != null) : !name.equals(other.getName()))
            return false;
        if ((createdAt == null) ? (other.getCreatedAt() != null) : !createdAt.equals(other.getCreatedAt()))
            return false;
        return true;
    }

}
