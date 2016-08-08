/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import sms.db.DirContract.DirContractEntry;
import sms.db.FilterDirContract.FilterDirContractEntry;
import sms.db.SmsDirContract.SmsDirContractEntry;
import sms.db.SmsDirContract.SmsDirDayViewContractEntry;
import sms.db.SpamPatternContract.SpamPatternContractEntry;

/**
 *
 * @author tomek
 */
public class SmsDirDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 16;
    public static final String DATABASE_NAME = "SmsDir.db";   
    
    private static final String COMMA_SEP = ",";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String UNIQUE = " UNIQUE";
    
    private static final String SQL_CREATE_SMS_DIR = 
            "CREATE TABLE " + SmsDirContractEntry.TABLE_NAME + " ("
            + SmsDirContractEntry._ID + " INTEGER PRIMARY KEY,"
            + SmsDirContractEntry.COLUMN_NAME_SMS_ID + INTEGER_TYPE + COMMA_SEP
            + SmsDirContractEntry.COLUMN_NAME_DIR_ID + INTEGER_TYPE + COMMA_SEP
            + SmsDirContractEntry.COLUMN_NAME_FROM_TAP + INTEGER_TYPE + COMMA_SEP
            + "UNIQUE (" + SmsDirContractEntry.COLUMN_NAME_SMS_ID + "," + SmsDirContractEntry.COLUMN_NAME_DIR_ID + ")" + COMMA_SEP
            + "FOREIGN KEY(" + SmsDirContractEntry.COLUMN_NAME_DIR_ID + ") REFERENCES " + DirContractEntry.TABLE_NAME + "(" + DirContractEntry._ID + ")"
            + " )";
    
    private static final String SQL_CREATE_FILTER_DIR = 
            "CREATE TABLE " + FilterDirContractEntry.TABLE_NAME + " ("
            + FilterDirContractEntry._ID + " INTEGER PRIMARY KEY,"
            + FilterDirContractEntry.COLUMN_NAME_FROM + TEXT_TYPE + COMMA_SEP
            + FilterDirContractEntry.COLUMN_NAME_DATE_FROM + INTEGER_TYPE + COMMA_SEP
            + FilterDirContractEntry.COLUMN_NAME_DATE_TO + INTEGER_TYPE + COMMA_SEP
            + FilterDirContractEntry.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP
            + FilterDirContractEntry.COLUMN_NAME_DIR_ID + INTEGER_TYPE + COMMA_SEP
            + "FOREIGN KEY(" + FilterDirContractEntry.COLUMN_NAME_DIR_ID + ") REFERENCES " + DirContractEntry.TABLE_NAME + "(" + DirContractEntry._ID + ")"
            + " )";
    
    private static final String SQL_CREATE_DIR = 
            "CREATE TABLE " + DirContractEntry.TABLE_NAME + " ("
            + DirContractEntry._ID + " INTEGER PRIMARY KEY,"
            + DirContractEntry.COLUMN_NAME_DIR_NAME + TEXT_TYPE + UNIQUE + COMMA_SEP
            + DirContractEntry.COLUMN_NAME_DIR_DESCRIPTION + TEXT_TYPE
            + " )";        
    
    private static final String SQL_CREATE_SPAM_PATTERN = 
            "CREATE TABLE " + SpamPatternContractEntry.TABLE_NAME + " ("
            + SpamPatternContractEntry._ID + " INTEGER PRIMARY KEY,"
            + SpamPatternContractEntry.COLUMN_NAME_SP_TEXT + TEXT_TYPE + COMMA_SEP
            + SpamPatternContractEntry.COLUMN_NAME_SP_HASH + TEXT_TYPE
            + " )";        
      
    private static final String SQL_DELETE_DIR
            = "DROP TABLE IF EXISTS " + DirContractEntry.TABLE_NAME;
    private static final String SQL_DELETE_SMS_DIR
            = "DROP TABLE IF EXISTS " + SmsDirContractEntry.TABLE_NAME;
    private static final String SQL_DELETE_FILTER_DIR
            = "DROP TABLE IF EXISTS " + FilterDirContractEntry.TABLE_NAME;
    private static final String SQL_DELETE_SPAM_PATTERN 
            = "DROP TABLE IF EXISTS " + SpamPatternContractEntry.TABLE_NAME;
    
    public SmsDirDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DIR);
        db.execSQL(SQL_CREATE_SMS_DIR);
        db.execSQL(SQL_CREATE_FILTER_DIR);
        db.execSQL(SQL_CREATE_SPAM_PATTERN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_SMS_DIR);
        db.execSQL(SQL_DELETE_FILTER_DIR);
        db.execSQL(SQL_DELETE_DIR);       
        db.execSQL(SQL_DELETE_SPAM_PATTERN);
        onCreate(db);
    }
}
