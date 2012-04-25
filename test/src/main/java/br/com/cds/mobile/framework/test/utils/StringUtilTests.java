package br.com.cds.mobile.framework.test.utils;

import java.util.Arrays;

import br.com.cds.mobile.framework.test.TestActivity;
import br.com.cds.mobile.framework.utils.StringUtil;
import android.test.ActivityInstrumentationTestCase2;

public class StringUtilTests extends
		ActivityInstrumentationTestCase2<TestActivity> {

	public StringUtilTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	public void testCollectionToCSV(){
		String input[] = { "val0", "val1,val1", "\"val2\"", "val3\nval3\rval3" };
		String expectedOtput =
				input[0]+","+
				"\""+input[1]+"\","+
				"\""+input[2].replaceAll("\\\"", "\"\"")+"\","+
				"\""+input[3]+"\"";
		String output = StringUtil.collectionToCSV(Arrays.asList(input));
		assertEquals(expectedOtput, output);
	}

	public void testStringIsNull(){
		String blankStrings[] = {null,""," ", "\t"};
		for(String blankString : blankStrings)
			assertTrue(StringUtil.isNull(blankString));
	}

	public void testHashes(){
		String sha1Regex = "[0-9a-fA-F]{40}";
		String md5Regex = "[0-9a-fA-F]{32}";

		String strA = new String("igual");
		String strB = new String("igual");
		String strC = new String("diferente");

		String shaA = StringUtil.SHA1(strA);
		String shaB = StringUtil.SHA1(strB);
		String shaC = StringUtil.SHA1(strC);

		String mdA = StringUtil.toMd5(strA);
		String mdB = StringUtil.toMd5(strB);
		String mdC = StringUtil.toMd5(strC);

		for(String sha1X : new String[]{shaA,shaB,shaC}){
			assertTrue(sha1X.matches(sha1Regex));
		}

		for(String md5X : new String[]{mdA,mdB,mdC}){
			assertTrue(md5X.matches(md5Regex));
		}

		assertEquals(shaA, shaB);
		assertEquals(mdA, mdB);

		assertFalse(shaA.equals(shaC));
		assertFalse(mdA.equals(mdC));

	}

	public void testBase64(){
		String strA = new String("igual");
		String strB = new String("igual");
		String strC = new String("diferente");

		assertEquals(strA, StringUtil.fromBase64(StringUtil.toBase64(strB)));

		assertFalse(strA.equals(StringUtil.fromBase64(StringUtil.toBase64(strC))));

	}

}
