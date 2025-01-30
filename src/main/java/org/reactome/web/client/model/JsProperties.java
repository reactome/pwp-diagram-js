package org.reactome.web.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class JsProperties {
    JavaScriptObject prop;

    public JsProperties(JavaScriptObject properties) {
        this.prop = properties;
    }

    public String get(String name) {
        return getImpl(this.prop, name);
    }

    public String get(String name, String defaultValue) {
        String value = get(name);
        return value == null ? defaultValue : value;
    }

    public int getInt(String name) {
        String val = get(name);
        return val == null ? 0 : Integer.parseInt(val);
    }

    public int getInt(String name, int defaultValue) {
        String value = get(name);
        return value == null ? defaultValue : Double.valueOf(value).intValue();
    }

    public boolean getBoolean(String name) {
        return Boolean.parseBoolean(get(name));
    }
    public boolean getBoolean(String name, boolean defaultValue) {
        String value = get(name);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public List<String> getArray(String name) {
        JsArrayString arrayImpl = getArrayImpl(prop, name);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < arrayImpl.length(); i++) {
            result.add(arrayImpl.get(i));
        }
        return result;
    }

    private static native String getImpl(JavaScriptObject p, String name) /*-{
        return p[name] ? p[name].toString() : p[name] === false ? "false" : null;
    }-*/;

    private static native JsArrayString getArrayImpl(JavaScriptObject p, String name)/*-{
        return p[name] ? p[name] : [];
    }-*/;
}
