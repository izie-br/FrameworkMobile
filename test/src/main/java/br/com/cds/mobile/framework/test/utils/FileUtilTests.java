package br.com.cds.mobile.framework.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.test.TestActivity;
import br.com.cds.mobile.framework.utils.FileUtil;

public class FileUtilTests extends 
		ActivityInstrumentationTestCase2<TestActivity> {

	public FileUtilTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	public void testWriteRead(){
		File file = new File(
				new File(getDataDir()),
				org.apache.commons.lang.RandomStringUtils.randomAlphabetic(12)
		);
		String content = org.apache.commons.lang.RandomStringUtils.random(120);
		PrintWriter pw;
		try {
			pw = FileUtil.openFileToAppend(file.getPath());
			pw.print(content);
			pw.close();
			BufferedReader reader = new BufferedReader(
					FileUtil.openFileToRead(file.getPath())
			);
			String read = reader.readLine();
			assertEquals(content, read);
			read = reader.readLine();
			assertNull(read);
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			if(file.exists())
				file.delete();
		}
	}

	public void testZipAndUnzip(){
		int count = 5;
		String fileNames[] = new String[count];
		File files[]= new File[count];
		String filesContent[] = new String[count];
		String zipFile = null;

		try {
			// Criando os arquivos e escrevendo, um a um
			for(int i=0;i<count;i++){
				files[i] = new File(
					new File(getDataDir()),
					org.apache.commons.lang.RandomStringUtils
						.randomAlphabetic(12)
				);
				// nomes dos arquivos
				fileNames[i] = files[i].getPath();
				// conteudo dos aquivos
				filesContent[i] = org.apache.commons.lang
						.RandomStringUtils.random(12000);
				// escrevendo
				PrintWriter pw =
						FileUtil.openFileToAppend(files[i].getPath());
				pw.print(filesContent[i]);
				pw.close();
			}

			// nome do arquivo zip
			zipFile = files[0].getPath()+".zip";

			// zipando
			FileUtil.zipFiles(zipFile, fileNames);
			for(File file : files)
				file.delete();

			// unzip, e recebendo os nomes
			// importante, esta sendo extraido na mesma pasta
			String outFiles[] = FileUtil.unzip(getDataDir(), zipFile);

			// conferindo cada arquivo extraido com o conteudo original
			for(String out : outFiles){
				File outFile = new File(out);
				// conferir se o arquivo existe
				if(!outFile.exists())
					fail();
				// lendo o arquivo
				Reader reader = FileUtil.openFileToRead(outFile.getPath());
				int i = -1;
				// pocurar o arquivo na lista original (Arrays.binarySearch nao funcinou)
				for(int j=0;j<fileNames.length;j++)
					if(fileNames[j].equals(outFile.getPath()))
						i = j;
				// se o arquivo nao estava na lista original
				if(i<0)
					fail();
				// buffer com mesmo tamanho da String original
				char[] outchars = new char[filesContent[i].length()];
				reader.read(outchars);
				// conferindo se o conteudo lido eh igual ao original
				assertEquals(filesContent[i],new String(outchars));
				// conferindo se o arquivo tem mesmo comprimento da string original
				if(reader.read()>0)
					fail();
			}

		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			// removendo todos arquivos
			for(File file : files)
				if(file.exists())
					file.delete();
			if(zipFile!=null){
				File zip = new File(zipFile);
				if(zip.exists())
					zip.delete();
			}
		}
	}

	public void testGzip(){
		// Arquivo de entrada, nome e conteudo
		File file = new File(new File(getDataDir()),
				org.apache.commons.lang.RandomStringUtils
				.randomAlphabetic(12)
		);
		String fileName = file.getPath();
		String fileContent = org.apache.commons.lang.RandomStringUtils
				.randomAlphabetic(120);

		// arquivo gz
		String gzFile = file.getPath()+".gz";

		// Arquivos gz de saido do metodo FileUtil.gzip
		String outGzFileName = null;
		// Arquivos de saido do metodo FileUtil.unGzip
		String outFileName = null;
		try {
			// Criando o arquivo e escrevendo
			PrintWriter pw =
					FileUtil.openFileToAppend(file.getPath());
			pw.print(fileContent);
			pw.close();

			// gzipando
			outGzFileName = FileUtil.gzip(fileName, gzFile);
			// remoendo o orignal
			file.delete();
			// des-gzipando
			outFileName = FileUtil.ungzip(outGzFileName, fileName);
			
			// lendo o arquivo
			Reader reader = FileUtil.openFileToRead(outFileName);
			// buffer com mesmo tamanho da String original
			char[] outchars = new char[fileContent.length()];
			reader.read(outchars);
			// conferindo se o conteudo lido eh igual ao original
			assertEquals(fileContent,new String(outchars));
			// conferindo se o arquivo tem mesmo comprimento da string original
			if(reader.read()>0)
				fail();
		} catch( IOException e){
			fail(e.getMessage());
		} finally {
			// removendo todos arquivos
			file = new File(fileName);
			if(file.exists())
				file.delete();
			if(gzFile!=null){
				File zip = new File(gzFile);
				if(zip.exists())
					zip.delete();
			}
			if(outGzFileName!=null){
				File zip = new File(outGzFileName);
				if(zip.exists())
					zip.delete();
			}
			if(outFileName!=null){
				File zip = new File(outFileName);
				if(zip.exists())
					zip.delete();
			}
		}
	}

	private String getDataDir(){
		return getActivity().getApplicationInfo().dataDir;
	}

}
