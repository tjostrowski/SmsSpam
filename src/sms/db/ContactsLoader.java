/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.db;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.widget.CursorAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tomek
 */
public class ContactsLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    protected Context context;
    protected Map<Integer, Pair<Loader<Cursor>, Cursor> > loadersMap;
    
    public static final int CONTACTS = 0;
    public static final int NUMBERS = 1;
    
    public class Pair<F, S> {
        private F first;
        private S second;
        
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    };
    
    public ContactsLoader(Context context) {
        this.context = context;
        this.loadersMap = new HashMap<>();
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader;
        if (id == CONTACTS) {
            loader = createLoader(ContactsContract.Contacts.CONTENT_URI, 
                    new String[]{
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts._ID
                    },
                    null, null, null);
        } else if (id == NUMBERS) {
            loader = createLoader(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                    new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone._ID
                    },
                    null, null, null);
        } else {
            throw new UnsupportedOperationException("Unsupported loader: " + id);
        }
        
        loadersMap.put(id, new Pair<Loader<Cursor>, Cursor>(loader, null));
        return loader;
    }
    
    public Loader<Cursor> createLoader(Uri uri, String[] projection, String sel, String[] selArgs, String sortOrder) {
        return new CursorLoader(
                context,
                uri,
                projection,
                sel,
                selArgs,
                sortOrder 
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        for (Map.Entry<Integer, Pair<Loader<Cursor>, Cursor> > entry : loadersMap.entrySet()) {
            if ( entry.getValue().first.equals(loader) ) {
                loadersMap.put(entry.getKey(), new Pair<>(loader, data));
                return;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // cursor remains null
    }

    public Cursor getCursor(int id) {
        return loadersMap.get(id).second;
    }
}
