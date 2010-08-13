package net.geekherd.metropcs.proxyswitcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Proxy;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class Configuration extends PreferenceActivity 
{
	public final static String TAG = "MetroPCS-ProxySwitcher";
	public final static String DEFAULT_PROXY = "10.223.2.4"; //MetroPCS Proxy Server
	public final static String DEFAULT_PROXY_PORT = "3128"; // MetroPCS Proxy Server Port
	
	public static String ACTION_ACTIVATE_PROXY = "ActivateProxy";
	public static String ACTION_DEACTIVATE_PROXY = "DectivateProxy";
	
	/** Preferences Constants **/
	public final static String PROXY_STATUS = "proxy_status";
	
	public final static String TOGGLE_ACTIVATE = "toggle_activate";
	public final static String TOGGLE_DEACTIVATE = "toggle_deactivate";
	
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
	
	
	private PreferenceScreen proxy_status;
	private PreferenceScreen toggle_activate;
	private PreferenceScreen toggle_deactivate;
    
	private CheckBoxPreference prefs_use_custom_proxy;
	private EditTextPreference prefs_custom_proxy;
	private EditTextPreference prefs_custom_proxy_port;
	
	private BroadcastReceiver mProxyChangeActionReceiver = new BroadcastReceiver()
	{
    	@Override
		public void onReceive(Context context, Intent intent) 
		{
    		new checkStatus().execute();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.configuration);
        
        proxy_status = (PreferenceScreen)findPreference("proxy_status");
        toggle_activate = (PreferenceScreen)findPreference("toggle_activate");
        toggle_deactivate = (PreferenceScreen)findPreference("toggle_deactivate");
        
        prefs_use_custom_proxy = (CheckBoxPreference)findPreference("prefs_use_custom_proxy");
        prefs_custom_proxy = (EditTextPreference)findPreference("prefs_custom_proxy");
        prefs_custom_proxy_port = (EditTextPreference)findPreference("prefs_custom_proxy_port");
        
        prefs_use_custom_proxy.setOnPreferenceChangeListener(customProxyCheckboxListener);
    }
    
    @Override
	protected void onResume()
	{
		super.onResume();
		
		updateCustomProxySummary(prefs_use_custom_proxy.isChecked());
		
		new checkStatus().execute();
		
		this.registerReceiver(this.mProxyChangeActionReceiver, 
				new IntentFilter(Proxy.PROXY_CHANGE_ACTION));
	}
    
    @Override
	protected void onStop()
	{
		super.onStop();
		
		this.unregisterReceiver(this.mProxyChangeActionReceiver);
		
		Boolean customProxyValid = true;
		Boolean customProxyPortValid = true;
		
		if (prefs_use_custom_proxy.isChecked())
		{
			if (!validateIP(prefs_custom_proxy.getText()))
			{
				Log.e(TAG, "Invalid ip address");
				customProxyValid = false;
			}
			
			try
			{
				int port = Integer.parseInt(prefs_custom_proxy_port.getText());
				
				if (port < 0 || port > 65535)
				{
					Log.e(TAG, "Invalid port number");
					customProxyPortValid = false;
				}
				
			} catch (NumberFormatException npe)
			{
				npe.printStackTrace();
				Log.e(TAG, "Invalid port number");
				customProxyPortValid = false;
			}
		}
		
		if (!customProxyValid || !customProxyPortValid)
		{
			prefs_use_custom_proxy.setChecked(false);
			prefs_custom_proxy.setText("");
			prefs_custom_proxy_port.setText("");
			
			Log.d(TAG, "Invalid ip address and/or port number. Cannot use custom proxy.");
			Toast.makeText(this, getString(R.string.prefs_use_custom_proxy_error), Toast.LENGTH_LONG).show();
		}
	}
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) 
    {
    	String key = preference.getKey();
    	
    	if (TOGGLE_ACTIVATE.equals(key))
    	{	
    		Intent sIntent = new Intent(this, Toggler.class);
    		sIntent.setAction(ACTION_ACTIVATE_PROXY);
    		sendBroadcast(sIntent);
    	} 
    	else if (TOGGLE_DEACTIVATE.equals(key))
    	{
    		Intent sIntent = new Intent(this, Toggler.class);
    		sIntent.setAction(ACTION_DEACTIVATE_PROXY);
    		sendBroadcast(sIntent);
    	}
    	
    	return true;
    }
    
    private void disableToggles()
    {
    	toggle_activate.setEnabled(false);
    	toggle_deactivate.setEnabled(false);
    }
    
    private void enableToggles()
    {
    	toggle_activate.setEnabled(true);
    	toggle_deactivate.setEnabled(true);
    }
    
    private class checkStatus extends AsyncTask<Void, Void, Boolean> 
	{
    	String proxyStatus = getString(R.string.status_inactive);
    	String u2nlStatus = getString(R.string.status_inactive);
    	
    	protected void onPreExecute()
    	{
    		Log.d(TAG, "checking proxy/u2nl status");
    		proxy_status.setSummary(getString(R.string.status_checking));
    		
    		disableToggles();
    	}
    	
    	@Override
		protected Boolean doInBackground(final Void... params) 
		{
    		try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		if (isProxyActive())
    			proxyStatus = getString(R.string.status_active);
    		
    		if (isU2NLActive())
    			u2nlStatus = getString(R.string.status_active);
    		
			return true;
		}
    	
    	protected void onPostExecute(Boolean state)
    	{
    		proxy_status.setSummary(String.format(
    				getString(R.string.proxy_status_sry), 
    				proxyStatus, 
    				u2nlStatus));
    		
    		enableToggles();
    	}
	}
    
    private Preference.OnPreferenceChangeListener 
    	customProxyCheckboxListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomProxySummary(newValue);
			
			return true;
		}
	};
    
    private void updateCustomProxySummary(Object value)
    {
    	if (value.equals(true))
    	{
    		prefs_custom_proxy.setSummary(String.format(
    				getString(R.string.prefs_custom_proxy_sryOn), 
    				prefs_custom_proxy.getText()));
    		prefs_custom_proxy_port.setSummary(String.format(
    				getString(R.string.prefs_custom_proxy_port_sryOn), 
    				prefs_custom_proxy_port.getText()));
    	}
    	else
    	{
    		prefs_custom_proxy.setSummary(String.format(
    				getString(R.string.prefs_custom_proxy_sryOff), 
    				DEFAULT_PROXY));
    		prefs_custom_proxy_port.setSummary(String.format(
    				getString(R.string.prefs_custom_proxy_port_sryOff), 
    				DEFAULT_PROXY_PORT));
    	}
    }
    
    private boolean isProxyActive()
    {
    	Boolean state = false;
    	
    	String currentProxy = Settings.Secure.
    			getString(getContentResolver(), Settings.Secure.HTTP_PROXY);

    	Log.d(TAG, "currentProxy: " + currentProxy);

    	if (currentProxy != null)
    		state = true;
    	
    	return state;
    }
    
    private boolean isU2NLActive()
    {
    	Boolean state = false;
    	
    	try {
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			BufferedReader osRes = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			os.writeBytes("ps | grep u2nl\n");
			os.flush();
			
			Thread.sleep(2000);
			
			String result = null;
			
			if (osRes.ready())
				result = osRes.readLine();
			
			if (result != null)
				state =  true;
			
			os.writeBytes("exit\n");
		  	os.flush();
		  	os.close();
			osRes.close();
   			process.waitFor();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
    	
    	return state;
    }
    
    private final static boolean validateIP(String ipAddress)
    {
        String[] parts = ipAddress.split( "\\." );
        
        if (parts.length != 4)
        {
            return false;
        }

        for ( String s : parts )
        {
            int i = Integer.parseInt( s );

            if ( (i < 0) || (i > 255) )
            {
                return false;
            }
        }

        return true;
    }
}
