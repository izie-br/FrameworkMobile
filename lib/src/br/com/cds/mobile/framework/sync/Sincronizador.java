package br.com.cds.mobile.framework.sync;

public interface Sincronizador {
	void iniciaSincronia(boolean forcar);
	void pararSincronia();
}
