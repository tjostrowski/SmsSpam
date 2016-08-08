/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.app.Activity;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sf.classifier4J.ClassifierException;
import net.sf.classifier4J.SimpleClassifier;
import net.sf.classifier4J.vector.HashMapTermVectorStorage;
import net.sf.classifier4J.vector.TermVectorStorage;
import net.sf.classifier4J.vector.VectorClassifier;
import sms.db.DbOp;
import sms.db.Dir;
import sms.db.SmsDir;
import sms.db.SpamPattern;

/**
 *
 * @author tomek
 */
public class SmsCategorizerTask {
    
    protected Activity activity;
    protected DbOp dbOp;    
    protected Dir spamDir;
    protected SmsDataProvider dataProvider;
    protected VectorClassifier vc;
    
    private List<String> categories;

    public SmsCategorizerTask(Activity activity, DbOp dbOp, SmsDataProvider dataProvider, Dir spamDir) {
        this.activity = activity;
        this.dbOp = dbOp;
        this.spamDir = spamDir;
        this.dataProvider = dataProvider;        
    }
    
    public void run(final File file) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                dbOp.fillSpamPatterns(file);
                initClassifier();
                
                List<SmsDir> currentSpamSmses = spamDir.getDirSmses();
                List<Sms> allSmses = dataProvider.getAllSmses();
                
                for (Sms sms : allSmses) {
                    if (currentSpamSmses.contains(new SmsDir(sms.getId(), null))) {
                        continue;
                    }
                    
                    if (isSpam(sms)) {
                        dbOp.putSmsDir(spamDir.getId(), sms.getId(), false);
                    }
                }
            }
        }).start();
    }
    
    public void initClassifier() {
        TermVectorStorage storage = new HashMapTermVectorStorage();
        this.vc = new VectorClassifier(storage);
        this.categories = new ArrayList<>();
        
        List<SpamPattern> spamPatterns = dbOp.getSpamPatterns();
        for (int i = 0; i < spamPatterns.size(); ++i) {
            SpamPattern pattern = spamPatterns.get(i);
            
            try {
                final String categoryName = "category" + i;
                vc.teachMatch(categoryName, pattern.getText());
                this.categories.add(categoryName);
            } catch (ClassifierException e)     {
                e.printStackTrace();
            }
        }
    }
    
    public static final double THRESHOLD = 0.5;
    
    public boolean isSpam(Sms sms) {
        for (String category : this.categories) {
            try {
                final double result = vc.classify(category, sms.getBody());
                
//                activity.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(activity, "Result: " + result, Toast.LENGTH_LONG).show();
//                    }
//                });
                
                if (result >= THRESHOLD) {
                    return true;
                }    
                
            } catch (ClassifierException e)     {
                e.printStackTrace();
            }    
        }                
        
        return false;
    }
}
