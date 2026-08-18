package com.dagong.survive.domain;

public class CareerDef {

    private String id;
    private String name;
    private String blurb;
    private String workApp;
    private Attrs attrs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public String getWorkApp() {
        return workApp;
    }

    public void setWorkApp(String workApp) {
        this.workApp = workApp;
    }

    public Attrs getAttrs() {
        return attrs;
    }

    public void setAttrs(Attrs attrs) {
        this.attrs = attrs;
    }
}
