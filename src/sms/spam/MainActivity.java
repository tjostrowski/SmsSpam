package sms.spam;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import sms.db.DbOp;
import sms.db.Dir;
import sms.db.FilterDir;
import sms.db.SmsDirDbHelper;
import sms.db.Utils;
import sms.views.BorderedTextView;
import sms.views.CustomScrollView;
import sms.views.FileDialog;
import sms.views.FoldersListAdapter;

public class MainActivity extends ActionBarActivity implements Observer
{
    protected SmsDataProvider smsDataProvider;
    protected SmsDataRenderer smsDataRenderer;
    protected DbOp dbOp;
    protected DataLoadObservable dataLoadObservable;
    
    public static final String BODY_COLUMN = "body";        
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    
    protected List<Dir> dirsForAdapter;
    protected FoldersListAdapter listAdapter;
    
    public void renderTabs() {
        final ActionBar actionBar = getSupportActionBar();
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
                if (tab.getPosition() == 0) {
                    if (smsDataRenderer != null && smsDataRenderer.shouldRestore()) {
                        setContentView(R.layout.main);
                        smsDataRenderer.restoreRenderingState();
                    }
                    
                    FileDialog fileDialog = new FileDialog(MainActivity.this, null, ".txt")
                            .setFileSelectedListener(new FileDialog.FileSelectedListener() {

                        @Override
                        public void fileSelected(File file) {
                            new SmsCategorizerTask(MainActivity.this, dbOp, smsDataProvider, dbOp.getDir("spam", true))
                                    .run(file);
                        }
                    });
//                    fileDialog.createFileDialog().show();
                }
                else if (tab.getPosition() == 1) {
//                    Intent intent = new Intent(MainActivity.this, FoldersActivity.class);
//                    startActivity(intent);
                    setContentView(R.layout.folders);
                    final ExpandableListView listView = (ExpandableListView) findViewById(R.id.folderList);
                    dirsForAdapter = dbOp.getDirs(true);
                    listAdapter = new FoldersListAdapter(MainActivity.this, smsDataProvider, 
                            dirsForAdapter);
                    listAdapter.registerDataSetObserver(new DataSetObserver() {
                        public void onChanged() {
//                            listAdapter.rebuildVisibleDirSmses();
                        }
                    });
                    listView.setAdapter(listAdapter);
                    
                    final ImageView addFolderButton = (ImageView)MainActivity.this.findViewById(R.id.addFolderButton);
                    final EditText folderToAdd = (EditText)MainActivity.this.findViewById(R.id.addFolderEdit);
                    
                    addFolderButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            final String folderNameToAdd = folderToAdd.getText().toString();
                            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
                            if (!Utils.isEmpty(folderNameToAdd)) {
                                intent.putExtra(Constants.DIR, new Dir(folderNameToAdd, folderNameToAdd));
                            }    
                            startActivityForResult(intent, Constants.FILTER_REQUEST);
                        }
                    });                    
                }
            }

            public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
                if (tab.getPosition() == 0) {
                    smsDataRenderer.saveRenderingState();
                }
            }

            public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
            }
        };
        
        int[] nameIds = new int[] {R.string.main_tab1, R.string.main_tab2, R.string.main_tab3};
        
        Resources res = getResources();
        for (int i = 0; i < nameIds.length; ++i) {
            actionBar.addTab(actionBar.newTab()
                    .setText(res.getString(nameIds[i]))
                    .setTabListener(tabListener)
            );
        }
    }
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        final ProgressDialog progress = Utils.createProgressDialog(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // db ops
        dbOp = DbOp.getInstance(getApplicationContext());  
        try {
            dbOp.putDir("default", "default");
            dbOp.putDir("spam", "spam");
        } catch (Exception e) {
            e.printStackTrace();
        }    
        
        renderTabs();
        super.registerForContextMenu(findViewById(R.id.table_main));
        
        CustomScrollView scrollView = (CustomScrollView)findViewById(R.id.scrollView1);
        smsDataProvider = SmsDataProviderImpl.getInstance(this);
        smsDataRenderer = SmsDataRendererImpl.getInstance(this);
        scrollView.setSmsDataRenderer(smsDataRenderer);

        smsDataProvider.load();
        
        smsDataRenderer.render(SmsDataRenderer.BATCH_SIZE, new SimpleProgressCallback(progress));
        
        dataLoadObservable = new DataLoadObservable();
        dataLoadObservable.addObserver(smsDataProvider);
        dataLoadObservable.addObserver(smsDataRenderer);
        dataLoadObservable.addObserver(this);
    }
    
    protected Map<Integer, String> menuIdMap = new LinkedHashMap<Integer, String>();
    protected static int MENU_ID_BASE = 0x6f0b0000;
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        Toast.makeText(getApplicationContext(), v.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        
        menuIdMap.clear();
        SubMenu submenuAddToFolder = menu.addSubMenu("Add to folder");
        List<Dir> dirs = dbOp.getDirs();
        for (int i = 0; i < dirs.size(); ++i) {
            Dir dir = dirs.get(i);
            submenuAddToFolder.add(0, MENU_ID_BASE+i, i, dir.getName());
            menuIdMap.put(MENU_ID_BASE+i, dir.getName());            
        }
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        
        if (item.getItemId() == R.id.context_menu_new_sms) {
            Intent intent = new Intent(this, NewSmsActivity.class);
            startActivity(intent);
            return true;
        }
        
        final String dirName = menuIdMap.get(item.getItemId());
        if (dirName != null) {  // it is sms dir          
            final Sms sms = (Sms)smsDataRenderer.getCurrentlyClicked().getTag();
            dbOp.putSmsDir(dirName, sms.getId(), true);
            return true;
        }
        
        return false;
    }    
    
    @Override
    public void onDestroy() {        
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.FILTER_REQUEST:
                if (resultCode == RESULT_OK) {
                    FilterDir filterDir = (FilterDir)data.getSerializableExtra(Constants.FILTER_DIR);
                    if (filterDir != null) {
                        recalculateFolderDir(filterDir);
                    }
                }
        }
    }    
    
    protected void recalculateFolderDir(final FilterDir filterDir) {
        final ProgressDialog progress = Utils.createProgressDialog(this);
        
        new Thread(new Runnable() {
            public void run() {
                List<Sms> filteredSmses = smsDataProvider.filterSmses(filterDir);
                for (Sms sms : filteredSmses) {
                    dbOp.putSmsDir(filterDir.getDir().getId(), sms.getId(), false);
                }
                int idx = dirsForAdapter.indexOf(filterDir.getDir());
                if (idx >= 0) {
                    Dir dir = dirsForAdapter.get(idx);
                    dir.setDirSmses(dbOp.getDirSmses(dir));
                    dir.setFilterDir(filterDir);
                } else {
                    Dir dir = filterDir.getDir();
                    dir.setDirSmses(dbOp.getDirSmses(dir));
                    dirsForAdapter.add(dir);
                }
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        listAdapter.notifyDataSetChanged();
                        progress.dismiss();
                    }
                });
            }
        }).start();
    }
    
    public void reloadFolder(Dir dir) {
        Dir reloadedDir = dbOp.getDir(dir.getName(), true);
        dirsForAdapter.set(dirsForAdapter.indexOf(reloadedDir), reloadedDir);
        listAdapter.notifyDataSetChanged();
    }
    
    public void removeFolder(Dir dir) {
        dirsForAdapter.remove(dir);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (dirsForAdapter == null) {
            return;
        }
        listAdapter.rebuildVisibleDirSmses();
        listAdapter.notifyDataSetChanged();
    }
    
    public void notifyDataLoadObservers() {      
        dataLoadObservable.setChanged();
        dataLoadObservable.notifyObservers(null);
    }
}
