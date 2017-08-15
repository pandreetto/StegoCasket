package org.apache.stegocasket.core;

import org.xml.sax.Attributes;

public interface Secret {

    public String getId();

    public void setId(String id);

    public String getType();

    public void processStartElement(String qName, Attributes attributes);

    public void processEndElement(String qName);

    public void processText(char[] ch, int start, int length);

    public String toXML();

}