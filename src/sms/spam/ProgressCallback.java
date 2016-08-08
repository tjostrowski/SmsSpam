/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.app.ProgressDialog;

/**
 *
 * @author tomek
 */
public interface ProgressCallback extends Callback {
    
    public void apply();
    
    public ProgressDialog getProgress();
}
