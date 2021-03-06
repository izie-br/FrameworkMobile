package com.quantium.mobile.framework.libandroidtest.utils;

import com.quantium.mobile.framework.utils.FileUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class FileUtilTest {

    private static final String TESTDIR = "__testdir";

    @BeforeClass
    public static void setUpClass() {
        // Cria o datadir se nao existe
        getDataDir();
    }

    @AfterClass
    public static void tearDownClass() {
        deleteDataDir();
    }

    private static String getDataDir() {
        File dir = new File(TESTDIR);
        if (!dir.exists()) {
            try {
                dir.mkdir();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        return dir.getAbsolutePath();
    }

    private static void deleteDataDir() {
        File dir = new File(TESTDIR);
        if (dir.exists()) {
            try {
                deleteFolderR(dir);
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }

    private static void deleteFolderR(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteFolderR(c);
        }
        if (!f.delete()) {
            throw new FileNotFoundException(
                    "Failed to delete file: " + f
            );
        }
    }

    /**
     * Escreve um arquivo com FileUtil.openFileToAppend,
     * em seguida confere se o arquivo eh lido corretaente
     * usando FileUtil.openFileToRead, conferindo se o conteudo
     * eh o mesmo
     */
    @Test
    public void testWriteRead() {
        File file = new File(
                new File(getDataDir()),
                org.apache.commons.lang.RandomStringUtils.randomAlphabetic(12)
        );
        String content =
                "teste" + "\n" + "de" + "\r" + "quebra" +
                        "\n\r" + "de" + "\r\n" + "linha" +
                        org.apache.commons.lang
                                .RandomStringUtils
                                .random(120);
        try {
            PrintWriter pw = FileUtil.openFileToAppend(file.getPath());
            pw.print(content);
            pw.close();
            BufferedReader reader = new BufferedReader(
                    FileUtil.openFileToRead(file.getPath())
            );
            StringBuilder sb = new StringBuilder();
            char buffer[] = new char[255];
            int count;
            while ((count = reader.read(buffer)) >= 0) {
                sb.append(String.valueOf(buffer, 0, count));
            }
            String read = sb.toString();
            assertEquals(content, read);

            // nao devem haver mais bytes (ou seja, chars) para ler
            int charsAvalilable = reader.read(new char[255]);
            assertTrue(charsAvalilable < 0);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (file.exists())
                file.delete();
        }
    }

    /**
     * Zipa um arquivo, des-zipa, e confere se o conteudo eh o mesmo
     */
    @Test
    public void testZipAndUnzip() {
        int count = 5;
        String fileNames[] = new String[count];
        File files[] = new File[count];
        String filesContent[] = new String[count];
        String zipFile = null;

        try {
            // Criando os arquivos e escrevendo, um a um
            for (int i = 0; i < count; i++) {
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
            zipFile = files[0].getPath() + ".zip";

            // zipando
            FileUtil.zipFiles(zipFile, fileNames);
            for (File file : files)
                file.delete();

            // unzip, e recebendo os nomes
            // importante, esta sendo extraido na mesma pasta
            String outFiles[] = FileUtil.unzip(getDataDir(), zipFile);

            // conferindo cada arquivo extraido com o conteudo original
            for (String out : outFiles) {
                File outFile = new File(out);
                // conferir se o arquivo existe
                if (!outFile.exists())
                    fail();
                // lendo o arquivo
                Reader reader = FileUtil.openFileToRead(outFile.getPath());
                int i = -1;
                // pocurar o arquivo na lista original (Arrays.binarySearch nao funcinou)
                for (int j = 0; j < fileNames.length; j++)
                    if (fileNames[j].equals(outFile.getPath()))
                        i = j;
                // se o arquivo nao estava na lista original
                if (i < 0)
                    fail();
                // buffer com mesmo tamanho da String original
                char[] outchars = new char[filesContent[i].length()];
                reader.read(outchars);
                // conferindo se o conteudo lido eh igual ao original
                assertEquals(filesContent[i], new String(outchars));
                // conferindo se o arquivo tem mesmo comprimento da string original
                if (reader.read() > 0)
                    fail();
            }

        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            // removendo todos arquivos
            for (File file : files)
                if (file.exists())
                    file.delete();
            if (zipFile != null) {
                File zip = new File(zipFile);
                if (zip.exists())
                    zip.delete();
            }
        }
    }

    /**
     * Gzipa um arquivo, des-gzipa, e confere se o conteudo eh o mesmo
     */
    @Test
    public void testGzip() {
        // Arquivo de entrada, nome e conteudo
        File file = new File(new File(getDataDir()),
                org.apache.commons.lang.RandomStringUtils
                        .randomAlphabetic(12)
        );
        String fileName = file.getPath();
        String fileContent = org.apache.commons.lang.RandomStringUtils
                .randomAlphabetic(12000);

        // arquivo gz
        String gzFile = file.getPath() + ".gz";

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
            assertEquals(fileContent, new String(outchars));
            // conferindo se o arquivo tem mesmo comprimento da string original
            if (reader.read() > 0)
                fail();
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            // removendo todos arquivos
            file = new File(fileName);
            if (file.exists())
                file.delete();
            if (gzFile != null) {
                File zip = new File(gzFile);
                if (zip.exists())
                    zip.delete();
            }
            if (outGzFileName != null) {
                File zip = new File(outGzFileName);
                if (zip.exists())
                    zip.delete();
            }
            if (outFileName != null) {
                File zip = new File(outFileName);
                if (zip.exists())
                    zip.delete();
            }
        }
    }

}
