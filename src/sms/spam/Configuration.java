/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.app.Activity;
import android.content.Context;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tomek
 */
public class Configuration {
    protected static final String CONF_PROPERTIES = "sms/spam/configuration.properties";
    
    public static final String LOAD_INBOX = "load_inbox";
    public static final String LOAD_OUTBOX = "load_outbox";
    public static final String LOAD_ALL = "load_all";
    
    protected static Configuration conf;
    protected Properties props;
    protected Context appContext;
    
    public static Configuration getInstance(Context ctx) {
        if (conf == null) {
            conf = new Configuration(ctx);
        }
        return conf;
    }
    
    private Configuration(Context ctx) {
        this.appContext = ctx;
        
        InputStream rawResource = ctx.getResources().openRawResource(R.raw.configuration);
        props = new Properties();
        try {
            props.load(rawResource);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String get(final String propertyName) {
        return props.getProperty(propertyName);
    }
    
    public long getLong(final String propertyName) {
        return Long.valueOf(props.getProperty(propertyName)).longValue();
    }
}
