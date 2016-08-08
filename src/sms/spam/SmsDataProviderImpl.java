/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.util.Collections;
import java.util.List;
import android.os.Handler;
import android.widget.Toast;
import static android.widget.Toast.makeText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import sms.db.DbOp;
import sms.db.FilterDir;
import sms.db.SmsDirContract;
import sms.db.Utils;

/**
 *
 * @author tomek
 */
public class SmsDataProviderImpl implements SmsDataProvider {
    public static final String BODY_COLUMN = "body";        
    public static final String ID_COLUMN = "_id";
    public static final String ADDRESS_COLUMN = "address";
    public static final String DATE_COLUMN = "date";
    public static final String SUBJECT_COLUMN = "subject";
    public static final String THREAD_ID_COLUMN = "thread_id";
    
    protected ContentResolver resolver;
    protected List<Sms> smses;
    protected Map<Long, Sms> smsesIdIndex;
    protected Map<Long, List<Sms>> smsesThreadIdIndex;
    protected List<Sms> activeSmses;
    protected List<Sms> displaySmses;
    protected int index;
    protected DbOp dbOp;
    
    private Activity debugActivity;
    
    protected Boolean loaded;
    protected final Object lock = new Object();
    
    protected static SmsDataProvider instance;
    protected Boolean hasAnyToLoad;
    
    public static SmsDataProvider getInstance(Activity activity) {
        if (instance == null) {
            instance = new SmsDataProviderImpl(activity);
        }
        return instance;
    }
    
    public static SmsDataProvider getInstance() {
        return getInstance(null);
    }
    
    private SmsDataProviderImpl(Activity activity) {
        this.resolver = activity.getContentResolver();
        this.index = 0;
        this.smses = Collections.synchronizedList(new ArrayList<Sms>());
        this.dbOp = DbOp.getInstance();
        this.smsesIdIndex = new ConcurrentHashMap<>();
        this.smsesThreadIdIndex = new ConcurrentHashMap<>();
        this.displaySmses = Collections.synchronizedList(new ArrayList<Sms>());
        this.activeSmses = Collections.synchronizedList(new ArrayList<Sms>());
        
        this.debugActivity = activity;
        this.loaded = Boolean.FALSE;
        this.hasAnyToLoad = Boolean.TRUE;
    }
    
    @Override
    public List<Sms> getSmses(int batch) {
        waitUntilLoaded();
        
        final int numToGet = Math.min(batch, displaySmses.size()-index);
        List<Sms> sublist = Collections.synchronizedList(new ArrayList<Sms>());
        if (numToGet == 0) {
            return sublist;
        }
        
        sublist.addAll(displaySmses.subList(index, index+numToGet));
        index += sublist.size();
        return sublist;
    }
    
    protected void waitUntilLoaded() {
        synchronized(lock) {
            while (!loaded) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {}
            }
        }
    }

    @Override
    public void rewind() {
        this.index = 0;
    }

    @Override
    public void load() {
        Configuration conf = Configuration.getInstance(debugActivity.getApplicationContext());
        final long loadBatch = conf.getLong(Configuration.LOAD_ALL);
        
        final CountDownLatch latch = new CountDownLatch(2);
        
        new Thread(new Runnable() {
            
            public void run() {
                final Cursor cursor = resolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
                cursor.moveToFirst();
                int i = 0;
                do {
                    final Sms sms = new Sms()
                            .setBody(null)
                            .setId(dbOp.loadLong(cursor, ID_COLUMN)) 
                            .setFrom(dbOp.loadString(cursor, ADDRESS_COLUMN))
                            .setDate(dbOp.loadDate(cursor, DATE_COLUMN))
                            .setSubject(dbOp.loadString(cursor, SUBJECT_COLUMN))
                            .setThreadId(dbOp.loadLong(cursor, THREAD_ID_COLUMN))
                            .setIsInbox(true);
                            
                    smses.add(sms);
                    smsesIdIndex.put(sms.getId(), sms);
                    if (sms.getThreadId() != null) {
                        final Long threadId = sms.getThreadId();
                        if (!smsesThreadIdIndex.containsKey(threadId)) {
                            smsesThreadIdIndex.put(threadId, new ArrayList<Sms>());
                        }
                        smsesThreadIdIndex.get(threadId).add(sms);
                    }
                } while (cursor.moveToNext());
                
                cursor.close();
                
                latch.countDown();              
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                final Cursor cursor = resolver.query(Uri.parse("content://sms/sent"), null, null, null, null);
                cursor.moveToFirst();
                int i = 0;
                do {
                    final Sms sms = new Sms()
                            .setBody(null)
                            .setId(dbOp.loadLong(cursor, ID_COLUMN)) 
                            .setFrom(dbOp.loadString(cursor, ADDRESS_COLUMN))
                            .setDate(dbOp.loadDate(cursor, DATE_COLUMN))
                            .setSubject(dbOp.loadString(cursor, SUBJECT_COLUMN))
                            .setThreadId(dbOp.loadLong(cursor, THREAD_ID_COLUMN))
                            .setIsInbox(false);
                            
                    smses.add(sms);
                    smsesIdIndex.put(sms.getId(), sms);
                    if (sms.getThreadId() != null) {
                        final Long threadId = sms.getThreadId();
                        if (!smsesThreadIdIndex.containsKey(threadId)) {
                            smsesThreadIdIndex.put(threadId, new ArrayList<Sms>());
                        }
                        smsesThreadIdIndex.get(threadId).add(sms);
                    }
                } while (cursor.moveToNext());
                
                cursor.close();
                
                latch.countDown();
            }
        }).start();
        
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(SmsDataProviderImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        final CountDownLatch indexLatch = new CountDownLatch(2);
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                Collections.sort(smses, new Comparator<Sms>() {

                    @Override
                    public int compare(Sms lhs, Sms rhs) {
                        return rhs.getDate().compareTo(lhs.getDate());
                    }
                });
                
                indexLatch.countDown();
            }
        }).start();
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (Map.Entry<Long,List<Sms>> entry : smsesThreadIdIndex.entrySet()) {
                    Collections.sort(entry.getValue(), new Comparator<Sms>() {

                        @Override
                        public int compare(Sms lhs, Sms rhs) {
                            return rhs.getDate().compareTo(lhs.getDate());
                        }                        
                    });
                }
                indexLatch.countDown();
            }
        }).start();
                
        try {
            indexLatch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(SmsDataProviderImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                loadMore(loadBatch);
                
                recalculateAll();
                
                synchronized(lock) {
                    loaded = Boolean.TRUE;
                    lock.notify();
                }
            }
        }).start();
    }    

    @Override
    public Sms getSms(long smsId) {
        return smsesIdIndex.get(smsId);
    }
    
    public int getNumLoaded() {
        return index;
    }
    
    @Override
    public List<Sms> filterSmses(FilterDir filterDir) {
        List<Sms> filteredSmses = new ArrayList<Sms>();
        List<String> words = new ArrayList(Arrays.asList(filterDir.getText().trim().split("\\s+")));
        // simple scan through all smses
        for (Sms sms : smses) {
            boolean ok = true;
            if (!Utils.isEmpty(filterDir.getFrom()) && !sms.getFrom().equalsIgnoreCase(filterDir.getFrom())) {
                ok = false;
            }
            if (ok && filterDir.getDateFrom() != null && !sms.getDate().after(filterDir.getDateFrom())) {
                ok = false;
            }
            if (ok && filterDir.getDateTo() != null && !sms.getDate().before(filterDir.getDateTo())) {
                ok = false;
            }
            if (ok && !Utils.isEmpty(filterDir.getText())) {
                final String subject = sms.getSubject();
                final String body = sms.getBody();
                List<String> subjectWords = new ArrayList(Arrays.asList(Utils.toNotNull(subject).trim().split("\\s+")));
                List<String> bodyWords = new ArrayList(Arrays.asList(Utils.toNotNull(body).trim().split("\\s+")));                
                
                subjectWords.retainAll(words);
                bodyWords.retainAll(words);
                
                if (subjectWords.isEmpty() && bodyWords.isEmpty()) {
                    ok = false;
                }
            }
            
            if (ok) {
                filteredSmses.add(sms);
            }
        }
        
        return filteredSmses;
    }

    @Override
    public Map<Date, List<Sms>> splitSmsesByDay(List<Sms> smses) {
        Map<Date, List<Sms>> mapDays = new TreeMap<Date, List<Sms>>(Collections.reverseOrder());
        for (Sms sms : smses) {
            Date day = Utils.toMidnight(sms.getDate());
            if (!mapDays.containsKey(day)) {
                mapDays.put(day, new ArrayList<Sms>());
            }    
            mapDays.get(day).add(sms);            
        }
        return mapDays;
    }

    @Override
    public List<Sms> getAllSmses() {
        return this.smses;
    }

    @Override
    public List<Sms> getSmsesInSameThread(Sms sms) {
        final Long threadId = sms.getThreadId();
        if (threadId == null) {
            return new ArrayList<>(Arrays.asList(sms));
        }
        if (!smsesThreadIdIndex.containsKey(threadId)) {
            return new ArrayList<>(Arrays.asList(sms));
        }
        
        return smsesThreadIdIndex.get(threadId);
    }

    @Override
    public void loadMore(final long batch) {
//        waitUntilLoaded();
        
        final CountDownLatch latch = new CountDownLatch(1);
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                final List<Sms> notLoadedInbox = new ArrayList<>();
                final List<Sms> notLoadedOutbox = new ArrayList<>();
                int i = 0;
                while ((notLoadedInbox.size() + notLoadedOutbox.size()) < batch && i < smses.size()) {
                    final Sms sms = smses.get(i++);
                    if (sms.getBody() == null) {
                        if (sms.isIsInbox()) {
                            notLoadedInbox.add(sms);
                        } else {
                            notLoadedOutbox.add(sms);
                        }
                    }
                }
                
                SmsDataProviderImpl.this.hasAnyToLoad = (i < smses.size());
                
                if (notLoadedInbox.isEmpty() && notLoadedOutbox.isEmpty()) {
                    return;
                }
                
                List<Long> idsIn = Utils.transform(notLoadedInbox, new Utils.Transformer<Sms, Long>() {

                    @Override
                    public Long apply(Sms sms) {
                        return sms.getId();
                    }
                });
                List<Long> idsOut = Utils.transform(notLoadedOutbox, new Utils.Transformer<Sms, Long>() {

                    @Override
                    public Long apply(Sms sms) {
                        return sms.getId();
                    }
                });
                
                String[] projection = {
                    ID_COLUMN,
                    BODY_COLUMN
                };
                
                if (!notLoadedInbox.isEmpty()) {
                    String whereClause = ID_COLUMN + " IN (" + Utils.repeat("?", notLoadedInbox.size()) + ")";
                    String[] whereArgs = Utils.transform(idsIn, new Utils.Transformer<Long, String>() {

                        @Override
                        public String apply(Long val) {
                            return String.valueOf(val);
                        }
                    }).toArray(new String[0]);
                    String orderBy = null;
                    
                    final Cursor cursor = resolver.query(Uri.parse("content://sms/inbox"), projection, whereClause, whereArgs, orderBy);
                    cursor.moveToFirst();
                    do {
                        Long id = dbOp.loadLong(cursor, ID_COLUMN);
                        String body = dbOp.loadString(cursor, BODY_COLUMN);
                        
                        Utils.find(notLoadedInbox, new Sms().setId(id)).setBody(body);
                    } while (cursor.moveToNext());
                    
                    cursor.close();
                }
                
                if (!notLoadedOutbox.isEmpty()) {
                    String whereClause = ID_COLUMN + " IN (" + Utils.repeat("?", notLoadedOutbox.size()) + ")";
                    String[] whereArgs = Utils.transform(idsOut, new Utils.Transformer<Long, String>() {

                        @Override
                        public String apply(Long val) {
                            return String.valueOf(val);
                        }
                    }).toArray(new String[0]);
                    String orderBy = null;
                    
                    final Cursor cursor = resolver.query(Uri.parse("content://sms/sent"), projection, whereClause, whereArgs, orderBy);
                    cursor.moveToFirst();
                    do {
                        Long id = dbOp.loadLong(cursor, ID_COLUMN);
                        String body = dbOp.loadString(cursor, BODY_COLUMN);
                        
                        Utils.find(notLoadedOutbox, new Sms().setId(id)).setBody(body);
                    } while (cursor.moveToNext());
                    
                    cursor.close();
                }
                
                debugActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(debugActivity, "Num loaded: " + (notLoadedInbox.size() + notLoadedOutbox.size()), Toast.LENGTH_LONG).show();
                    }
                });
                
                latch.countDown();
            }
        }).start();
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Logger.getLogger(SmsDataProviderImpl.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public boolean hasAnyToLoad() {
        waitUntilLoaded();
        return this.hasAnyToLoad;
    }
    
    protected void recalculateAll() {
        recalculateActiveSmses();
        recalculateDisplaySmses();
    }
    
    protected void recalculateActiveSmses() {
        activeSmses.clear();
        for (int i = 0; i < smses.size(); ++i) {
            final Sms sms = smses.get(i);
            if (sms.getBody() != null) {
                activeSmses.add(sms);
            }
        }    
    }
    
    protected void recalculateDisplaySmses() {
        List<Sms> ignores = new ArrayList<>();
        for (int i = 0; i < activeSmses.size(); ++i) {
            final Sms sms = smses.get(i);
            
            if (ignores.contains(sms)) {
                continue;
            }

            displaySmses.add(sms);

            if (sms.getThreadId() == null) {
                continue;
            }

            Date day = Utils.toMidnight(sms.getDate());
            int j = i + 1;
            boolean finished = false;
            do {
                final Sms nextSms = smses.get(j++);

                if (Utils.toMidnight(nextSms.getDate()).before(day)) {
                    finished = true;
                }

                if (nextSms.getThreadId() == null) {
                    continue;
                }

                // add to ignores smses with same threadId and same day 
                if (nextSms.getThreadId().equals(sms.getThreadId())) {
                    ignores.add(nextSms);
                }
            } while (!finished && i < smses.size());
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        Long numToLoad = (data == null || !(data instanceof Long)) ? Long.valueOf(50) : (Long)data;
        
        loadMore(numToLoad.longValue());
        
        recalculateAll();
    }
}
