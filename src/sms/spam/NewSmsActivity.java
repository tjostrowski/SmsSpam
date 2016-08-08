/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.support.v4.app.FragmentActivity;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.AutoCompleteTextView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FilterQueryProvider;
import android.widget.Toast;
import sms.db.ContactsLoader;

/**
 *
 * @author tomek
 */
public class NewSmsActivity extends FragmentActivity {
    SimpleCursorAdapter contactsCursorAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_sms_layout);
        
        final AutoCompleteTextView autocompleteContact = (AutoCompleteTextView) findViewById(R.id.autocompleteContact);
        contactsCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.autocomplete,
                null,
                new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                new int[] {R.id.autocompleteText, R.id.autocompleteNumber},
                0);
        
        final ContactsLoader loader = new ContactsLoader(this);
        getSupportLoaderManager().initLoader(ContactsLoader.CONTACTS, null, loader);
        getSupportLoaderManager().initLoader(ContactsLoader.NUMBERS, null, loader);
        
        contactsCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(final CharSequence constraint) {
                if (getSupportLoaderManager().hasRunningLoaders()) {
                    return null;
                }
                
                return new CursorWrapper(loader.getCursor(ContactsLoader.NUMBERS)) {
                    int pos, count;
                    int[] indexes;
                    
                    {
                        this.pos = 0;
                        if (!constraint.equals("")) {
                            this.indexes = new int[super.getCount()];
                            this.count = 0;
                            for (int i = 0, j = 0; i < super.getCount(); ++i) {
                                super.moveToPosition(i);
                                if (super.getString(0).contains(constraint) || super.getString(1).contains(constraint)) {
                                    this.indexes[j++] = i;
                                    this.count++;
                                }
                            }
                        } else {
                            this.count = super.getCount();
                            this.indexes = new int[this.count];
                            for (int i = 0; i < count; ++i) {
                                this.indexes[i] = i;
                            }
                        }
                    }
                    
                    @Override
                    public boolean move(int offset) {
                        return this.moveToPosition(this.pos+offset);
                    }
                    
                    @Override
                    public boolean moveToNext() {
                        return this.moveToPosition(this.pos+1);
                    }
                    
                    @Override
                    public boolean moveToPrevious() {
                        return this.moveToPosition(this.pos-1);
                    }
                    
                    @Override
                    public boolean moveToFirst() {
                        return this.moveToPosition(0);
                    }
                    
                    @Override
                    public boolean moveToLast() {
                        return this.moveToPosition(this.count-1);
                    }
                    
                    @Override
                    public boolean moveToPosition(int position) {
                        if (position < 0 || position >= count) {
                            return false;
                        }
                        this.pos = indexes[position];
                        return super.moveToPosition(this.pos);
                    }
                    
                    @Override
                    public int getCount() {
                        return this.count;
                    }
                    
                    @Override
                    public int getPosition() {
                        return this.pos;
                    }
                    
                    @Override
                    public void close() {
                    }
                };
            }
        });
       
        
        autocompleteContact.setAdapter(contactsCursorAdapter);
        autocompleteContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int index, long id) {
                Cursor cursor = (Cursor)adapter.getItemAtPosition(index);
                autocompleteContact.setText( cursor.getString(1) );
            }
        });
//        autocompleteContact.setAdapter(new ArrayAdapter<String>
//        (this, R.layout.autocomplete, new String[] {"alfa", "beta", "gamma"}));
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
