package org.apache.stegocasket.core;

public interface RenderableSecret
        extends Secret {

    public String getValue();

    public void setValue(String value);

    public int getLayoutId();

}