package com.quantium.mobile.framework.db;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.quantium.mobile.framework.DAOFactory;

public abstract class AbstractSQLiteDAOFactory implements DAOFactory {

	private final Map<EntityKey, Reference<?>> entityCache =
			new HashMap<EntityKey, Reference<?>>();
	int cachePushCount = 0;

	public synchronized void pushToCache(Object klassId, Serializable keys [],
	                        Object entity)
	{
		// Conferir se eh proxy e extrair
		EntityKey key = new EntityKey(klassId, keys);
		entityCache.put(key, new SoftReference<Object>(entity));
		// A cada 10 'pushes'
		// conferir por itens nao utilizados
		if ( (cachePushCount%10) == 0 ) {
			trim();
		}
	}

	private void trim () {
		Set<EntityKey> keySet = entityCache.keySet();
		for (EntityKey key : keySet) {
			getOrRemoveIfNull(key);
		}
	}

	public void removeFromCache(Object klassId, Serializable keys []) {
		EntityKey key = new EntityKey(klassId, keys);
		entityCache.remove(key);
	}

	public Object cacheLookup(Object klassId, Serializable keys []){
		EntityKey key = new EntityKey(klassId, keys);
		return getOrRemoveIfNull(key);
	}

	private Object getOrRemoveIfNull(EntityKey key) {
		Reference<?> ref = entityCache.get(key);
		if (ref == null) {
			return null;
		}
		Object obj = ref.get();
		if (obj == null) {
			entityCache.remove(key);
			return null;
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<Reference<T>> lookupForClass(Class<T> klass) {
		ArrayList<Reference<T>> list =
				new ArrayList<Reference<T>>();
		for (EntityKey key : entityCache.keySet()) {
			Object obj = getOrRemoveIfNull(key);
			if ( obj != null && klass.isInstance(obj) ){
				list.add(  new SoftReference<T>( (T)obj )  );
			}
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