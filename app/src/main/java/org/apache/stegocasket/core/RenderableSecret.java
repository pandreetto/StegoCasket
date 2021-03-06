package org.apache.stegocasket.core;

import org.xml.sax.Attributes;

public class RenderableSecret implements Secret {

    private String key;

    private String value;

    private String type;

    private StringBuffer currText;

    public RenderableSecret(String type) {
        this.type = type;
    }

    public String toString() {
        return key;
    }

    public String getId() {
        return key;
    }

    public void setId(String id) {
        key = id;
    }

    public String getType() {
        return type;
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