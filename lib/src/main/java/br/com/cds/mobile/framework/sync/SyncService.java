package br.com.cds.mobile.framework.sync;

import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.logging.LogPadrao;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

public abstract class SyncService extends Service implements Synchronizer {

	public static final int NOFLAGS = 0;
	public static final int VIBATE = 1;
	private final long TEMPO_PARA_ATUALIZACAO = 15000;

	// public static void setAtividadePadrao(Activity atividadePadrao) {
	// ServicoSincronia.atividadePadrao = atividadePadrao;
	// }
	// public static Activity getAtividadePadrao() {
	// return atividadePadrao;
	// }

	private final IBinder conexao = new LocalBinder();
	private static boolean executando;
	private static long inicioExecucao;
	private static long fimExecucao;

	public static long getInicioExecucao() {
		return inicioExecucao;
	}

	public static long getFimExecucao() {
		return fimExecucao;
	}

	public static boolean isExecutando() {
		return executando;
	}

	@Override
	public void iniciaSincronia(final boolean forcar) {
		new Thread() {
			@Override
			public void run() {
				LogPadrao.d("Sincronia::Inicio: %d", inicioExecucao);
				if (executando) {
					LogPadrao.d("Servico de sincronia ja sendo executado...");
				} else {
					criarNotificacao(SyncService.this, SyncServiceMessages.INIT, VIBATE);
					LogPadrao.d("Processando sincronia no serviço");
					executando = true;
					inicioExecucao = System.currentTimeMillis();
					processaSincronia(forcar);
					executando = false;
					fimExecucao = System.currentTimeMillis();
					criarNotificacao(SyncService.this, SyncServiceMessages.END, VIBATE);
					new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						};
					}.start();
					if (forcar) {
						SyncManager.setForcarSincronia(false);
					}
					LogPadrao.d("Sincronia::Fim: %d", fimExecucao);
				}
			}
		}.start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return conexao;
	}

//	private boolean isImagensDefasadas() {
//		int qtdDiasDefasagem = Integer.parseInt(getResources().getString(R.string.parametro_defasagem_atualizacao));
//		String dataAtualizacao = BaseApplication.getPreferences()
//				.getString("dataAtualizacaoImagens", null);
//		if (dataAtualizacao == null) {
//			return true;
//		}
//		Date dtAtualizacao = DateUtil.stringToDate(dataAtualizacao);
//		Date dtDefasagem = DateUtil.adicionaDias(dtAtualizacao, qtdDiasDefasagem);
//		return dtAtualizacao.after(dtDefasagem);
//	}

//	private void baixaImagens() {
//		new Thread() {
//			public void run() {
//				try {
//					baixarArquivoCompleto();
//				} catch (Throwable t) {
//					LogPadrao.e(t);
//				} finally {
//					baixarArquivosIndividuais();
//					BaseApplication.getPreferences().edit().putString(
//							"dataAtualizacaoImagens",
//							DateUtil.dateToString(new Date())
//					).commit();
//				}
//			};
//		}.start();
//	}

//	private void baixarArquivosIndividuais() {
//		ArrayList<ProdutoBean> produtos = getBO().listarProdutosSemImagem();
//		int qtd = produtos.size();
//		int i = 1;
//		for (ProdutoBean produto : produtos) {
//			try {
//				if (verificaFotoExistente(produto.getCodigo())) {
//					produto.setDtAtualizacaoFoto(new Date());
//					getBO().alterarProduto(produto);
//				} else {
//					criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia",
//							" sincronizando fotos dos produtos (" + i + "/" + qtd + ")", DetalhesSincronia.class, true);
//					try {
//						baixarFotoProduto(produto.getCodigo());
//					} catch (FileNotFoundException e) {
//						getBO().gravarErro(e, true);
//					}
//					if (verificaFotoExistente(produto.getCodigo())) {
//						produto.setDtAtualizacao(new Date());
//						getBO().alterarProduto(produto);
//					}
//				}
//				i++;
//			} catch (Throwable t2) {
//				getBO().gravarErro(t2);
//			}
//		}
//	}

//	private void baixarArquivoCompleto() throws IOException {
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", " sincronizando fotos dos produtos (pacote)",
//				DetalhesSincronia.class, true);
//		String nomeArquivo = "produtos.zip";
//		String path = getBO().getPathImagensProdutos();
//		if (GenericComunicacao.baixarArquivo(getBO().montarURLImagensProdutos(), path, nomeArquivo, "fotos produtos pacote")) {
//			FileUtil.unzip(path, nomeArquivo);
//		}
//	}
//
//	private void baixarFotoProduto(long codigo) throws IOException {
//		String nomeArquivo = "product.zip";
//		String path = getBO().getPathImagensProdutos() + "product-" + codigo + "/";
//		FileUtil.baixarArquivo(getBO().montarURLImagensProdutos(codigo), path, nomeArquivo, "fotos_produtos_individual");
//		FileUtil.unzipAPK(path, nomeArquivo);
//	}
//
//	private boolean verificaFotoExistente(long codigo) {
//		File arquivoGrande = new File(getBO().getPathImagensProdutosTamanho(codigo, ProdutoBO.GRANDE));
//		File arquivoPequeno = new File(getBO().getPathImagensProdutosTamanho(codigo, ProdutoBO.PEQUENA));
//		if ((arquivoGrande.exists() && arquivoGrande.length() > 0)
//				|| (arquivoPequeno.exists() && arquivoPequeno.length() > 0)) {
//			return true;
//		}
//		return false;
//	}

	protected void criarNotificacao(Context context, SyncServiceMessages message) {
		criarNotificacao(context, message, NOFLAGS);
	}

//	private NotificationManager nm;
//	private Notification n;
//	private PendingIntent p;
//	public static String sincroniaAtual;

	protected abstract void criarNotificacao(Context context, SyncServiceMessages message,int flags); 
//	{
//		if (mensagem != null) {
//			sincroniaAtual = mensagem.toString();
//		}
//		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		n = new Notification(R.drawable.icon, mensagem, System.currentTimeMillis());
//		p = PendingIntent.getActivity(this, 0, new Intent(this, activity), 0);
//		n.setLatestEventInfo(this, titulo, mensagem, p);
//		if (vibra) {
//			// n.vibrate = new long[] { 100, 250, 100, 500 };
//		}
//		nm.notify(R.string.app_name, n);
//	}

	protected abstract void processaSincronia(boolean forcar);
//	{
//		executando = true;
//		try {
//			// Antes de cada item de sincronia deve haver um verificaLogout(),
//			// que
//			// é um método que verifica se o usuário fez logout ou não. Isto não
//			// atrapalha o desempenho, porque o método verificaLogout() tem
//			// apenas
//			// uma condição, caso o usuário esteja fora do sistema ele lança um
//			// AvisoException parando a excecução da sincronia.
//			verificarVersao();
//
//			criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "aguardando o login...",
//					DetalhesSincronia.class);
//			aguardaLogin();
//			inicioExecucao = System.currentTimeMillis();
//			LogPadrao.d("Logado");
////			enviaVendas();
////			verificaLogout();
////			enviaNaoVendas();
////			verificaLogout();
////			enviaOrcamentos();
////			verificaLogout();
////			enviaPedidos();
////			verificaLogout();
////			if (forcar) {
////				LogPadrao.d("ATENCAO... FORÇANDO A SINCRONIA");
////				verificarDadosSensíveis();
////				verificaLogout();
////				verificarDadosApoio();
////				verificaLogout();
////			} else {
////				LogPadrao.d("ATENCAO... SINCRONIA INTELIGENTE");
////				sincroniaInteligente();
////				verificaLogout();
////			}
////			enviaSincronias();
////			verificaLogout();
////			verificaImagensProdutos();
////			verificaLogout();
////			verificaLogout();
//			enviaErros();
//
//			// enviaErros();
//			// if (getBO().getUltimaSincronia().equals(new Date(0))) {
//			// getBO().salvarUltimaSincronia(new Date());
//			// } else {
//			// }
//			// enviaImagensPdv();
//			// enviaRastreamentos();
//		//} catch (AvisoException a) {
//		//	LogPadrao.d("Aviso:" + a.getMessage());
//		//} catch (ComunicacaoException c) {
//		//	LogPadrao.d("OFFLINE");
////		} catch (RuntimeException r) {
////			LogPadrao.e(r);
////			if ("Este aparelho não está cadastrado.".equals(r.getMessage())) {
////				LogPadrao.d("Encerrando serviço");
////				GerenciadorSincronia.getInstance(forcar).setPermitido(false);
////				GerenciadorSincronia.getInstance(forcar).destroy();
////				stopSelf();
////			}
//		} catch (Throwable t) {
//			t.printStackTrace();
//			LogPadrao.e(t);
//		}
//		executando = false;
//		fimExecucao = System.currentTimeMillis();
//		sincroniaAtual = null;
//	}

//	private void enviaNaoVendas() {
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "Não vendas", DetalhesSincronia.class);
//		getBO().enviarNaoVendas();
//	}

	// private void enviaImagensPdv() {
	// getBO().enviaImagensPdv();
	//
	// }

//	private void sincroniaInteligente() {
//		criarNotificacao(ServicoSincronia.this, SincroniaMessages.SMART_SYNC);
//		getBO().sincroniaInteligente();
//	}

	public CountDownTimer getContadorAtualizacao() {
		return new CountDownTimer(TEMPO_PARA_ATUALIZACAO, TEMPO_PARA_ATUALIZACAO) {
			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				String estaEmBG = BaseApplication.getPreferences("appEmBG").getString("estaEmBG","nao");
				if (estaEmBG.equals("nao")) {
//					VersaoBean versao = getBO()
//							.buscarVersao(ServicoSincronia.this, getBO().getImei(), getBO().getMac());
					//Intent intent = getAtividadePadrao().baixarApk(versao, null);
					//getAtividadePadrao().startActivity(intent);
					BaseApplication.getPreferences("appEmBG")
						.edit()
						.putString("atualizando", "sim")
						.commit();
					getContadorAtualizacao().start();
				} else {
					LogPadrao.d("Nao precisa chamar a atualização. Ja esta disponivel.");
					getContadorAtualizacao().start();
				}
			}
		};
	}

//	private void verificarVersao() {
//		try {
//			criarNotificacao(ServicoSincronia.this, SyncServiceMessages.CHECKING_VERSION);
//			VersaoBean versao = getBO().buscarVersao(this, getBO().getImei(), getBO().getMac());
//			if (!versao.getVersaoDb().equals(getBO().getVersaoDb())) {
//				LogPadrao.d("Db Desatualizado.");
//				Looper.prepare();
//				getContadorAtualizacao().start();
//				Looper.loop();
//			} else if (!versao.getVersaoApp().equals(getBO().getVersaoApp())) {
//				Looper.prepare();
//				getContadorAtualizacao().start();
//				Looper.loop();
//			} else {
//				LogPadrao.d("Atualizado e permitido.");
//				BaseApplication.getPreferences("appEmBG")
//					.edit()
//					.putString("estaEmBG", "nao")
//					.commit();
//			}
//		} catch (Throwable t) {
//			LogPadrao.e(t);
//		}
//	}

//	private void enviaPedidos() {
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "enviando pedidos...", DetalhesSincronia.class);
//		getBO().enviarPedidos();
//	}

//	private void enviaSincronias() {
//		try {
//			getBO().enviarSincronias();
//		} catch (Throwable t) {
//			LogPadrao.e(t);
//		}
//	}

//	private void enviaOrcamentos() {
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "enviando orçamentos...",
//				DetalhesSincronia.class);
//		getBO().enviarOrcamentos();
//	}

	// private void enviaRastreamentos() {
	// criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia",
	// "enviando rastreamento...",
	// DetalhesSincronia.class);
	// getBO().enviarRastreamentos();
	// }

//	private void enviaVendas() {
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "sincronizando vendas...",
//				DetalhesSincronia.class);
//		getBO().enviarVendas();
//	}

//	private void aguardaLogin() throws InterruptedException {
//		while (getBO().getVendedorLogado() == null) {
//			Thread.sleep(2000);
//		}
//	}

//	private void verificaLogout() {
//		if (getBO().getVendedorLogado() == null) {
//			nm.cancelAll();
//			throw new AvisoException("Deu logout");
//		}
//	}


	// TODO 
	private void enviaErros() {
		criarNotificacao(SyncService.this, SyncServiceMessages.SENDING_LOGS);
		try {
			// getBO().gravarErro(new RuntimeException("Testando"));
			//getBO().enviarErros();
		} catch (Throwable t) {
			LogPadrao.e(t);
			//getBO().enviarErrosPorEmail();
		}
	}

//	private void verificarDadosSensíveis() throws InterruptedException {
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "sincronizando clientes...",
//				DetalhesSincronia.class);
//		getBO().receberClientes();
//		LogPadrao.d("Cliente: Recebimento finalizado.");
//		verificaLogout();
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "sincronizando preços e produtos...",
//				DetalhesSincronia.class);
//		getBO().receberProdutos();
//		LogPadrao.d("Produto: Recebimento finalizado.");
//		verificaLogout();
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "sincronizando combos...",
//				DetalhesSincronia.class);
//		getBO().receberCombos();
//		verificaLogout();
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "sincronizando descontos...",
//				DetalhesSincronia.class);
//		getBO().receberEscalonado();
		// criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia",
		// "recebendo vendas...", DetalhesSincronia.class);
		// getBO().receberVendas();
		// LogPadrao.d("Venda: Recebimento finalizado.");
		// criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia",
		// "recebendo pedidos...",
		// DetalhesSincronia.class);
		// getBO().receberPedidos();
		// LogPadrao.d("Pedido: Recebimento finalizado.");
		// getBO().receberProdutosVendas();
//		verificaLogout();
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "sincronizando relatórios...",
//				DetalhesSincronia.class);
//		getBO().receberRelatorios();
//		// if (dadosSensiveisDefasado()) {
//		// criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia",
//		// "sincronizando regionais...",
//		// DetalhesSincronia.class);
//		// getBO().receberRegionais();
//		// LogPadrao.d("ProdutosVenda: Recebimento finalizado.");
//		IntentUtil.salvarSharedPreferencePadrao(this, "atualizacaoDadosSensivel",
//				StringUtil.dateToStringBanco(new Date()), getBO().getIdVendedorLogado());
//		// } else {
//		// criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia",
//		// "dados sensíveis atualizados... ignorando...",
//		// DetalhesSincronia.class);
//		// }
//	}

//	private void verificarDadosApoio() {
//		// if (dadosApoioDefasado()) {
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "sincronizando motivos...",
//				DetalhesSincronia.class);
//		getBO().receberMotivos();
//		LogPadrao.d("Motivos: Recebimento finalizado.");
//		criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia", "sincronizando formas de pagamento...",
//				DetalhesSincronia.class);
//		if (getBO().listarParcelas().size() != 20 || getBO().listarFormasPagamento().size() != 10
//				|| getBO().listarTiposPagamento().size() != 4) {
//			getBO().receberPagamento();
//		}
//		IntentUtil.salvarSharedPreferencePadrao(this, "atualizacaoDadosApoio",
//				StringUtil.dateToStringBanco(new Date()), getBO().getIdVendedorLogado());
//		// } else {
//		// criarNotificacao(ServicoSincronia.this, "FVM Flora - Sincronia",
//		// "dados de apoio atualizados... ignorando...",
//		// DetalhesSincronia.class);
//		// }
//	}

	// private boolean dadosSensiveisDefasado() {
	// int segundosAtualizacaoSensivel = (int)
	// GerenciadorSincronia.TEMPO_ESPERA;
	// Date dtDefasado =
	// StringUtil.stringToTimestamp(StringUtil.dateToStringBanco(DateUtil.subtraiSegundos(
	// new Date(), segundosAtualizacaoSensivel)));
	// String dataSincronia = IntentUtil.buscarSharedPreferencePadrao(this,
	// "atualizacaoDadosSensivel",
	// StringUtil.dateToStringBanco(dtDefasado));
	// Date dtSincronia = StringUtil.stringToTimestamp(dataSincronia);
	// if (dtSincronia.equals(dtDefasado)) {
	// return true;
	// }
	// if (dtSincronia.before(dtDefasado)) {
	// return true;
	// }
	// return false;
	// }

	// private boolean dadosApoioDefasado() {
	// int minutosAtualizacaoApoio = IntentUtil.getParametro(this,
	// R.string.parametro_defasagem_atualizacao_apoio);
	// Date dtDefasado =
	// StringUtil.stringToTimestamp(StringUtil.dateToStringBanco(DateUtil.subtraiMinutos(new
	// Date(),
	// minutosAtualizacaoApoio)));
	// String dataSincronia = IntentUtil.buscarSharedPreferencePadrao(this,
	// "atualizacaoDadosApoio",
	// StringUtil.dateToStringBanco(dtDefasado));
	// Date dtSincronia = StringUtil.stringToTimestamp(dataSincronia);
	// if (dtSincronia.equals(dtDefasado)) {
	// return true;
	// }
	// if (dtSincronia.before(dtDefasado)) {
	// return true;
	// }
	// return false;
	// }

//	private BOFacade getBO() {
//		return BOFacade.getInstance();
//	}

//	private void verificaImagensProdutos() {
//		if (isImagensDefasadas()) {
//			baixaImagens();
//		}
//	}

	@Override
	public void pararSincronia() {
		stopSelf();
	}

	public class LocalBinder extends Binder {
		public Synchronizer getSincronizador() {
			return SyncService.this;
		}
	}



}
