/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tomek
 */
public class FileDialog {
    
    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    
    public interface DirectorySelectedListener {
        void directorySelected(File file);
    }
    
    protected Activity activity;
    protected String[] fileList;
    protected FileSelectedListener fileListener;
    protected DirectorySelectedListener dirListener;
    protected File currentPath;
    protected String suffix;
    
    public FileDialog(Activity activity, File currentPath, String suffix) {
        this.activity = activity;
        if (currentPath == null) {
            currentPath = Environment.getExternalStorageDirectory();
        }
        this.currentPath = currentPath;
        this.suffix = suffix;
        loadFileList(currentPath);
    }
    
    public void fireFileListener(File f) {
        if (fileListener != null) {
            fileListener.fileSelected(f);
        }
    }
    
    public void fireDirectoryListener(File f) {
        if (dirListener != null) {
            dirListener.directorySelected(f);
        }
    }
    
    public FileDialog setFileSelectedListener(FileSelectedListener listener) {
        this.fileListener = listener;
        return this;
    }
    
    public Dialog createFileDialog() {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Choose file");
        builder.setItems(fileList, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(currentPath, fileList[which]);
                if (file.isDirectory()) {
                    loadFileList(file);
                    currentPath = file;
                    dialog.cancel();
                    dialog.dismiss();
                    showDialog();
                } else {
                    fireFileListener(file);
                }
            }
        });
        
        dialog = builder.show();
        return dialog;
    }
    
    public void showDialog() {
        createFileDialog().show();
    }
    
    protected void loadFileList(File path) {
        if (path.exists()) {
            List<String> fileList = new ArrayList<String>();
            if (!path.getAbsolutePath().equals("/")) {
                fileList.add("..");
            }
            
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(suffix) || sel.isDirectory();
                }
            };
            fileList.addAll(Arrays.asList(path.list(filter)));
            this.fileList = fileList.toArray(new String[0]);
        }
    }
}
