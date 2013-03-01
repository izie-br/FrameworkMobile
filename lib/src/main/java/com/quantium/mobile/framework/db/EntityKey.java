package com.quantium.mobile.framework.db;

import java.io.Serializable;
import java.util.Arrays;

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
