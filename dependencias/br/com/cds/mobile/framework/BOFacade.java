package br.com.cds.mobile.framework;

import java.util.Date;

import android.os.Environment;
//import br.com.cds.mobile.framework.tarefa.Tarefa;
//import br.com.cds.mobile.flora.service.ServicoSincronia;
//import br.com.cds.mobile.flora.util.IntentUtil;
//import br.com.cds.mobile.flora.util.LogPadrao;
//import br.com.cds.mobile.flora.util.StringUtil;
import br.com.cds.mobile.framework.utils.SQLiteUtils;

public class BOFacade {

	private static BOFacade instance;
	public static final byte AMBIENTE_PRODUCAO = 1;
	public static final byte AMBIENTE_HOMOLOGACAO = 2;
//	public static final byte AMBIENTE_DESENVOLVIMENTO = 3;
	public static int ID_NOTIFICACAO_VENDAS_PENDENTES = 5463542;

	// Sempre alterar quando houver uma nova versao! Importante para que caso
	// haja erro, mesmo assim saibamos da versao do aplicativo.
	public static final String VERSAO_APP = "1.3.1.20";

	// NUNCA MUDAR PARA AMBIENTE_PRODUCAO
	public static byte AMBIENTE = AMBIENTE_HOMOLOGACAO;

	// NUNCA MUDAR PARA AMBIENTE_PRODUCAO

	public static BOFacade getInstance() {
		if (instance == null) {
			instance = new BOFacade();
		}
		return instance;
	}


	public BOFacade() {
	}

	public String getCaminhoLog() {
		return Environment.getDataDirectory() + "/data/br.com.cds.mobile.flora/logs/";
	}

	public String getArquivoLog() {
		return SQLiteUtils.dateToString(new Date()) + "_log.txt";
	}

	public void gravarErro(Throwable t) {
		gravarErro(t,false);
	}

	public void gravarErro(Throwable t, boolean forcar) {
		try {
//			erroBO.gravarErro(t, forcar);
		} catch (Throwable t2) {
			t2.printStackTrace();
		}
	}

}
