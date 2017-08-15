package org.apache.stegocasket.core;

import java.io.OutputStream;
import java.io.PrintWriter;

public class SecretWriter {

    private PrintWriter writer;

    public SecretWriter(OutputStream out) {
        writer = new PrintWriter(out);
        writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        writer.println("<secretlist>");
    }

    /*
     * TODO check special XML chars
     */
    public void write(Secret secret) {
        String tmps = secret.toXML();
        writer.print("<secret class=\"" + secret.getType() + "\">\n");
        writer.print(tmps);
        writer.print("</secret>\n");
    }

    public void close() {
        writer.println("</secretlist>");
        writer.close();
    }
}