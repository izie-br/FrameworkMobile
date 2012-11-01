package com.quantium.mobile.framework.db;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import com.quantium.mobile.framework.DAOFactory;

public abstract class AbstractSQLiteDAOFactory implements DAOFactory {

	private final Map<EntityKey, Object> entityCache = new WeakHashMap<EntityKey, Object>();

	public void pushToCache(Object klassId, Serializable keys [],
	                        Object entity)
	{
		EntityKey key = new EntityKey(klassId, keys);
		entityCache.put(key, entity);
	}

	public void removeFromCache(Object klassId, Serializable keys []) {
		EntityKey key = new EntityKey(klassId, keys);
		entityCache.remove(key);
	}

	public Object cacheLookup(Object klassId, Serializable keys []){
		EntityKey key = new EntityKey(klassId, keys);
		Object obj = entityCache.get(key);
		return obj;
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<Reference<T>> lookupForClass(Class<T> klass) {
		ArrayList<Reference<T>> list =
				new ArrayList<Reference<T>>();
		for (Object obj : entityCache.values()) {
			if ( obj != null && klass.isInstance(obj) ){
				list.add(  new WeakReference<T>( (T)obj )  );
			}
		}
		for (Object obj : entityCache.values()) {
			list.add(  new WeakReference<T>( (T)obj )  );
		}
		return list;
	}

}

final class EntityKey {

	private final Object klassId;
	private final Serializable keys[];

	private final int hashcode;

	public EntityKey(Object klassId, Serializable keys []) {
		this.keys = keys;
		this.klassId = klassId;
		this.hashcode = generateHashCode(keys, klassId);
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	private static int generateHashCode(Serializable keys [], Object klassId) {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(keys);
		result = prime * result + klassId.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if ( !(obj instanceof EntityKey) )
			return false;
		EntityKey other = (EntityKey) obj;
		if ( !(klassId == other.klassId) )
			return false;
		if (!Arrays.equals(keys, other.keys))
			return false;
		return true;
	}

}