package model;

import java.io.Serializable;

public class TrashItem implements Serializable {
    private String name;
    private String type; // "plastic", "paper", "metal"

    public TrashItem(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
