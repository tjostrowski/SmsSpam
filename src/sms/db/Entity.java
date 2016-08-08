/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

import java.io.Serializable;

/**
 *
 * @author tomek
 */
public class Entity implements Serializable {
    protected long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public boolean isTransient() {
        return id <= 0L;
    }
}
