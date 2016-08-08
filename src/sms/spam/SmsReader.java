/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import static sms.spam.MainActivity.BODY_COLUMN;

/**
 *
 * @author tomek
 */
public class SmsReader {
    
    public String[] readMessages(final ContentResolver resolver, final MainActivity activity) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                final List<String> messages = new ArrayList<String>();
                final int pagination = 20;
                Cursor cursor = resolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
                int colIdx = 0;
                final TableLayout tview = (TableLayout) activity.findViewById(R.id.table_main);
                cursor.moveToFirst();
                do {
                    if (cursor.getColumnCount() > 0) {
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < cursor.getColumnCount(); ++i) {
                            if (cursor.getColumnName(i).equalsIgnoreCase(BODY_COLUMN)) {
                                sb.append(cursor.getString(i));
                                break;
                            }
                        }
                        messages.add(sb.toString());
                    } else {
                        messages.add("");
                    }

                    if (colIdx++ % pagination == 0 && colIdx <= 30) {
                        final int idx = colIdx;
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                for (int i = Math.max(idx - pagination, 0); i < idx; ++i) {

                                    TableRow tbrow1 = new TableRow(activity);
                                    TextView tv0 = new TextView(activity);
                                    tv0.setBackgroundColor(Color.RED);
                                    tv0.setText(messages.get(i));
                                    tv0.setTextColor(Color.WHITE);
                                    tv0.setGravity(Gravity.LEFT);

                                    tbrow1.setPadding(0, 5, 0, 5);
                                    tbrow1.addView(tv0);

                                    tview.addView(tbrow1);
                                }
                            }
                        });
                    }

                } while (cursor.moveToNext());

            }
        }, 100);
        
        return new String[0];
    }
}
