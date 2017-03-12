package org.apache.stegocasket.core;

import org.apache.stegocasket.R;
import org.xml.sax.Attributes;

public class PropertySecret
        implements RenderableSecret {

    private String key;

    private String value;

    private StringBuffer currText;

    public PropertySecret() {
    }

    public String toString() {
        return key;
    }

    public String getId() {
        return key;
    }

    public int getLayoutId() {
        /*
        TODO define secret layout
         */
        //return R.layout.secretprop;
        return 0;
    }

    public void setId(String id) {
        key = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void processStartElement(String qName, Attributes attributes) {
        currText = new StringBuffer();
    }

    public void processEndElement(String qName) {
        if (qName.equals("key")) {
            key = currText.toString().trim();
        } else {
            value = currText.toString().trim();
        }
        currText = null;

    }

    public void processText(char[] ch, int start, int length) {
        if (currText != null) {
            currText.append(ch, start, length);
        }
    }

    public String toXML() {
        StringBuffer buff = new StringBuffer();
        buff.append("<key>").append(key).append("</key>\n");
        buff.append("<value>").append(value).append("</value>\n");
        return buff.toString();
    }
}