package com.quantium.mobile.framework.comunication;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.quantium.mobile.framework.ErrorCode;
import com.quantium.mobile.framework.FrameworkException;
import com.quantium.mobile.framework.Tarefa;
import com.quantium.mobile.framework.logging.LogPadrao;


public abstract class GenericComunicacao {

	private static final int BUFFER_1K = 1024;
	public static final String TIPO_RETORNO     = "tipo";
	public static final String RETORNO_SUCESSO  = "sucesso";
	public static final String RETORNO_AVISO    = "aviso";
	public static final String RETORNO_ERRO     = "erro";
	public static final String MENSAGEM_RETORNO = "mensagem";

	public static final int SIZE = 50;
	protected static final int TENTATIVAS_DE_CONEXAO = 5;

	private static boolean conectado = true;
	private static MudancaEstadoComunicacaoListener mecListeners[];

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
		int oldSize = mecListeners == null ? 0 : mecListeners.length;
		MudancaEstadoComunicacaoListener old[] = mecListeners;
		mecListeners = new MudancaEstadoComunicacaoListener[ oldSize+1 ];
//		for(int i=0;i<size;i++)
//			mecListeners[i] = old[i];
		System.arraycopy(old, 0, mecListeners, 0, oldSize);
		mecListeners[oldSize] = listener;
	}

	public static boolean baixarArquivo(
			String urlString, String path, String arquivo,
			Tarefa<?, ?> tarefa, String sincronia
	) throws FrameworkException {
		try {
			// LogPadrao.d("Com:" + urlString);
			HttpURLConnection c;
			try {
				URL url = new URL(urlString);
				c = (HttpURLConnection) url.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
			} catch (MalformedURLException e) {
				LogPadrao.e(e);
				return false;
			} catch (ProtocolException e){
				LogPadrao.e (e);
				return false;
			}
			// LogPadrao.d("c.getContentLength():" + c.getContentLength());
			long contentLength = c.getContentLength();
	
			File folder = new File(path);
			folder.mkdirs();
			File file = new File(folder, arquivo);
			if (!file.exists()) {
				file.createNewFile();
			} else {
				if ( file.length() == c.getContentLength())
					return false;
				throw new FrameworkException(ErrorCode.UNABLE_TO_CREATE_EXISTING_FILE);
			}
			// LogPadrao.d("outputFile.exists():" + outputFile.exists());
			// LogPadrao.d("outputFile.length():" + outputFile.length());
			// LogPadrao.d("c.getContentLength():" + c.getContentLength());
			InputStream is = c.getInputStream();;
			c.connect();
			FileOutputStream fos = new FileOutputStream(file);
	
			byte[] buffer = new byte[BUFFER_1K];
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
			// TODO mover isto para cima
			if (!GenericComunicacao.isConetado())
				GenericComunicacao.conectado = true;
			return true;
		} catch (IOException e){
			throw new RuntimeException(e);
		}
	}


}
