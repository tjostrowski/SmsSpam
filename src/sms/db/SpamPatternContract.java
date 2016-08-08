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
public class SpamPatternContract {
    public SpamPatternContract() {}
    
    public static abstract class SpamPatternContractEntry implements BaseColumns {
        public static final String TABLE_NAME = "spam_pattern";
                        
        public static final String COLUMN_NAME_SP_TEXT = "text";
        public static final String COLUMN_NAME_SP_HASH = "hash";        
    }
}
