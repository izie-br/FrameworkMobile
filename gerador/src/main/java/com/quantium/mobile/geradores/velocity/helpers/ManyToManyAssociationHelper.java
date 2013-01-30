package com.quantium.mobile.geradores.velocity.helpers;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.javabean.Property;

public class ManyToManyAssociationHelper extends AssociationHelper {

	private boolean isThisTableA;
	private String joinTable;
	private Property keyToA;
	private Property referenceA;
	private Property keyToB;
	private Property referenceB;

	public boolean getIsThisTableA() {
		return isThisTableA;
	}

	public boolean isThisTableA() {
		return isThisTableA;
	}

	public void setThisTableA(boolean thisTableA) {
		this.isThisTableA = thisTableA;
	}

	public String getJoinTableUpper () {
		return CamelCaseUtils.camelToUpper(
				CamelCaseUtils.toLowerCamelCase(joinTable)
		);
	}

	/*
	 * Getters/setters padrao
	 */
	public String getJoinTable() {
		return joinTable;
	}
	public void setJoinTable(String joinTable) {
		this.joinTable = joinTable;
	}
	public Property getKeyToA() {
		return keyToA;
	}
	public void setKeyToA(Property keyToA) {
		this.keyToA = keyToA;
	}
	public Property getReferenceA() {
		return referenceA;
	}
	public void setReferenceA(Property referenceA) {
		this.referenceA = referenceA;
	}
	public Property getKeyToB() {
		return keyToB;
	}
	public void setKeyToB(Property keyToB) {
		this.keyToB = keyToB;
	}
	public Property getReferenceB() {
		return referenceB;
	}
	public void setReferenceB(Property referenceB) {
		this.referenceB = referenceB;
	}

}
