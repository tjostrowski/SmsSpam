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
public class SmsDirContract {
    
    public SmsDirContract() {}
    
    public static abstract class SmsDirContractEntry implements BaseColumns {
        public static final String TABLE_NAME = "sms_dir";
                
        public static final String COLUMN_NAME_SMS_ID = "sms_id";
        public static final String COLUMN_NAME_DIR_ID = "dir_id";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_FROM_TAP = "from_tap";
    }
    
    public static abstract class SmsDirDayViewContractEntry implements BaseColumns {
        public static final String VIEW_NAME = "sms_dir_day_view";
        
        public static final String COLUMN_NAME_SMS_ID = "sms_id";
        public static final String COLUMN_NAME_DIR_ID = "dir_id";
        public static final String COLUMN_NAME_DAY = "day";
    }
}
