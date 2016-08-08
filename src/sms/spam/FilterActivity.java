/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import sms.db.DbOp;
import sms.db.Dir;
import sms.db.FilterDir;
import sms.db.Utils;
import static sms.spam.R.id.filterDateFrom;

/**
 *
 * @author tomek
 */
public class FilterActivity extends Activity {
    
    protected final static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.filter_popup);
        
        final Dir initialDir = (Dir)getIntent().getSerializableExtra(Constants.DIR);
        final FilterDir initialFilterDir = (initialDir != null) ? initialDir.getFilterDir() : null;

        final EditText textName = (EditText)findViewById(R.id.filterName);
        final EditText textFrom = (EditText)findViewById(R.id.filterFrom);
        final EditText textDateFrom = (EditText)findViewById(R.id.filterDateFrom);
        final EditText textDateTo = (EditText)findViewById(R.id.filterDateTo);
        final EditText textText = (EditText)findViewById(R.id.filterText);
        
        if (initialDir != null) {
            if (initialDir.getName() != null) {
                textName.setText(initialDir.getName());
            }
        }
        
        if (initialFilterDir != null) {
            if (initialFilterDir.getFrom() != null) {
                textFrom.setText(initialFilterDir.getFrom());
            }    
            if (initialFilterDir.getDateFrom() != null) {
                textDateFrom.setText(dateFormat.format(initialFilterDir.getDateFrom()));
            }
            if (initialFilterDir.getDateTo() != null) {
                textDateTo.setText(dateFormat.format(initialFilterDir.getDateTo()));
            }
            if (initialFilterDir.getText() != null) {
                textText.setText(initialFilterDir.getText());
            }
        }        
        
        Button btnOK = (Button) findViewById(R.id.ok);
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {                
                Date dateFrom = null;
                try {
                    dateFrom = dateFormat.parse(textDateFrom.getText().toString());
                } catch (ParseException e) {}
                Date dateTo = null;
                try {
                    dateTo = dateFormat.parse(textDateTo.getText().toString());
                } catch (ParseException e) {}
                
                final String filterName = textName.getText().toString();
                if (Utils.isEmpty(filterName)) {
                    return;
                }
                
                DbOp dbOp = DbOp.getInstance();
                
                Dir editedDir;
                if (initialDir == null) {
                    editedDir = dbOp.putDir(filterName, filterName);
                } else if (initialDir.isTransient()) {
                    editedDir = dbOp.putDir(filterName, initialDir.getDescription());
                } else {
                    editedDir = initialDir;
                    if (!initialDir.getName().equals(filterName)) {
                        editedDir.setName(filterName);
                        editedDir = dbOp.updateDir(editedDir);
                    }
                }
                
                FilterDir editedFilterDir = new FilterDir(editedDir)
                        .setFrom(textFrom.getText().toString())
                        .setDateFrom(dateFrom)
                        .setDateTo(dateTo)
                        .setText(textText.getText().toString());                              
                
                Intent intent = new Intent();
                if (editedFilterDir.containsAny() && !editedFilterDir.equals(initialFilterDir)) {
                    dbOp.deleteDirFilters(editedDir);
                    dbOp.deleteDirSmses(editedDir, false);
                    dbOp.putFilterDir(editedFilterDir);
                    
                    intent.putExtra(Constants.FILTER_DIR, editedFilterDir);
                }
                setResult(RESULT_OK, intent);
                
                finish();
            }
        });

        Button btnDismiss = (Button) findViewById(R.id.dismiss);
        btnDismiss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public void onDestroy() {        
        super.onDestroy();
    }
}
