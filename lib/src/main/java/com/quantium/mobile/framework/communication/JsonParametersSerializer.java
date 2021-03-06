package com.quantium.mobile.framework.communication;

import com.quantium.mobile.framework.utils.JSONUtils;
import com.quantium.mobile.framework.utils.StringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;

import java.util.List;
import java.util.Map;

/**
 * Transforma o map em JSON simples.
 *
 * @author Igor Soares
 */
public class JsonParametersSerializer extends ParametersSerializer {

    @Override
    public List<NameValuePair> serialize(Map<String, Object> map) {

        return new InnerJsonParametersSerializer().serialize(map);
    }

    @Override
    public HttpEntity getEntity(Map<String, Object> map) throws Exception {
        Object json = JSONUtils.parseToJson(map);
        return new StringEntity(json.toString(), StringUtil.DEFAULT_ENCODING);
    }


}
