/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.view.View;
import java.util.Observer;

/**
 *
 * @author tomek
 */
public interface SmsDataRenderer extends Observer {
    public static final int BATCH_SIZE = 50;
    
    public boolean isRendering();
    public void render(int batch, Callback callback);
    
    public View getCurrentlyClicked();
    public void setCurrentlyClicked(View currentlyClicked);
    
    public void saveRenderingState();    
    public boolean restoreRenderingState();    
    public boolean shouldRestore();
}
