/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import java.io.Serializable;
import java.util.Date;
import sms.db.Utils;

/**
 *
 * @author tomek
 */
public class Sms implements Serializable {
    protected Long id;    
    protected String body;
    protected String from;
    protected String to;
    protected Date date;
    protected String subject;
    protected Long threadId;
    protected boolean isInbox;

    public Sms() {
        this("", "", "");
    }
    
    public Sms(final String body) {
        this(body, "", "");
    }
    
    public Sms(final String body, final String from, final String to) {
        this.body = body;
        this.from = from;
        this.to = to;
    }

    public String getBody() {
        return body;
    }

    public Sms setBody(String body) {
        this.body = body;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Sms setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Sms setTo(String to) {
        this.to = to;
        return this;
    }
    
    public long getId() {
        return id;
    }

    public Sms setId(Long id) {
        this.id = id;
        return this;
    }
    
    public Date getDate() {
        return date;
    }

    public Sms setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public Sms setSubject(String subject) {
        this.subject = subject;
        return this;
    }
    
    public Long getThreadId() {
        return threadId;
    }

    public Sms setThreadId(Long threadId) {
        this.threadId = threadId;
        return this;
    }

    public boolean isIsInbox() {
        return isInbox;
    }

    public Sms setIsInbox(boolean isInbox) {
        this.isInbox = isInbox;
        return this;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sms other = (Sms) obj;
        if (!Utils.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
