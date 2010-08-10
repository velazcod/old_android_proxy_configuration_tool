package net.geekherd.metropcs.proxyswitcher;

import android.app.Activity;
import android.os.Bundle;

public class Configuration extends Activity 
{
	public final static String TAG = "MetroPCS-ProxySwitcher";
	public final static String DEFAULT_PROXY = "10.223.2.4"; //MetroPCS Proxy Server
	public final static String DEFAULT_PROXY_PORT = "3128"; // MetroPCS Proxy Server Port
	
	
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