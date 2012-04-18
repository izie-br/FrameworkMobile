package br.com.cds.mobile.framework.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import br.com.cds.mobile.framework.BOFacade;
import br.com.cds.mobile.framework.LogPadrao;


public class FileUtil {

//	public static boolean baixarArquivo(String urlString, String path, String arquivo, Tarefa<?, ?> tarefa,
//			String sincronia) throws IOException {
//		return BOFacade.getInstance().baixarArquivo(urlString, path, arquivo, tarefa, sincronia);
//	}
//
//	public static boolean baixarArquivo(String string, String path, String nomeArquivo, String sincronia)
//			throws IOException {
//		return baixarArquivo(string, path, nomeArquivo, null, sincronia);
//	}

	public static String unzipAPK(String path, String zipname) throws IOException {
		InputStream is;
		ZipInputStream zis;
		String apk = null;
		int i = 0;
		is = new FileInputStream(path + zipname);
		zis = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry ze;

		while ((ze = zis.getNextEntry()) != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int count;

			String filename = ze.getName();
			LogPadrao.d("unzipando:" + path + filename);
			if (ze.isDirectory()) {
				File pasta = new File(path + filename);
				if (pasta.exists()) {
					pasta.delete();
				}
				pasta.mkdir();
			} else {
				new File(path + filename).delete();
				FileOutputStream fout = new FileOutputStream(path + filename);
				while ((count = zis.read(buffer)) != -1) {
					baos.write(buffer, 0, count);
					byte[] bytes = baos.toByteArray();
					fout.write(bytes);
					baos.reset();
				}
				if (filename.toUpperCase().contains(".APK")) {
					apk = filename;
				}
				fout.close();
			}
			zis.closeEntry();
			i++;
		}

		zis.close();
		LogPadrao.d("Arquivos extraidos:" + i);

		return apk;
	}

	public static void compactarArquivoZip(String file, String zipFile) {
		try {
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
			byte[] data = new byte[1000];
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			int count;
			out.putNextEntry(new ZipEntry(zipFile));
			while ((count = in.read(data, 0, 1000)) != -1) {
				out.write(data, 0, count);
			}
			in.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			BOFacade.getInstance().gravarErro(e, true);
		}
	}

	public static byte[] compactarArquivoZlib(byte[] input) {
		try {
			Deflater deflater = new Deflater();
			deflater.setInput(input);
			deflater.finish();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			while (!deflater.finished()) {
				int byteCount = deflater.deflate(buf);
				baos.write(buf, 0, byteCount);
			}
			deflater.end();

			byte[] compressedBytes = baos.toByteArray();
			return compressedBytes;
		} catch (Exception e) {
			BOFacade.getInstance().gravarErro(e, true);
		}
		return null;
	}

	public static byte[] compress(String string) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
		GZIPOutputStream gos = new GZIPOutputStream(os);
		gos.write(string.getBytes());
		gos.close();
		byte[] compressed = os.toByteArray();
		os.close();
		return compressed;
	}

	public static String decompress(byte[] compressed) throws IOException {
		final int BUFFER_SIZE = 32;
		ByteArrayInputStream is = new ByteArrayInputStream(compressed);
		GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
		StringBuilder string = new StringBuilder();
		byte[] data = new byte[BUFFER_SIZE];
		int bytesRead;
		while ((bytesRead = gis.read(data)) != -1) {
			string.append(new String(data, 0, bytesRead));
		}
		gis.close();
		is.close();
		return string.toString();
	}

	public static void compactarArquivoGZIP(String file, String zipFile) {
		int BUFFER = 1024;
		BufferedInputStream origin = null;
		FileOutputStream dest;
		try {
			dest = new FileOutputStream(zipFile);
			GZIPOutputStream out = new GZIPOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			FileInputStream fi = new FileInputStream(file);
			origin = new BufferedInputStream(fi, BUFFER);
			// ZipEntry entry = new
			// ZipEntry(file.substring(file.lastIndexOf("/") + 1));
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
			out.close();
		} catch (IOException e) {
			BOFacade.getInstance().gravarErro(e, true);
		}
	}

}
