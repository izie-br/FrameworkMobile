package com.quantium.mobile.geradores.velocity.helpers;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.javabean.Property;

public class OneToManyAssociationHelper extends AssociationHelper {

	private Table table;
	private Property foreignKey;
	private Property referenceKey;
	private String keyToA;
	private String keyToAPluralized;
	private boolean nullable;

	/*
	 * Getters/setters padrao
	 */
	public Table getTable() {
		return table;
	}
	public void setTable(Table table) {
		this.table = table;
	}
	public Property getForeignKey() {
		return foreignKey;
	}
	public void setForeignKey(Property foreignKey) {
		this.foreignKey = foreignKey;
	}
	public Property getReferenceKey() {
		return referenceKey;
	}
	public void setReferenceKey(Property referenceKey) {
		this.referenceKey = referenceKey;
	}
	public String getKeyToA() {
		return keyToA;
	}
	public void setKeyToA(String keyToA) {
		this.keyToA = keyToA;
	}
	public String getKeyToAPluralized() {
		return keyToAPluralized;
	}
	public void setKeyToAPluralized(String keyToAPluralized) {
		this.keyToAPluralized = keyToAPluralized;
	}
	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
}
