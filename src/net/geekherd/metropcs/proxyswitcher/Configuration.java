package net.geekherd.metropcs.proxyswitcher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Configuration extends Activity 
{
	public final static String TAG = "MetroPCS-ProxySwitcher";
	public final static String DEFAULT_PROXY = "10.223.2.4"; //MetroPCS Proxy Server
	public final static String DEFAULT_PROXY_PORT = "3128"; // MetroPCS Proxy Server Port
	
	
	/** Preferences Constants **/
	public final static String PREF_AUTO_SWITCH_ENABLED = "prefs_autoswitch_enabled";
	public final static Boolean PREF_AUTO_SWITCH_ENABLED_DEFAULT = true;
	
	public final static String PREF_USE_U2NL = "prefs_use_u2nl";
	public final static Boolean PREF_USE_U2NL_DEFAULT = true;
	
	public final static String PREF_USE_CUSTOM_PROXY = "prefs_use_custom_proxy";
	public final static Boolean PREF_USE_CUSTOM_PROXY_DEFAULT = false;
	
	public final static String PREF_PROXY = "prefs_custom_proxy";
	public final static String PREF_PROXY_DEFAULT = DEFAULT_PROXY;
	
	public final static String PREF_PROXY_PORT = "prefs_custom_proxy_port";
	public final static String PREF_PROXY_PORT_DEFAULT = DEFAULT_PROXY_PORT;
	/** Preferences Constants **/
	
	
	private SharedPreferences preferences;
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        /*
         * TODO:
         * 
         * Make a good UI that lets the user choose the proxy 
         * server and port to use on the broadcast receiver
         * 
         *  
         */
    }
}