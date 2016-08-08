/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author tomek
 */
public class FilterDir extends Entity implements Serializable {
    
    public enum Contains {
        ONLY_NAME(1),
        FROM(1 << 1),
        DATE_FROM(1 << 2),
        DATE_TO(1 << 3),
        TEXT(1 << 4);
        
        int val;
        
        Contains(int i) {
            this.val = i;
        }
        
        int getVal() {
            return val;
        }
    };
    
    protected Dir dir;
    protected String from;
    protected Date dateFrom;
    protected Date dateTo;
    protected String text;
        
    protected EnumSet<Contains> contains;
    
    public FilterDir(final Dir dir, long id) {
        contains = EnumSet.noneOf(Contains.class);
        setId(id);
        setDir(dir);
    }        
    
    public FilterDir(final Dir dir) {
        contains = EnumSet.noneOf(Contains.class);
        setDir(dir);
    }        
    
    public FilterDir(final Dir dir, final String from, final Date dateFrom, final Date dateTo,
            final String text) {
        contains = EnumSet.noneOf(Contains.class);
        setDir(dir);
        setFrom(from);
        setDateFrom(dateFrom);
        setDateTo(dateTo);
        setText(text);
    }
    
    public boolean contains(Contains name) {
        return contains.contains(name);
    }
    
    public boolean containsAll(Contains... names) {
        return contains.containsAll(Arrays.asList(names));
    }
    
    public boolean containsAny() {
        int cc = toCompressedContains();
        if (cc > 0) {
            cc -= Contains.ONLY_NAME.getVal();
        }
        return (cc > 0);
    }
    
    public int toCompressedContains() {
        int val = 0;
        for (Contains c : contains) {
            val |= c.getVal();
        }
        return val;
    }
    
    public EnumSet<Contains> fromCompressedContains(int val) {
        EnumSet<Contains> all = EnumSet.allOf(Contains.class);
        EnumSet<Contains> dest = EnumSet.noneOf(Contains.class);
        for (Contains c : all) {
            if ((val & c.getVal()) == c.getVal()) {
                dest.add(c);
            }
        }
        return dest;
    }

    public Dir getDir() {
        return dir;
    }

    public FilterDir setDir(Dir dir) {
        this.dir = dir;
        contains.add(Contains.ONLY_NAME);
        return this;
    }

    public String getFrom() {
        return from;
    }

    public FilterDir setFrom(String from) {
        this.from = from;
        if (from != null) {
            contains.add(Contains.FROM);
        }
        return this;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public FilterDir setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
        if (dateFrom != null) {
            contains.add(Contains.DATE_FROM);
        }
        return this;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public FilterDir setDateTo(Date dateTo) {
        this.dateTo = dateTo;
        if (dateTo != null) {
            contains.add(Contains.DATE_TO);
        }
        return this;
    }

    public String getText() {
        return text;
    }

    public FilterDir setText(String text) {
        this.text = text;
        if (text != null) {
            contains.add(Contains.TEXT);
        }
        return this;
    }

    public EnumSet<Contains> getContains() {
        return contains;
    }

    public void setContains(EnumSet<Contains> contains) {
        this.contains = contains;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FilterDir other = (FilterDir) obj;
        if (!Utils.equals(this.from, other.from)) {
            return false;
        }
        if (!Utils.equals(this.dateFrom, other.dateFrom)) {
            return false;
        }
        if (!Utils.equals(this.dateTo, other.dateTo)) {
            return false;
        }
        if (!Utils.equals(this.text, other.text)) {
            return false;
        }
        return true;
    }
}
