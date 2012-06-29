package com.quantium.mobile.framework.utils;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

public class IntentUtil {

	public static void salvarSharedPreferencePadrao(Context context, String chave, String valor, long idVendedor) {
		context.getSharedPreferences("padrao-" + idVendedor, 0).edit().putString(chave, valor).commit();
	}

	public static void salvarSharedPreference(Context context, String preference, String chave, String valor) {
		context.getSharedPreferences(preference, 0).edit().putString(chave, valor).commit();
	}

	public static String buscarSharedPreferencePadrao(Context context, String chave, String valorPadrao, long idVendedor) {
		return context.getSharedPreferences("padrao-" + idVendedor, 0).getString(chave, valorPadrao);
	}

	public static String buscarSharedPreference(Context context, String preference, String chave, String valorPadrao) {
		return context.getSharedPreferences(preference, 0).getString(chave, valorPadrao);
	}

	public static void criarAtalho(Context context, int titulo, int icone) {
		Intent shortcutIntent = new Intent();
		shortcutIntent.setClassName(context.getPackageName(), context.getClass().getName());
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.addCategory(Intent.ACTION_PICK_ACTIVITY);
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(titulo));
		BitmapDrawable bd = (BitmapDrawable) context.getResources().getDrawable(icone);
		Bitmap newbit;
		newbit = bd.getBitmap();
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, newbit);
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		context.sendBroadcast(intent);
	}

	public static Intent instalarApp(String path) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
		return intent;
	}

//	public static void removerAtalho(DefaultActivity activity, int titulo, int icone) {
//		Intent shortcutIntent = new Intent();
//		shortcutIntent.setClassName(activity.getPackageName(), activity.getClass().getName());
//		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		shortcutIntent.addCategory(Intent.ACTION_PICK_ACTIVITY);
//		Intent intent = new Intent();
//		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
//		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getResources().getString(titulo));
//		BitmapDrawable bd = (BitmapDrawable) activity.getResources().getDrawable(icone);
//		Bitmap newbit;
//		newbit = bd.getBitmap();
//		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, newbit);
//		intent.putExtra("duplicate", false);
//		intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
//		activity.sendBroadcast(intent);
//	}

	public static int getParametro(Context context, int parametro) {
		return Integer.parseInt(context.getString(parametro));
	}

//	public static void criarNotificacao(DefaultActivity context, String titulo, String mensagem, Class<?> activity,
//			boolean vibra, int idNotificacao, int icon) {
//
//		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		Notification n = new Notification(icon, mensagem, System.currentTimeMillis());
//
//		PendingIntent p = PendingIntent.getActivity((Context) context, 0, new Intent((Context) context, activity), 0);
//		n.setLatestEventInfo((Context) context, titulo, mensagem, p);
//		if (vibra) {
//			// n.vibrate = new long[] { 100, 250, 100, 500 };
//		}
//		nm.notify(idNotificacao, n);
//
//	}

}
