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
public class SmsDir extends Entity implements Serializable {
    protected long smsId;
    protected Dir dir;

    public SmsDir(long id, long smsId, Dir dir) {
        this.id = id;
        this.smsId = smsId;
        this.dir = dir;
    }       

    public SmsDir(long smsId, Dir dir) {
        this.smsId = smsId;
        this.dir = dir;
    }

    public long getSmsId() {
        return smsId;
    }

    public void setSmsId(long smsId) {
        this.smsId = smsId;
    }

    public Dir getDir() {
        return dir;
    }

    public void setDir(Dir dir) {
        this.dir = dir;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SmsDir other = (SmsDir) obj;
        if (this.smsId != other.smsId) {
            return false;
        }
        return true;
    }
}
