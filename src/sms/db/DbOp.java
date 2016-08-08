/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tomek
 */
public class DbOp {
    protected SmsDirDbHelper dbHelper;
    protected Context context;
    protected static DbOp instance;
    
    public static DbOp getInstance(Context context) {
        if (instance == null) {
            instance = new DbOp(context);
        }
        return instance;
    }
    
    public static DbOp getInstance() {
        return getInstance(null);
    }
    
    private DbOp(Context context) {
        this.context = context;
    }
    
    public SmsDirDbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new SmsDirDbHelper(context);
        }
        return dbHelper;
    }
    
    public Dir putDir(final String dirname, final String desc) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(DirContract.DirContractEntry.COLUMN_NAME_DIR_NAME, dirname);
        values.put(DirContract.DirContractEntry.COLUMN_NAME_DIR_DESCRIPTION, desc);
        
        long newDirId = db.insert(DirContract.DirContractEntry.TABLE_NAME, null, values);
        return new Dir(newDirId, dirname, desc);
    }
    
    public Dir updateDir(final Dir dir) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(DirContract.DirContractEntry.COLUMN_NAME_DIR_NAME, dir.getName());
        values.put(DirContract.DirContractEntry.COLUMN_NAME_DIR_DESCRIPTION, dir.getDescription());
        
        String whereClause = DirContract.DirContractEntry._ID + " = ?";
        String[] whereArgs = new String[] { String.valueOf(dir.getId()) };
        
        int numUpdated = db.update(DirContract.DirContractEntry.TABLE_NAME, values, whereClause, whereArgs);
        
        return dir;
    }
    
    public Dir getDir(final String dirname) {
        return getDir(dirname, false);
    }
    
    public Dir getDir(final String dirname, final boolean withDeps) {
        SQLiteDatabase db = getDbHelper().getReadableDatabase();
        
        String[] projection = {
            DirContract.DirContractEntry._ID,
            DirContract.DirContractEntry.COLUMN_NAME_DIR_NAME,
            DirContract.DirContractEntry.COLUMN_NAME_DIR_DESCRIPTION
        };
                
        String whereClause = DirContract.DirContractEntry.COLUMN_NAME_DIR_NAME + " = ?";
        String[] whereArgs = new String[] { dirname };
        String orderBy = null;
        
        Cursor c = db.query(DirContract.DirContractEntry.TABLE_NAME, projection, whereClause, whereArgs, null, null, orderBy);
        if (c.getCount() == 0) {
            return null;
        }
        
        c.moveToFirst();
        Dir dir = new Dir(
                c.getLong(c.getColumnIndex(DirContract.DirContractEntry._ID)),
                c.getString(c.getColumnIndex(DirContract.DirContractEntry.COLUMN_NAME_DIR_NAME)),
                c.getString(c.getColumnIndex(DirContract.DirContractEntry.COLUMN_NAME_DIR_DESCRIPTION))
        );
        
        if (withDeps) {
            dir.setDirSmses(getDirSmses(dir));
            dir.setFilterDir(getFilterDir(dir));
        } else {
            dir.setDirSmses(Collections.EMPTY_LIST);
            dir.setFilterDir(null);
        }
        
        c.close();
        
        return dir;
    }
    
    public boolean deleteDir(Dir dir) {
        if (dir == null) {
            return false;
        }
        
        deleteDirSmses(dir, true);
        deleteDirSmses(dir, false);
        deleteDirFilters(dir);
        
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        String whereClause = DirContract.DirContractEntry._ID + " = ? ";                
        String[] whereArgs = new String[] { String.valueOf(dir.getId()) };
        
        db.delete(DirContract.DirContractEntry.TABLE_NAME, whereClause, whereArgs);
        return true;
    }
    
    public boolean deleteDirSmses(Dir dir, boolean fromTap) {
        if (dir == null) {
            return false;
        }
        
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        String whereClause = SmsDirContract.SmsDirContractEntry.COLUMN_NAME_DIR_ID + " = ? " +
                "AND " + SmsDirContract.SmsDirContractEntry.COLUMN_NAME_FROM_TAP + " = ?";               
        String[] whereArgs = new String[] { String.valueOf(dir.getId()), (fromTap) ? "1" : "0" };
        
        db.delete(SmsDirContract.SmsDirContractEntry.TABLE_NAME, whereClause, whereArgs);
        return true;
    }
    
    public boolean deleteDirFilters(Dir dir) {
        if (dir == null) {
            return false;
        }
        
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        String whereClause = FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DIR_ID + " = ?";
        String[] whereArgs = new String[] { String.valueOf(dir.getId()) };
        
        db.delete(FilterDirContract.FilterDirContractEntry.TABLE_NAME, whereClause, whereArgs);
        return true;
    }
    
    public List<SmsDir> getDirSmses(Dir dir) {
        if (dir == null) {
            return Collections.EMPTY_LIST;
        }
        
        SQLiteDatabase db = getDbHelper().getReadableDatabase();
        
        String[] projection = {
            SmsDirContract.SmsDirContractEntry._ID,
            SmsDirContract.SmsDirContractEntry.COLUMN_NAME_SMS_ID            
        };
                
        String whereClause = SmsDirContract.SmsDirContractEntry.COLUMN_NAME_DIR_ID + " = ?";
        String[] whereArgs = new String[] { String.valueOf(dir.getId()) };
        String orderBy = SmsDirContract.SmsDirContractEntry.COLUMN_NAME_SMS_ID + " desc";
        
        Cursor c = db.query(SmsDirContract.SmsDirContractEntry.TABLE_NAME, projection, whereClause, whereArgs, null, null, orderBy);
        if (c.getCount() == 0) {
            dir.setDirSmses(Collections.EMPTY_LIST);
            return Collections.EMPTY_LIST;
        }        
        
        List<SmsDir> dirSmses = new ArrayList<SmsDir>();
        c.moveToFirst();
        do {
            dirSmses.add(new SmsDir(
                    c.getLong(c.getColumnIndex(SmsDirContract.SmsDirContractEntry._ID)),
                    c.getLong(c.getColumnIndex(SmsDirContract.SmsDirContractEntry.COLUMN_NAME_SMS_ID)),
                    dir));
        } while (c.moveToNext());
        
        c.close();
        
        dir.setDirSmses(dirSmses);
        
        return dirSmses;
    }        
    
    public FilterDir getFilterDir(Dir dir) {
        if (dir == null) {
            return null;
        }
        
        SQLiteDatabase db = getDbHelper().getReadableDatabase();
        
        String[] projection = {
            FilterDirContract.FilterDirContractEntry._ID,
            FilterDirContract.FilterDirContractEntry.COLUMN_NAME_FROM,
            FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DATE_FROM,
            FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DATE_TO,
            FilterDirContract.FilterDirContractEntry.COLUMN_NAME_TEXT,
        };
                
        String whereClause = FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DIR_ID + " = ?";
        String[] whereArgs = new String[] { String.valueOf(dir.getId()) };
        String orderBy = null;
        
        Cursor c = db.query(FilterDirContract.FilterDirContractEntry.TABLE_NAME, projection, whereClause, whereArgs, null, null, orderBy);
        if (c.getCount() == 0) {
            return null;
        }
        
        c.moveToFirst();
        
        FilterDir filterDir = new FilterDir( dir, c.getLong(c.getColumnIndex(FilterDirContract.FilterDirContractEntry._ID)) )
                .setFrom( loadString(c, FilterDirContract.FilterDirContractEntry.COLUMN_NAME_FROM) )
                .setDateFrom( loadDate(c, FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DATE_FROM) )
                .setDateTo( loadDate(c, FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DATE_TO) )
                .setText( loadString(c, FilterDirContract.FilterDirContractEntry.COLUMN_NAME_TEXT) );
        
        c.close();
        
        dir.setFilterDir(filterDir);
                
        return filterDir;
    }
    
    public String loadString(Cursor c, final String columnName) {
        String ret = null;
        try {
            ret = c.getString(c.getColumnIndex(columnName));
        } catch (Exception e) {
            // column is NULL
        }
        return ret;
    }
    
    public Date loadDate(Cursor c, final String columnName) {
        Date ret = null;
        try {
            final int columnIndex = c.getColumnIndex(columnName);
            if (c.isNull(columnIndex)) {
                return null;
            }
            long milis = c.getLong(columnIndex);
            ret = new Date(milis);
        } catch (Exception e) {
            // column is NULL
        }
        return ret;
    }
    
    public Long loadLong(Cursor c, final String columnName) {
        Long ret = null;
        try {
            final int columnIndex = c.getColumnIndex(columnName);
            if (c.isNull(columnIndex)) {
                return null;
            }
            return c.getLong(columnIndex);
        } catch (Exception e) {
            // column is NULL
        }
        return ret;
    }
    
    public List<Dir> getDirs() {
        return getDirs(false);
    }
    
    public List<Dir> getDirs(final boolean withDeps) {
        SQLiteDatabase db = getDbHelper().getReadableDatabase();
        
        String[] projection = {
            DirContract.DirContractEntry._ID,
            DirContract.DirContractEntry.COLUMN_NAME_DIR_NAME,
            DirContract.DirContractEntry.COLUMN_NAME_DIR_DESCRIPTION
        };
                
        String whereClause = null;
        String[] whereArgs = null;
        String orderBy = DirContract.DirContractEntry.COLUMN_NAME_DIR_NAME + " asc";
        
        Cursor c = db.query(DirContract.DirContractEntry.TABLE_NAME, projection, whereClause, whereArgs, null, null, orderBy);
        if (c.getCount() == 0) {
            return Collections.EMPTY_LIST;
        }
        
        List<Dir> dirs = new ArrayList<Dir>();
        c.moveToFirst();
        do {
            Dir dir = new Dir(c.getLong(c.getColumnIndex(DirContract.DirContractEntry._ID)),
                    c.getString(c.getColumnIndex(DirContract.DirContractEntry.COLUMN_NAME_DIR_NAME)),
                    c.getString(c.getColumnIndex(DirContract.DirContractEntry.COLUMN_NAME_DIR_DESCRIPTION)));
            
            if (withDeps) {
                getDirSmses(dir);
                getFilterDir(dir);
            }
            
            dirs.add(dir);
        } while (c.moveToNext());
        
        c.close();
        
        return dirs;
    }
    
    public long putSmsDir(final long dirId, final long smsId, final boolean fromTap) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(SmsDirContract.SmsDirContractEntry.COLUMN_NAME_DIR_ID, Long.valueOf(dirId));
        values.put(SmsDirContract.SmsDirContractEntry.COLUMN_NAME_SMS_ID, Long.valueOf(smsId));
        values.put(SmsDirContract.SmsDirContractEntry.COLUMN_NAME_FROM_TAP, Boolean.valueOf(fromTap));
        
        long newId = db.insert(SmsDirContract.SmsDirContractEntry.TABLE_NAME, null, values);
        return newId;
    }
    
    public long putSmsDir(final String dirName, final long smsId, final boolean fromTap) {
        Dir dir = getDir(dirName);
        if (dir != null) {
            return putSmsDir(dir.getId(), smsId, fromTap);
        }
        return -1L;
    }
    
    public long putFilterDir(final FilterDir filterDir) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        
        if (!filterDir.containsAny() || (filterDir.getDir() == null)) {
            return -1L;
        }
        
        ContentValues values = new ContentValues();
        values.put(FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DIR_ID, filterDir.getDir().getId());
//        if (filterDir.getFrom() != null) {
            values.put(FilterDirContract.FilterDirContractEntry.COLUMN_NAME_FROM, filterDir.getFrom());
//        }
//        if (filterDir.getDateFrom() != null) {
            values.put(FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DATE_FROM, (filterDir.getDateFrom() != null) ? filterDir.getDateFrom().getTime() : null);
//        }
//        if (filterDir.getDateTo() != null) {
            values.put(FilterDirContract.FilterDirContractEntry.COLUMN_NAME_DATE_TO, (filterDir.getDateTo() != null) ? filterDir.getDateTo().getTime() : null);
//        }
//        if (filterDir.getText() != null) {
            values.put(FilterDirContract.FilterDirContractEntry.COLUMN_NAME_TEXT, filterDir.getText());
//        }
        
        long newRowId = db.insert(FilterDirContract.FilterDirContractEntry.TABLE_NAME, null, values);
        filterDir.setId(newRowId);
        return newRowId;
    }
    
    public boolean spamPatternExists(final String text, final String hash) {
        SQLiteDatabase db = getDbHelper().getReadableDatabase();
        
        String[] projection = {
            DirContract.DirContractEntry._ID,            
        };
                
        String whereClause = SpamPatternContract.SpamPatternContractEntry.COLUMN_NAME_SP_HASH + " = ?";
        String[] whereArgs = new String[] { hash };
        String orderBy = null;
        
        Cursor c = db.query(SpamPatternContract.SpamPatternContractEntry.TABLE_NAME, projection, whereClause, whereArgs, null, null, orderBy);
        final boolean hasAny = (c.getCount() > 0);
        c.close();
        
        return hasAny;
    }
    
    public SpamPattern putSpamPattern(final String text, final String hash) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(SpamPatternContract.SpamPatternContractEntry.COLUMN_NAME_SP_TEXT, text);
        values.put(SpamPatternContract.SpamPatternContractEntry.COLUMN_NAME_SP_HASH, hash);
        
        long newId = db.insert(SpamPatternContract.SpamPatternContractEntry.TABLE_NAME, null, values);
        return new SpamPattern(newId, text, hash);
    }
    
    public List<SpamPattern> getSpamPatterns() {
        SQLiteDatabase db = getDbHelper().getReadableDatabase();
        
        String[] projection = {
            SpamPatternContract.SpamPatternContractEntry._ID,
            SpamPatternContract.SpamPatternContractEntry.COLUMN_NAME_SP_TEXT,
            SpamPatternContract.SpamPatternContractEntry.COLUMN_NAME_SP_HASH
        };
                
        String whereClause = null;
        String[] whereArgs = null;
        String orderBy = null;
        
        Cursor c = db.query(SpamPatternContract.SpamPatternContractEntry.TABLE_NAME, projection, whereClause, whereArgs, null, null, orderBy);
        
        List<SpamPattern> patterns = new ArrayList<>();
        c.moveToFirst();
        do {
            patterns.add(new SpamPattern(c.getLong(c.getColumnIndex(SpamPatternContract.SpamPatternContractEntry._ID)),
                    c.getString(c.getColumnIndex(SpamPatternContract.SpamPatternContractEntry.COLUMN_NAME_SP_TEXT)),
                    c.getString(c.getColumnIndex(SpamPatternContract.SpamPatternContractEntry.COLUMN_NAME_SP_HASH))));
        } while (c.moveToNext());
        
        c.close();
        return patterns;
    }
    
    protected String getMD5Hash(String s) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
        md.update(s.getBytes(), 0, s.length());
        return new BigInteger(1, md.digest()).toString();
    }
    
    public void fillSpamPatterns(File file) {
        fillSpamPatterns(file, null);
    }
    
    public void fillSpamPatterns(File file, String encoding) {
        if (!file.exists()) {
            return;
        }
        
        try {
            if (encoding == null) {
                encoding = "UTF-8";
            }
            
            BufferedReader br = null;
            try {
//                (
//                    br = new BufferedReader(
//                    new InputStreamReader(
//                            new BufferedInputStream(new FileInputStream(file)), encoding))) {
                br = new BufferedReader(
                    new InputStreamReader(
                            new BufferedInputStream(new FileInputStream(file)), encoding));
                
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.equals("")) {
                        final String hash = getMD5Hash(line);
                        if (!spamPatternExists(line, hash)) {
                            putSpamPattern(line, hash);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {}   
                }
            }    
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
