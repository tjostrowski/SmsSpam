/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.app.Activity;
import android.os.Bundle;

/**
 *
 * @author tomek
 */
public class FoldersActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folders);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
