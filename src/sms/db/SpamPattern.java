/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

/**
 *
 * @author tomek
 */
public class SpamPattern {
    protected long id;
    protected String text;
    protected String hash;

    public SpamPattern(long id, String name, String hash) {
        this.id = id;
        this.text = name;
        this.hash = hash;
    }

    public SpamPattern(String name, String hash) {
        this.text = name;
        this.hash = hash;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String name) {
        this.text = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
