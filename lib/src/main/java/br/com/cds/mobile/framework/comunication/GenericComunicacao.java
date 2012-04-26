package br.com.cds.mobile.framework.comunication;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.com.cds.mobile.framework.ErrorCode;
import br.com.cds.mobile.framework.FrameworkException;
import br.com.cds.mobile.framework.Tarefa;
import br.com.cds.mobile.framework.logging.LogPadrao;

public class GenericComunicacao {

	public static final String TIPO_RETORNO     = "tipo";
	public static final String RETORNO_SUCESSO  = "sucesso";
	public static final String RETORNO_AVISO    = "aviso";
	public static final String RETORNO_ERRO     = "erro";
	public static final String MENSAGEM_RETORNO = "mensagem";
	public static final int SIZE = 50;
	protected static final int TENTATIVAS_DE_CONEXAO = 5;
	private static boolean conectado = false;
	private static List<MudancaEstadoComunicacaoListener> mecListeners;


	/**
	 * Retorna o estado da conexao
	 * 
	 * @return
	 */
	public static boolean isConetado() {
		return conectado;
	}

	/**
	 * Setter de estado da conexao. Executa os listeners de alteracao de estado
	 * de conexao
	 */
	public static void setConectado(boolean conectado) {
		// se nao ha mudanca: return
		if (conectado == GenericComunicacao.conectado)
			return;
		GenericComunicacao.conectado = conectado;
		// se nao ha listeners, retorna
		if (mecListeners == null)
			return;
		// execucao dos listeners
		for (MudancaEstadoComunicacaoListener listener : mecListeners) {
			if (conectado)
				listener.onConectado();
			else
				listener.onDesconectado();
		}
	}

	public static void addMudancaEstadoComunicacaoListener(MudancaEstadoComunicacaoListener listener) {
		if (mecListeners == null)
			mecListeners = new ArrayList<MudancaEstadoComunicacaoListener>();
		mecListeners.add(listener);
	}

	public static boolean baixarArquivo(String urlString, String path, String arquivo, Tarefa<?, ?> tarefa, String sincronia)
			throws IOException {
		float conteudo;
		long timestampFim;
		long timestampInicio;

// TODO
//		if (sincronia != null) {
//			getBOFacade().iniciarSincronia(sincronia, SincroniaBean.RECEBIMENTO, 1);
//		}
		// TODO verificar erros
		Throwable erro = null;
		boolean retorno = false;
		try {
			// LogPadrao.d("Com:" + urlString);
			URL url = new URL(urlString);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			// LogPadrao.d("c.getContentLength():" + c.getContentLength());
			long contentLength = c.getContentLength();

			File file = new File(path);
			file.mkdirs();
			if (!file.exists()) {
				throw new FrameworkException(ErrorCode.SD_CARD_NOT_FOUND);
			}
			File outputFile = new File(file, arquivo);
			// LogPadrao.d("outputFile.exists():" + outputFile.exists());
			// LogPadrao.d("outputFile.length():" + outputFile.length());
			// LogPadrao.d("c.getContentLength():" + c.getContentLength());
			if (outputFile.exists() && outputFile.length() == c.getContentLength()) {
				retorno = false;
			} else {
				timestampInicio = System.currentTimeMillis();
				c.connect();
				FileOutputStream fos = new FileOutputStream(outputFile);
				conteudo = c.getContentLength();
				InputStream is = c.getInputStream();

				byte[] buffer = new byte[1024];
				int len1 = 0;
				int i = 0;
				while ((len1 = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len1);
					// LogPadrao.d("i++, (int) contentLength / 1024:" + (i++) +
					// "/"
					// +
					// ((int) contentLength / 1024));
					if (tarefa != null) {
						if (contentLength > 0) {
							tarefa.publicaProgresso(i++/(contentLength/1024));
						} else {
							tarefa.publicaProgresso(i++/(8000/1024));
						}
					}
				}
				fos.close();
				is.close();
				timestampFim = System.currentTimeMillis();
				// TODO mover isto para cima
				if (!GenericComunicacao.isConetado())
					GenericComunicacao.conectado = true;
				retorno = true;
			}
		} catch (Throwable t) {
//			if (ComunicacaoException.isComunicacaoException(t)) {
//				GenericComunicacao.setConectado(false);
//			}
			erro = t;
			retorno = false;
// TODO
//		} finally {
//			if (sincronia != null) {
//				getBOFacade().finalizarSincronia(sincronia, SincroniaBean.RECEBIMENTO, 1, erro,
//						calculaTaxaTransferencia(), (int) conteudo);
//			}
		}
		return retorno;
	}

}
