package br.com.cds.mobile.framework;

import java.util.Date;

import android.os.Environment;
import br.com.cds.mobile.framework.config.Aplicacao;
import br.com.cds.mobile.framework.utils.DateUtil;

public class BOFacade {

	private static BOFacade instance;
	public static final byte AMBIENTE_PRODUCAO = 1;
	public static final byte AMBIENTE_HOMOLOGACAO = 2;
//	public static final byte AMBIENTE_DESENVOLVIMENTO = 3;
	public static int ID_NOTIFICACAO_VENDAS_PENDENTES = 5463542;

	//Manter esta declaracao de versao em uma linha
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
		return Environment.getDataDirectory() + "/data/"+Aplicacao.getContext().getPackageName()+"/logs/";
	}

	public String getArquivoLog() {
		return DateUtil.dateToString(new Date()) + "_log.txt";
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
