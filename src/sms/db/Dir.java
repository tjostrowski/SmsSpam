/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tomek
 */
public class Dir extends Entity implements Serializable {
    protected String name;
    protected String description;
    protected List<SmsDir> dirSmses = new ArrayList<>();
    protected FilterDir filterDir;

    public Dir(final Dir dir) {
        this(dir.getId(), dir.getName(), dir.getDescription());
    }
    
    public Dir(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dirSmses = new ArrayList<>();
    }
    
    public Dir(String name, String description) {
        this(0L, name, description);
    }

    public List<SmsDir> getDirSmses() {
        return dirSmses;
    }

    public void setDirSmses(List<SmsDir> dirSmses) {
        this.dirSmses = dirSmses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FilterDir getFilterDir() {
        return filterDir;
    }

    public void setFilterDir(FilterDir filterDir) {
        this.filterDir = filterDir;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Dir other = (Dir) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Utils.equals(this.name, other.name)) {
            return false;
        }
        if (!Utils.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }
}
