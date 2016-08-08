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
public class DirContract {
    
    public DirContract() {}
    
    public static abstract class DirContractEntry implements BaseColumns {
        public static final String TABLE_NAME = "dir";
                
        public static final String COLUMN_NAME_DIR_NAME = "dirname";
        public static final String COLUMN_NAME_DIR_DESCRIPTION = "description";
    }
}
