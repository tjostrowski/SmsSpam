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
public class SimpleProgressCallback implements ProgressCallback {
    protected ProgressDialog progressDialog;
    
    public SimpleProgressCallback(ProgressDialog dialog) {
        progressDialog = dialog;
    }
    
    @Override
    public void apply() {
        progressDialog.dismiss();
    }

    @Override
    public ProgressDialog getProgress() {
        return progressDialog;
    }
}
