/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sms.db.DbOp;
import sms.db.Dir;
import sms.db.SmsDir;
import sms.spam.Constants;
import sms.spam.FilterActivity;
import sms.spam.MainActivity;
import sms.spam.R;
import static sms.spam.R.menu.context;
import sms.spam.Sms;
import sms.spam.SmsDataProvider;
import sms.spam.SmsDataRendererImpl;

/**
 *
 * @author tomek
 */
public class FoldersListAdapter extends BaseExpandableListAdapter {
    protected MainActivity mainActivity;
    protected List<Dir> dirs;
    protected SmsDataProvider dataProvider;

    public FoldersListAdapter(MainActivity mainActivity, SmsDataProvider dataProvider, List<Dir> dirs) {
        this.mainActivity = mainActivity;        
        this.dataProvider = dataProvider;
        this.dirs = rebuildDirs(dirs);
    }
    
    @Override
    public int getGroupCount() {
        return dirs.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {              
        return dirs.get(groupPosition).getDirSmses().size() +
                (dataProvider.hasAnyToLoad() ? 1 : 0);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dirs.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dirs.get(groupPosition).getDirSmses().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        final Dir dir = (Dir)getGroup(groupPosition);
        if (view == null) {
            LayoutInflater inf = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.group_heading, null);
        }
        
        TextView heading = (TextView) view.findViewById(R.id.heading);
        final int numDirSmses = (dir.getDirSmses() == null) ? 0 : dir.getDirSmses().size();
        heading.setText(dir.getName().trim() + " [" + numDirSmses + "]");
        
        ImageView confButton = (ImageView)view.findViewById(R.id.confFilter);
        final Context context = view.getContext();
        confButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FilterActivity.class);
                intent.putExtra(Constants.DIR, dir);
                (FoldersListAdapter.this.mainActivity).startActivityForResult(intent, Constants.FILTER_REQUEST);
            }
        });
        
        final DialogInterface.OnClickListener removeListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        DbOp.getInstance().deleteDir(dir);
                        mainActivity.removeFolder(dir);
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        
        ImageView removeButton = (ImageView)view.findViewById(R.id.removeFilter);
        removeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.alert_confirm)
                        .setPositiveButton(R.string.alert_yes, removeListener)
                        .setNegativeButton(R.string.alert_no, removeListener)
                        .show();
            }
        });

//        ImageButton button = (ImageButton)view.findViewById(R.id.confFilter);
//        final Context context = view.getContext();
//        button.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, FilterActivity.class);
//                intent.putExtra(Constants.DIR, dir);
//                ((Activity)FoldersListAdapter.this.context).startActivityForResult(intent, Constants.FILTER_REQUEST);
//            }
//        });
  
        return view;        
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        if (childPosition == 0 && dataProvider.hasAnyToLoad()) {
            // first is 'Load more button'
            return SmsDataRendererImpl.buildLoadMoreButton(mainActivity);
        }
        
        if (dataProvider.hasAnyToLoad()) {
            childPosition--;
        }
        
        SmsDir smsDir = (SmsDir)getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater inf = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.child_layout, null);
        }
        
        Sms sms = dataProvider.getSms(smsDir.getSmsId());
        
        LayoutInflater layoutInflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View smsLayout = layoutInflater.inflate(R.layout.sms_layout, null);
        TextView from = (TextView)smsLayout.findViewById(R.id.from);
        TextView date = (TextView)smsLayout.findViewById(R.id.date);
        TextView text = (TextView)smsLayout.findViewById(R.id.text);
        
        from.setText(sms.getFrom());
        from.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        from.setTextColor(Color.BLACK);
        date.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(sms.getDate()));
        date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        date.setTextColor(Color.BLACK);
        text.setText(sms.getBody());
        text.setTextColor(Color.BLACK);
        
        return smsLayout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    
    public List<Dir> rebuildDirs() {
        return rebuildDirs(dirs);
    }
    
    public List<Dir> rebuildDirs(List<Dir> dirs) {
        List<Dir> transformedDirs = new ArrayList<Dir>();
        for (Dir dir : dirs) {
            Dir newDir = new Dir(dir);
            transformedDirs.add(newDir);
            for (SmsDir smsDir : dir.getDirSmses()) {
                Sms sms = dataProvider.getSms(smsDir.getSmsId());
                if (sms.getBody() != null) {
                    newDir.getDirSmses().add(smsDir);
                }    
            }
        }
        return transformedDirs;
    }
    
    public void rebuildVisibleDirSmses() {
        for (Dir dir : dirs) {
            dir.getDirSmses().clear();
            DbOp.getInstance().getDirSmses(dir);
            for (SmsDir smsDir : new ArrayList<>(dir.getDirSmses())) {
                Sms sms = dataProvider.getSms(smsDir.getSmsId());
                if (sms.getBody() == null) {
                    dir.getDirSmses().remove(smsDir);
                }    
            }
        }
    }
}
