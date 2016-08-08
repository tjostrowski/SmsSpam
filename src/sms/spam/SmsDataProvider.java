/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.database.Cursor;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import sms.db.FilterDir;

/**
 *
 * @author tomek
 */
public interface SmsDataProvider extends Observer {

    public void rewind();
    public void load();
    public List<Sms> getSmses(final int batch);
    
    public List<Sms> getAllSmses();
    
    public Sms getSms(final long smsId);
    
    public int getNumLoaded();
    
    public List<Sms> filterSmses(FilterDir filterDir);
    
    public Map<Date, List<Sms> > splitSmsesByDay(List<Sms> smses);
    
    public List<Sms> getSmsesInSameThread(Sms sms);
    
    public void loadMore(final long batch);
    
    public boolean hasAnyToLoad();
}
