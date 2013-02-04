package com.quantium.mobile.framework.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.quantium.mobile.framework.utils.StringUtil;

public class FileUtil {

	public static PrintWriter openFileToAppend(String path) throws IOException{
		File logFile = new File(path);
		File folder = logFile.getParentFile();
		if (!folder.exists())
			folder.mkdirs();
		if( !logFile.exists())
			logFile.createNewFile();  // throws IOException
		PrintWriter pw = null;
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(logFile,true),
							StringUtil.DEFAULT_ENCODING
					)
			);
			pw = new PrintWriter(bufferedWriter){
				@Override
				protected void finalize() throws Throwable {  // Nao estava escrevendo logs ao 
					try{                                      // ao finalizar dento de uma softreference.                 
						this.flush();                         // Sobreescrevi o metodo finalize
						this.close();                         // para realizar o flush.        
					} finally {
						super.finalize();
					}
				}
			};
		} catch (UnsupportedEncodingException e) {  // Este erro nao deve acontecer em hipotese alguma
			throw new RuntimeException(e);          // a menos que a plataforma nao suporte UTF-8!  O_o
		} catch (FileNotFoundException e) {  // Este nao deve ocorrer, o arquivo de log jah
			throw new RuntimeException(e);   // foi criado e, se nao, um erro jah foi lancado
		}                                    // reescreva isto se o metodo "openLogFile" for refatorado
		return pw;
	}

	public static Reader openFileToRead(String path) throws FileNotFoundException{
		try{
			return new InputStreamReader(
					new FileInputStream(path),
					StringUtil.DEFAULT_ENCODING
			);
		} catch (UnsupportedEncodingException e) {// Este erro nao deve acontecer em hipotese alguma 
			throw new RuntimeException(e);        // a menos que a plataforma nao suporte UTF-8!  O_o
		}
	}


	public static String[] unzip(String path, String zipname) throws IOException {
		ArrayList<String> files = new ArrayList<String>(1);
		InputStream is;
		ZipInputStream zis;
		// int i = 0;
		is = new FileInputStream(zipname);
		zis = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry ze;

		while ((ze = zis.getNextEntry()) != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int count;

			String filename = ze.getName();
			if (ze.isDirectory()) {
				File pasta = new File(path + filename);
				if (pasta.exists()) {
					pasta.delete();
				}
				pasta.mkdir();
			} else {
				File file = new File(filename);
				file.delete();
				file.createNewFile();
				FileOutputStream fout = new FileOutputStream(file);
				while ((count = zis.read(buffer)) != -1) {
					baos.write(buffer, 0, count);
					byte[] bytes = baos.toByteArray();
					fout.write(bytes);
					baos.reset();
				}
				files.add(filename);
				fout.close();
			}
			zis.closeEntry();
			//i++;
		}
		zis.close();
		String out[] = new String[files.size()];
		for(int i=0;i<files.size();i++)
			out[i] = files.get(i);
		return out;
	}

	public static void zipFiles(String zipFile,String...files) throws IOException{
		if(files==null||files.length==0)
			return;

		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
		int bufferSize = 1000;
		byte[] data = new byte[bufferSize];

		for(String file:files){
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			int count;
			out.putNextEntry(new ZipEntry(file));
			while ((count = in.read(data, 0, bufferSize)) != -1) {
				out.write(data, 0, count);
			}
			in.close();
		}
		out.flush();
		out.close();
	}

//	public static byte[] compactarArquivoZlib(byte[] input) {
//		Deflater deflater = new Deflater();
//		deflater.setInput(input);
//		deflater.finish();
//
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		byte[] buf = new byte[8192];
//		while (!deflater.finished()) {
//			int byteCount = deflater.deflate(buf);
//			baos.write(buf, 0, byteCount);
//		}
//		deflater.end();
//
//		byte[] compressedBytes = baos.toByteArray();
//		return compressedBytes;
//	}
//
//	public static byte[] compress(String string) throws IOException {
//		ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
//		GZIPOutputStream gos = new GZIPOutputStream(os);
//		gos.write(string.getBytes());
//		gos.close();
//		byte[] compressed = os.toByteArray();
//		os.close();
//		return compressed;
//	}
//
	public static String ungzip(String gzFile, String outFile) throws IOException {
		int bufferSize = 32;
		String outFileName = outFile;
		int numSufix = 2;
		while(new File(outFileName).exists()){
			outFileName = outFile+numSufix;
			numSufix++;
		}
		FileInputStream is = new FileInputStream(gzFile);
		GZIPInputStream gis = new GZIPInputStream(is, bufferSize);
		FileOutputStream out = new FileOutputStream(outFileName);
		byte[] data = new byte[bufferSize];
		int bytesRead;
		while ((bytesRead = gis.read(data)) != -1) {
			out.write(data, 0, bytesRead);
		}
		is.close();
		out.close();
		return outFile;
	}

	public static String gzip(String file, String gzFile) throws IOException{
		int bufferSize = 1024;
		String outFileName = gzFile;
		int numSufix = 2;
		while(new File(outFileName).exists()){
			outFileName = gzFile+numSufix;
			numSufix++;
		}
		BufferedInputStream origin = null;
		FileOutputStream dest= new FileOutputStream(gzFile);
		GZIPOutputStream out = new GZIPOutputStream(new BufferedOutputStream(dest));
		byte data[] = new byte[bufferSize];
		FileInputStream fi = new FileInputStream(file);
		origin = new BufferedInputStream(fi, bufferSize);
		int count;
		while ((count = origin.read(data, 0, bufferSize)) != -1) {
			out.write(data, 0, count);
		}
		origin.close();
		out.close();
		return outFileName;
	}

}
