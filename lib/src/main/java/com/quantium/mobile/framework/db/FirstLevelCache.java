package com.quantium.mobile.framework.db;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.query.Q.QNode1X1;
import com.quantium.mobile.framework.validation.Constraint;

/**
 * Crie um Timer, ou similar para executar o metodo trim,
 * para remover todas as SoftReference vazias do cache
 * 
 * @author Igor Soares
 *
 */
public class FirstLevelCache {

	private final Map<EntityKey, Reference<?>> entityCache =
			new HashMap<EntityKey, Reference<?>>();

	private ReadWriteLock rwLock = new ReentrantReadWriteLock(false);

	protected <T> Reference<T> createReference(T obj) {
		return new SoftReference<T>(obj);
	}

	public void pushToCache(Object klassId, Serializable keys [],
	                        Object entity)
	{
		Lock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			// Conferir se eh proxy e extrair
			EntityKey key = new EntityKey(klassId, keys);
			entityCache.put(key, createReference(entity));
		} finally {
			writeLock.unlock();
		}
	}

	public void trim () {
		Lock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			Iterator<Map.Entry<EntityKey,Reference<?>>> it =
					entityCache.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<EntityKey, Reference<?>> entry = it.next();
				Reference<?> ref = entry.getValue();
				if (ref == null || ref.get() == null) {
					it.remove();
				}
			}
		} finally {
			writeLock.unlock();
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
			Reference<?> ref = entityCache.get(key);
			return (ref == null)? null : ref.get();
		} finally {
			readLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<Reference<T>> lookupForClass(Class<T> klass) {
		Lock readLock = rwLock.readLock();
		readLock.lock();
		try {
			ArrayList<Reference<T>> list =
					new ArrayList<Reference<T>>();
			for (EntityKey key : entityCache.keySet()) {
				Reference<?> ref = entityCache.get(key);
				Object obj = (ref == null)? null : ref.get();
				if ( obj != null && klass.isInstance(obj) ){
					list.add(createReference((T)obj));
				}
			}
			return list;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * <p>
	 *   Avalia se a busca representada pelo {Q} pode ser resolvida no cache
	 *   sem acesso a banco
	 * </p>
	 * <p>
	 *   Na implementacao atual apenas buscas por ID sao suportadas.
	 * </p>
	 * <p>
	 *   Se o cache nao tem a resposta completa ao {Q} este metodo deve
	 *   retornar NULL
	 * </p>
	 * 
	 * @param klass classe alvo
	 * @param q {Q} de busca
	 * @return Items do cache. NULL se o cache nao tem a resposta completa para o {Q}
	 */
	public <T> List<T> loadQFromCache(Class<T> klass, Q q) {
		// Para impedir  uso do cache, comente o corpo do metodo
		// e substitua por "return null;"
		if (q == null)
			return null;
		if (!(q.getRooNode() instanceof Q.QNode1X1))
			return null;
		Q.QNode1X1 node = (QNode1X1) q.getRooNode();
		if (!isPk(node.column()))
			return null;
		Object id = node.getArg();
		if (id == null || !(id instanceof Serializable))
			return null;
		@SuppressWarnings("unchecked")
		T obj = (T) cacheLookup(klass, new Serializable[]{(Serializable)id});
		if (obj == null)
			return null;
		ArrayList<T> list = new ArrayList<T>(1);
		list.add(obj);
		return list;
	}

	private static boolean isPk(Table.Column<?> column) {
		Collection<Constraint> constraints = column.getConstraintList();
		for (Constraint constraint : constraints){
			if (constraint instanceof Constraint.PrimaryKey)
				return true;
		}
		return false;
	}

}
