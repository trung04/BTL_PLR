/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;

/**
 *
 * @author D E L L
 */
public class Message implements Serializable {

    private String type;
    private Object content;

    public Message(String type, Object content) {
        this.type = type;
        this.content = content;
    }

    // Getters
    public String getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }
}
