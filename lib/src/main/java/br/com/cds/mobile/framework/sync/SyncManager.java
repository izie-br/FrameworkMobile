package br.com.cds.mobile.framework.sync;

import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.logging.LogPadrao;
import android.content.Context;
import android.content.Intent;

public class SyncManager extends Thread {
	private static boolean executou = false;
	private static SyncManager instance;
	public static long TEMPO_ESPERA = 600000;
	// public static long TEMPO_ESPERA = 30000;
	private static boolean primeira = true;

	public static SyncManager getInstance(boolean forcar) {
		if (instance == null) {
			instance = new SyncManager();
		}
		return instance;
	}

	private boolean ativo = false;

	private boolean ligado = false;

	private boolean permitido = true;

	private static boolean forcar = false;

	public static void setForcarSincronia(boolean forcar) {
		SyncManager.forcar = forcar;
	}

	private SyncManager() {
	}

	@Override
	public void destroy() {
		ligado = false;
		ativo = false;
		instance = null;
		executou = false;
		Synchronizer sincronizador = BaseApplication.getSincronizador();
		if (sincronizador != null) {
			sincronizador.pararSincronia();
		}
	}

	public void setPermitido(boolean permitido) {
		this.permitido = permitido;
	}

	@Override
	public void run() {
		if (executou) {
			return;
		}
		while (ligado) {
			if (BaseApplication.getContext() == null) {
				destroy();
				return;
			}
			if (!permitido) {
				destroy();
				return;
			}
			// LogPadrao.d("Sincronizando.");
			// while (GenericActivity.getInstance().getSincronizador() == null)
			// {
			// try {
			// LogPadrao.d("Sincronizador não conectado.");
			// Thread.sleep(2000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// }
			try {
				// GenericActivity.getInstance().startService(
				// new Intent((Context) GenericActivity.getInstance(),
				// ServicoSincronia.class));
				Context context =  BaseApplication.getContext();
				context.bindService(
						// TODO SyncService eh abstract!!!
						new Intent( context, SyncService.class),
						// TODO o service connection
						BaseApplication.getServiceConnection(),
						Context.BIND_AUTO_CREATE);
				System.out.println("Sincronizando.");
				Thread.sleep(2000);
				Synchronizer sincronizador = BaseApplication.getSincronizador();
				if (sincronizador != null) {
					sincronizador.iniciaSincronia(forcar);
					primeira = false;
				}
				try {
					// LogPadrao.d("Aguardando.");
					if (!primeira) {
						System.out.println("Aguardando.");
						Thread.sleep(TEMPO_ESPERA);
					}
					// TEMPO_ESPERA = 300000;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Throwable t) {
				LogPadrao.e(t);
			}
		}
		executou = true;
		// if (forcar) {
		// destroy();
		// getInstance(false).start();
		// }
	}

	@Override
	public synchronized void start() {
		if (!ativo && BaseApplication.getServiceConnection() != null) {
			ativo = true;
			ligado = true;
			try {
				super.start();
			} catch (Throwable t) {
				LogPadrao.e(t);
			}
		}
	}

}
