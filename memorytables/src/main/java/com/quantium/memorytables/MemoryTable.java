package com.quantium.memorytables;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class MemoryTable extends TreeMap<Comparable<?>,Object> {
	private static final long serialVersionUID = 6790619834629358255L;

	private long sequence = 1;

	public long getSequence(){
		long current = this.sequence;
//		this.sequence++;
		return current;
	}

	public long insert(Object value){
		Object old = this.put(this.sequence, value);
		if (old != null){
			throw new RuntimeException("Erro ao inserir sem chave primaria");
		}
		return this.sequence;
	}

	@Override
	public Object put(Comparable<?> key, Object value) {
		Object last = super.put(key, value);
		if (last != null)
			return last;

		if (key instanceof Number){
			long newkey = ((Number)key).longValue();
			if (newkey == this.sequence){
				this.sequence++;
			}
			else if (newkey > this.sequence) {
				this.sequence = newkey +1;
			}
		}
		return null;
	}

	@Override
	public void putAll(Map<? extends Comparable<?>,
			               ? extends Object> map)
	{
		if (map == null)
			return;
		Iterator<? extends Comparable<?>> it = map.keySet().iterator();
		while (it.hasNext()){
			Comparable<?> key = it.next();
			this.put(key, map.get(key));
		}
	}

}