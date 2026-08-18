package com.dagong.survive.domain;

import java.util.Collections;
import java.util.List;

public class EndingDef {

    private String id;
    private String name;
    private int priority;
    private String shareHook;
    private List<String> lines = Collections.emptyList();

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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getShareHook() {
        return shareHook;
    }

    public void setShareHook(String shareHook) {
        this.shareHook = shareHook;
    }

    public List<String> getLines() {
        return lines == null ? Collections.<String>emptyList() : lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
