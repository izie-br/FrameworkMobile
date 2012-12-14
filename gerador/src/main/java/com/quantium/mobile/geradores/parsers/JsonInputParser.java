package com.quantium.mobile.geradores.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;

public class JsonInputParser implements InputParser {

	public static final String INPUT_PARSER_IDENTIFIER = "json";

	@Override
	public Collection<JavaBeanSchema> getSchemas(File inputFile,
			GeneratorConfig information,
			Map<String, Object> defaultProperties) throws GeradorException {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(inputFile);
			String fileContent = IOUtils.toString(inputStream);
			JSONObject json = new JSONObject(fileContent);
			String name = json.getString("projectName");
			List<JSONObject> packageInfos = getPackagesInformations(json);
		} catch (IOException e) {
			throw new GeradorException(e);
		} catch (JSONException e) {
			throw new GeradorException(e);
		}
		return null;
	}

	private List<JSONObject> getPackagesInformations(JSONObject json)
			throws JSONException {
		JSONArray packageList = json.getJSONArray("packageList");
		List<JSONObject> packageInfos = new ArrayList<JSONObject>();
		for (int i = 0; i < packageList.length(); i++) {
			packageInfos.add(packageList.getJSONObject(i));
		}
		return packageInfos;
	}

}
