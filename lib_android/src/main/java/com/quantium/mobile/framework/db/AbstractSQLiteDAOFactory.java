package com.quantium.mobile.framework.db;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.logging.LogPadrao;

public abstract class AbstractSQLiteDAOFactory implements DAOFactory {

	private final HashMap<EntityKey, SoftReference<Object>> entityCache =
			new HashMap<EntityKey, SoftReference<Object>>();

	public AbstractSQLiteDAOFactory(){
		new TrimThread(this);
	}

	public void pushToCache(Object klassId, Serializable keys [],
	                        Object entity)
	{
		EntityKey key = new EntityKey(klassId, keys);
		entityCache.put(key, new SoftReference<Object>(entity));
	}

	public void removeFromCache(Object klassId, Serializable keys []) {
		EntityKey key = new EntityKey(klassId, keys);
		entityCache.remove(key);
	}

	public Object cacheLookup(Object klassId, Serializable keys []){
		EntityKey key = new EntityKey(klassId, keys);
		SoftReference<Object> softReference = entityCache.get(key);
		if (softReference == null)
			return null;
		Object obj = softReference.get();
		if (obj == null)
			entityCache.remove(key);
		return obj;
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<SoftReference<T>> lookupForClass(Class<T> klass) {
		ArrayList<SoftReference<T>> list =
				new ArrayList<SoftReference<T>>();
		for (SoftReference<?> reference : entityCache.values()) {
			Object obj = reference.get();
			if ( obj != null && klass.isInstance(obj) ){
				list.add((SoftReference<T>)reference);
			}
		}
		return list;
	}

	public void trim(){
		ArrayList<Object> deleted = new ArrayList<Object>();
		for (Object key : entityCache.keySet()){
			SoftReference<Object> softReference = entityCache.get(key);
			if (softReference.get() == null)
				deleted.add(key);
		}
		for (Object key : deleted)
			entityCache.remove(key);
	}

	private static class TrimThread extends Thread {

		WeakReference<AbstractSQLiteDAOFactory> factory;

		private TrimThread(AbstractSQLiteDAOFactory factory){
			this.factory = new WeakReference<AbstractSQLiteDAOFactory>(factory);
		}

		@Override
		public void run() {
			for (;;){
				AbstractSQLiteDAOFactory factoryIns = factory.get();
				if (factoryIns == null)
					return;
				factoryIns.trim();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					LogPadrao.e(e);
				}
			}
		}
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