package com.quantium.mobile.framework.communication;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.quantium.mobile.framework.MapSerializable;
import com.quantium.mobile.framework.logging.LogPadrao;

/**
 * Transforma o Map
 * <pre>
 * {
 *     "nome" : "Item1" ,
 *     "valor" : {
 *         "subitema": 1,
 *         "subitemb": 2
 *     }
 * }
 * </pre>
 * Em:
 * <pre>
 *    nome=Item1&valor.subitema=1&valor.subitemb=2
 * </pre>
 * 
 * @author Igor Soares
 */
public class IndexedKeyParametersSerializer extends ParametersSerializer{

	private String pattern = "%1$s.%2$s";

	public IndexedKeyParametersSerializer(String pattern) {
		super();
		this.pattern = pattern;
	}

	@Override
	public List<NameValuePair> serialize(Map<String, Object> map) {
		List<NameValuePair> params = new ArrayList<NameValuePair>(map.size());
		serializeObj(null, map, params);
		return params;
	}

	@Override
	public HttpEntity getEntity(Map<String, Object> map) throws UnsupportedEncodingException {
		return new UrlEncodedFormEntity(serialize(map));
	}

	public void serializeObj(
			String parentKey, Object obj, List<NameValuePair> out)
	{
		if (obj == null)
			return;
		if (obj instanceof Map){
			Map<?,?> map = (Map<?,?>)obj;
			for (Object k : map.keySet()){
				if (k == null){
					LogPadrao.e("parametro com chave null");
					continue;
				}
				String keystr = parentKey == null?
					k.toString():
					String.format(pattern,parentKey, k.toString());
				Object val = map.get(k);
				if (val == null)
					continue;
				serializeObj(keystr, val, out);
			}
		} else if (obj instanceof MapSerializable){
			serializeObj(parentKey, ((MapSerializable<?>)obj).toMap(), out);
		} else if (obj instanceof Collection){
			Iterator<?> it = ((Collection<?>)obj).iterator();
			Integer i=0;
			while (it.hasNext()){
				Object val = it.next();
				if (val == null)
					continue;
				String keystr = parentKey == null?
					i.toString():
					String.format(pattern,parentKey, i.toString());
				serializeObj(keystr,val, out);
				i++;
			}
		} else {
			out.add(new BasicNameValuePair(parentKey, obj.toString()));
		}
	}

}
