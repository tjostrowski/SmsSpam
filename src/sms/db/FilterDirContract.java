/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

import android.provider.BaseColumns;

/**
 *
 * @author tomek
 */
public class FilterDirContract {
    public FilterDirContract() {}
    
    public static abstract class FilterDirContractEntry implements BaseColumns {
        public static final String TABLE_NAME = "filter_dir";
                
        public static final String COLUMN_NAME_DIR_ID = "dir_id";
        public static final String COLUMN_NAME_FROM = "text_from";
        public static final String COLUMN_NAME_DATE_FROM = "date_from";
        public static final String COLUMN_NAME_DATE_TO = "date_to";
        public static final String COLUMN_NAME_TEXT = "text";
    }
}
