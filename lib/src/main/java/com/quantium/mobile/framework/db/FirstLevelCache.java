package com.quantium.mobile.framework.db;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FirstLevelCache {

	private final Map<EntityKey, Reference<?>> entityCache =
			new HashMap<EntityKey, Reference<?>>();

	private int cachePushCount = 0;
	private ReadWriteLock rwLock = new ReentrantReadWriteLock(false);

	public void pushToCache(Object klassId, Serializable keys [],
	                        Object entity)
	{
		Lock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			// Conferir se eh proxy e extrair
			EntityKey key = new EntityKey(klassId, keys);
			entityCache.put(key, new SoftReference<Object>(entity));
			// A cada 10 'pushes'
			// conferir por itens nao utilizados
			if ( (cachePushCount%10) == 0 ) {
				trim();
			}
		} finally {
			writeLock.unlock();
		}
	}

	private void trim () {
		Set<EntityKey> keySet = entityCache.keySet();
		for (EntityKey key : keySet) {
			getOrRemoveIfNull(key);
		}
	}

	public void removeFromCache(Object klassId, Serializable keys []) {
		Lock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			EntityKey key = new EntityKey(klassId, keys);
			entityCache.remove(key);
		} finally {
			writeLock.unlock();
		}
	}

	public Object cacheLookup(Object klassId, Serializable keys []){
		Lock readLock = rwLock.readLock();
		readLock.lock();
		try {
			EntityKey key = new EntityKey(klassId, keys);
			return getOrRemoveIfNull(key);
		} finally {
			readLock.unlock();
		}
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
		Lock readLock = rwLock.readLock();
		readLock.lock();
		try {
			ArrayList<Reference<T>> list =
					new ArrayList<Reference<T>>();
			for (EntityKey key : entityCache.keySet()) {
				Object obj = getOrRemoveIfNull(key);
				if ( obj != null && klass.isInstance(obj) ){
					list.add(  new SoftReference<T>( (T)obj )  );
				}
			}
			return list;
		} finally {
			readLock.unlock();
		}
	}

}
