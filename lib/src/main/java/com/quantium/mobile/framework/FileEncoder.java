package com.quantium.mobile.framework;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.quantium.mobile.framework.utils.Base64Coder;

public class FileEncoder {
	//
	// public static byte[] encode(String filePath) {
	// FileOutputStream file = new FileOutputStream(filePath);
	// ByteArrayInputStream bais = new ByteArrayInputStream(file.);
	// return
	// }
	//
	// public static void setImplementation() {
	//
	// }
	//
	public static String encodeFile(String file) throws IOException {
		File source = new File(file);
		if (!source.exists()) {
			return null;
		}
		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(source);
			int read = 0;
			while ((read = ios.read(buffer)) != -1)
				ous.write(buffer, 0, read);
		} finally {
			try {
				if (ous != null)
					ous.close();
			} catch (IOException e) {
				// swallow, since not that important
			}
			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
				// swallow, since not that important
			}
		}
		return new String(Base64Coder.encode(ous.toByteArray()));
	}

	public static void decodeFile(String base64, String destination) throws IOException {
		if(destination == null){
			throw new RuntimeException("Destination nao pode ser nulo!");
		}
		byte[] btDataFile = Base64Coder.decode(base64.toCharArray());
		File of = new File(destination);
		FileOutputStream osf = new FileOutputStream(of);
		osf.write(btDataFile);
		osf.flush();
		osf.close();
	}
}
