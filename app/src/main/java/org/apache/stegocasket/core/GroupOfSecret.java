package org.apache.stegocasket.core;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Comparator;

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

    public String getType() {
        return "GroupOfSecret";
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

    public Secret get(String secId) {
        for (Secret sec : this) {
            if (sec.getId().equals(secId))
                return sec;
        }
        return null;
    }

    public void processStartElement(String qName, Attributes attributes) {
        if (qName.equals("group")) {
            id = attributes.getValue("id");
        } else if (qName.equals("secretitem")) {
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
            buff.append(secItem.getType()).append("\">");
            buff.append(secItem.toXML());
            buff.append("</secretitem>\n");
        }

        return buff.toString();
    }

    public String toString() {
        return getId();
    }

    public Comparator<Secret> getComparator() {
        return new Comparator<Secret>() {
            @Override
            public int compare(Secret s1, Secret s2) {
                return s1.getId().compareTo(s2.getId());
            }
        };
    }

}