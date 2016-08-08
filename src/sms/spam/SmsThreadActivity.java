/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sms.db.DbOp;
import sms.views.Border;
import sms.views.BorderedTextView;
import sms.views.CustomScrollView;

/**
 *
 * @author tomek
 */
public class SmsThreadActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thread_layout);
        
        final Sms sms = (Sms)getIntent().getSerializableExtra(Constants.SMS);
        final SmsDataProvider smsDataProvider = SmsDataProviderImpl.getInstance();
        
        final CustomScrollView scroll = (CustomScrollView)findViewById(R.id.scrollView1);
        TableLayout tview = (TableLayout) scroll.getChildAt(0);
        Map<String, Integer> senderColors = new HashMap<>();
        List<Sms> smsesInThread = smsDataProvider.getSmsesInSameThread(sms);
        for (Sms assocSms : smsesInThread) {
            TableRow tbrow1 = new TableRow(scroll.getContext());
            final BorderedTextView tv0 = new BorderedTextView(scroll.getContext());
            
            final String from = assocSms.getFrom();
            int color;
            if (senderColors.containsKey(from)) {
                color = senderColors.get(from);
            } else {
                Collection<Integer> colors =  senderColors.values();
                if (colors.contains(Color.RED)) {
                    color = Color.BLUE;
                } else {
                    color = Color.RED;
                }
                senderColors.put(from, color);
            }            
            tv0.setBackgroundColor(color);
            
            tv0.setText(new SimpleDateFormat("dd-MM-yyyy").format(assocSms.getDate()) + ": " + assocSms.getBody());
            tv0.setTextColor(Color.WHITE);
            tv0.setGravity(Gravity.LEFT);
            tv0.setBorders(new Border[]{
                new Border(BorderedTextView.BORDER_TOP, 0, 2, Color.YELLOW),
                new Border(BorderedTextView.BORDER_BOTTOM, 0, 2, Color.YELLOW),

            });
            tbrow1.setPadding(0, 5, 0, 5);
            tbrow1.addView(tv0);

            tview.addView(tbrow1);
        }
    }
    
    @Override
    public void onDestroy() {        
        super.onDestroy();
    }
}
