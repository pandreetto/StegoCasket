package org.apache.stegocasket.core;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SecretParser
        extends DefaultHandler {

    private SAXParser saxParser;

    private InputSource input;

    private Secret currSecret;

    private ArrayList<Secret> secretList;

    public SecretParser(Reader xmlReader) throws IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        try {
            saxParser = spf.newSAXParser();
            input = new InputSource(xmlReader);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }

        secretList = new ArrayList<Secret>();
    }

    public void parse()
            throws IOException {
        try {
            saxParser.parse(input, this);
        } catch (Exception ex) {
            Log.e(this.getClass().getName(), ex.getMessage(), ex);
            throw new IOException(ex.getMessage());
        }
    }

    public void startElement(String uri, String name, String qName, Attributes attributes)
            throws SAXParseException {

        if (qName.equals("secret")) {
            String className = attributes.getValue("class");

            // patch for back compatibility
            if (className.startsWith("oss.") || className.startsWith("org.")) {
                className = className.substring(className.lastIndexOf('.') + 1);
            }

            if (className.equals("GroupOfSecret")) {
                currSecret = new GroupOfSecret();
            } else {
                currSecret = new RenderableSecret(className);
            }

        } else if (!qName.equals("secretlist")) {

            currSecret.processStartElement(qName, attributes);

        }
    }

    public void endElement(String uri, String name, String qName)
            throws SAXParseException {

        if (qName.equals("secret")) {
            secretList.add(currSecret);
            currSecret = null;
        } else if (!qName.equals("secretlist")) {
            currSecret.processEndElement(qName);
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXParseException {
        if (currSecret != null) {
            currSecret.processText(ch, start, length);
        }
    }

    public ArrayList<Secret> getSecrets() {
        return secretList;
    }

}
