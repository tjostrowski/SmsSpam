/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import sms.spam.SmsDataProvider;
import sms.spam.SmsDataRenderer;

/**
 *
 * @author tomek
 */
public class CustomScrollView extends ScrollView {
    
    protected SmsDataRenderer dataRenderer;

    public CustomScrollView(Context context) {
        super(context);
    }
    
    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setSmsDataRenderer(SmsDataRenderer dataRenderer) {
        this.dataRenderer = dataRenderer;
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = (View) getChildAt(getChildCount() - 1);
        int diff = view.getBottom() - (getHeight() + getScrollY() + view.getTop());
        if (diff <= 0 && dataRenderer != null) {
            if (!dataRenderer.isRendering()) {
                dataRenderer.render(SmsDataRenderer.BATCH_SIZE, null);
            }            
        }
        
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
