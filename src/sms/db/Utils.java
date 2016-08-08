/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

import android.app.ProgressDialog;
import android.content.Context;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author tomek
 */
public class Utils {
    
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
    
    public static boolean isEmpty(String s) {
        return (s == null || s.equals(""));
    }
    
    public static String toNotNull(String s) {
        return (s == null) ? "" : s;
    }
    
    public static Date toMidnight(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar calOut = Calendar.getInstance();
        calOut.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        calOut.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        calOut.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
        return calOut.getTime();
    }

    public static ProgressDialog createProgressDialog(Context context) {
        return createProgressDialog(context, null);
    }
    
    public static ProgressDialog createProgressDialog(Context context, final String title) {
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setTitle(!isEmpty(title) ? title : "Please wait!");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        return progress;
    }
    
    public interface Transformer<T,U> {
        public U apply(T val);
    }
    
    public static <T,U> List<U> transform(List<T> from, Transformer<T,U> transformer) {
        List<U> toList = new ArrayList<>();
        for (T t: from) {
            toList.add(transformer.apply(t));
        }
        return toList;
    }
    
    public static String repeat(final String s, int times) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < times; ++i) {
            sb.append(s);
            if (i < times - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    public static <T> T find(List<? extends T> list, T val) {
        int index = list.indexOf(val);
        if (index >= 0) {
            return list.get(index);
        }
        return null;
    }
}
