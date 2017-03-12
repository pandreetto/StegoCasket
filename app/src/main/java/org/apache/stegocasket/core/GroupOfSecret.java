package org.apache.stegocasket.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.Attributes;

public class GroupOfSecret
        extends ArrayList<Secret>
        implements Secret, Iterable<Secret> {

    public static final long serialVersionUID = 1472057412L;

    private String id;

    private Secret currSecret;

    public GroupOfSecret() {
        super();
        id = null;
        currSecret = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void remove(String secId) {
        Secret tmpsec = null;
        for (Secret sec : this) {
            if (sec.getId().equals(secId)) {
                tmpsec = sec;
                break;
            }
        }

        super.remove(tmpsec);
    }

    @Deprecated
    public Iterator<Secret> iterator() {
        return super.iterator();
    }

    public void processStartElement(String qName, Attributes attributes) {
        if (qName.equals("group")) {
            id = attributes.getValue("id");
        } else if (qName.equals("secretitem")) {
            String className = attributes.getValue("class");

            // patch for back compatibility
            if (className.startsWith("oss.crypto.casket")) {
                className = className.replace("oss.crypto.casket", "org.apache.stegocasket.core");
            }

            try {
                currSecret = (Secret) Class.forName(className).newInstance();
            } catch (Exception ex) {
                /*
                 * TODO handle exception
                 */
            }
        } else {
            currSecret.processStartElement(qName, attributes);
        }
    }

    public void processEndElement(String qName) {
        if (qName.equals("secretitem")) {

            this.add(currSecret);
            currSecret = null;

        } else if (!qName.equals("group")) {

            currSecret.processEndElement(qName);

        }
    }

    public void processText(char[] ch, int start, int length) {
        if (currSecret != null) {
            currSecret.processText(ch, start, length);
        }
    }

    public String toXML() {

        StringBuffer buff = new StringBuffer();
        buff.append("<group id=\"").append(id).append("\"/>\n");
        for (Secret secItem : this) {
            buff.append("<secretitem class=\"");
            buff.append(secItem.getClass().getName()).append("\">");
            buff.append(secItem.toXML());
            buff.append("</secretitem>\n");
        }

        return buff.toString();
    }

    public String toString() {
        return getId();
    }

}