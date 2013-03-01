package com.quantium.mobile.framework.db;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FirstLevelCache {

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
