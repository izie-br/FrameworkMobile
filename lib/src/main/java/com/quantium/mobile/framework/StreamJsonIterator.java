package com.quantium.mobile.framework;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Reader;
import java.util.Iterator;


public class StreamJsonIterator implements Iterator<JSONObject> {

    private Reader reader;
    private FrameworkJSONTokener current;
    private JSONObject nextObj;

    public StreamJsonIterator(Reader reader) {
        this.reader = reader;
        this.current = new FrameworkJSONTokener(reader);
    }

    @Override
    public boolean hasNext() {
        if (nextObj == null)
            nextObj = getNextfromStream();
        return (nextObj != null);
    }

    @Override
    public JSONObject next() {
        if (nextObj == null)
            return getNextfromStream();
        JSONObject obj = nextObj;
        nextObj = null;
        return obj;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            reader.close();
        } finally {
            super.finalize();
        }
    }

    private JSONObject getNextfromStream() {
        JSONObject json = null;
        try {
            for (; ; ) {
                char c = current.nextClean();
                if (c == 0)
                    return null;
                if (c == ']')
                    return null;
                if (c == '{')
                    break;
            }                   /* Encontrar a primeira '{' */
            current.back();     /* E voltar 1 caractere em seguida */
            json = current.nextJSONObject();
        } catch (JSONException e) {
            // TODO conferir se o arquivo acabou
        }
        return json;
    }
}
