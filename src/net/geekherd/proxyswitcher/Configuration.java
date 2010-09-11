/*
 * Copyright (C) 2010 Daniel Velazco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.geekherd.proxyswitcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Proxy;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class Configuration extends PreferenceActivity 
{
	/*
	 * 
	 * Main screen.
	 * We use this activity to allow the user to manually activate/deactivate proxy,
	 * as well as change default settings for the proxy.
	 * 
	 * Can be improved.
	 * 
	 */
	
	/** Default Settings **/
	public final static String TAG = "ProxySwitcher";
	public final static String DEFAULT_METRO_PROXY = "10.223.2.4"; //MetroPCS Proxy Server (proxy.metropcs.com or wap.metropcs.com)
	public final static String DEFAULT_METRO_PROXY_PORT = "3128"; // MetroPCS Proxy Server Port
	public final static String DEFAULT_METRO_MMS = "10.223.2.4"; //MetroPCS Proxy Server (mms.metropcs.com) //TODO: update ip with correct one
	public final static String DEFAULT_METRO_MMS_PORT = "3128"; // MetroPCS Proxy Server Port
	
	public final static String DEFAULT_CRICKET_PROXY = "10.132.25.254"; //Cricket Proxy Server
	public final static String DEFAULT_CRICKET_PROXY_PORT = "8080"; // Cricket Proxy Server Port
	public final static String DEFAULT_CRICKET_MMS = "10.132.25.254"; //Cricket Proxy Server //TODO: update ip with correct one
	public final static String DEFAULT_CRICKET_MMS_PORT = "8080"; // Cricket Proxy Server Port
	/** Default Settings **/

	/** Network Interfaces **/
	public final static String DEFAULT_INTERFACE = "rmnet0"; //DEFAULT INTERFACE USED BY IPTABLES
	public final static String DEFAULT_INTERFACE_MOTO_SHOLES = "ppp0"; //INTERFACE USED ON MOTOROLA DROID 1 PRE-FROYO
	public final static String DEFAULT_INTERFACE_MOTO_SHOLES_FROYO = "ppp0"; //INTERFACE USED ON MOTOROLA DROID 1 ON FROYO /*TODO: THIS IS NOT RIGHT! */
	public final static String DEFAULT_INTERFACE_HTC = "rmnet0"; //INTERFACE USED ON MOST HTC DEVICES
	/** Network Interfaces **/
	
	public static String ACTION_ACTIVATE_ALL = "ActivateAll";
	public static String ACTION_ACTIVATE_PROXY = "ActivateProxy";
	public static String ACTION_ACTIVATE_U2NL = "ActivateU2NL";
	public static String ACTION_DEACTIVATE_ALL = "DectivateAll";
	public static String ACTION_DEACTIVATE_PROXY = "DectivateProxy";
	public static String ACTION_DEACTIVATE_U2NL = "DeactivateU2NL";
	
	/** Preference Screen Constants **/
	public final static String PROXY_STATUS = "proxy_status";
	
	public final static String PREF_CREDITS = "prefs_credits";
	
	public final static String TOGGLE_ACTIVATE = "toggle_activate";
	public final static String TOGGLE_DEACTIVATE = "toggle_deactivate";
	/** Preference Screen Constants **/
	
	/** Preferences Constants **/
	public final static String CARRIER_METROPCS = "metropcs";
	public final static String CARRIER_CRICKET = "cricket";
	
	public final static String PREF_CARRIER_SELECTION = "prefs_carrier_selection";
	public final static String PREF_CARRIER_SELECTION_DEFAULT = CARRIER_METROPCS;
	
	public final static String PREF_AUTO_SWITCH_ENABLED = "prefs_autoswitch_enabled";
	public final static Boolean PREF_AUTO_SWITCH_ENABLED_DEFAULT = true;
	
	public final static String PREF_USE_U2NL = "prefs_use_u2nl";
	public final static Boolean PREF_USE_U2NL_DEFAULT = true;
	
	public final static String PREF_USE_CUSTOM_PROXY = "prefs_use_custom_proxy";
	public final static Boolean PREF_USE_CUSTOM_PROXY_DEFAULT = false;
	
	public final static String PREF_PROXY = "prefs_custom_proxy";
	public final static String PREF_PROXY_DEFAULT = DEFAULT_METRO_PROXY;
	
	public final static String PREF_PROXY_PORT = "prefs_custom_proxy_port";
	public final static String PREF_PROXY_PORT_DEFAULT = DEFAULT_METRO_PROXY_PORT;
	
	public final static String PREF_USE_MMS_U2NL = "prefs_use_mms_u2nl";
	public final static Boolean PREF_USE_MMS_U2NL_DEFAULT = true;
	
	public final static String PREF_USE_CUSTOM_MMS = "prefs_use_custom_mms";
	public final static Boolean PREF_USE_CUSTOM_MMS_DEFAULT = false;
	
	public final static String PREF_MMS = "prefs_custom_mms";
	public final static String PREF_MMS_DEFAULT = DEFAULT_METRO_MMS;
	
	public final static String PREF_MMS_PORT = "prefs_custom_mms_port";
	public final static String PREF_MMS_PORT_DEFAULT = DEFAULT_METRO_MMS_PORT;
	/** Preferences Constants **/
	
	
	private PreferenceScreen proxy_status;
	private PreferenceScreen toggle_activate;
	private PreferenceScreen toggle_deactivate;
	
	private CheckBoxPreference prefs_autoswitch_enabled;
	private ListPreference prefs_carrier_selection;
	
	private CheckBoxPreference prefs_use_u2nl;
 
	private PreferenceScreen prefs_custom_settings;
	
	private CheckBoxPreference prefs_use_custom_proxy;
	private EditTextPreference prefs_custom_proxy;
	private EditTextPreference prefs_custom_proxy_port;
	
	private CheckBoxPreference prefs_use_custom_mms;
	private EditTextPreference prefs_custom_mms;
	private EditTextPreference prefs_custom_mms_port;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	super.onCreate(savedInstanceState);
    	setProgressBarIndeterminateVisibility(true);
        addPreferencesFromResource(R.xml.configuration);
        
        proxy_status = (PreferenceScreen)findPreference("proxy_status");
        toggle_activate = (PreferenceScreen)findPreference("toggle_activate");
        toggle_deactivate = (PreferenceScreen)findPreference("toggle_deactivate");
        
        prefs_autoswitch_enabled = (CheckBoxPreference)findPreference("prefs_autoswitch_enabled");
        
        prefs_carrier_selection = (ListPreference)findPreference("prefs_carrier_selection");
        
        prefs_use_u2nl = (CheckBoxPreference)findPreference("prefs_use_u2nl");
        
        prefs_custom_settings = (PreferenceScreen)findPreference("prefs_custom_settings");
 
        prefs_use_custom_proxy = (CheckBoxPreference)findPreference("prefs_use_custom_proxy");
        prefs_custom_proxy = (EditTextPreference)findPreference("prefs_custom_proxy");
        prefs_custom_proxy_port = (EditTextPreference)findPreference("prefs_custom_proxy_port");
        
        prefs_use_custom_proxy.setOnPreferenceChangeListener(customProxyCheckboxListener);
        prefs_custom_proxy.setOnPreferenceChangeListener(customProxyEditTextListener);
        prefs_custom_proxy_port.setOnPreferenceChangeListener(customProxyPortEditTextListener);
        
        prefs_use_custom_mms = (CheckBoxPreference)findPreference("prefs_use_custom_mms");
        prefs_custom_mms = (EditTextPreference)findPreference("prefs_custom_mms");
        prefs_custom_mms_port = (EditTextPreference)findPreference("prefs_custom_mms_port");
        
        prefs_use_custom_mms.setOnPreferenceChangeListener(customMMSCheckboxListener);
        prefs_custom_mms.setOnPreferenceChangeListener(customMMSEditTextListener);
        prefs_custom_mms_port.setOnPreferenceChangeListener(customMMSPortEditTextListener);
    }
    
    private BroadcastReceiver mProxyChangeActionReceiver = new BroadcastReceiver()
	{
    	@Override
		public void onReceive(Context context, Intent intent) 
		{
    		new checkStatus().execute();
		}
	};
    
    /*
     * As soon as the activity resumes, check for the state of the proxy/u2nl.
     */
    @Override
	protected void onResume()
	{
		super.onResume();
		
		updateCustomProxySummary(prefs_use_custom_proxy.isChecked(), null, null);
		updateCustomMMSSummary(prefs_use_custom_mms.isChecked(), null, null);
		
		new checkStatus().execute();
		
		this.registerReceiver(this.mProxyChangeActionReceiver, 
					new IntentFilter(Proxy.PROXY_CHANGE_ACTION));
	}
    
    /*
     * Here we test the custom proxy/mms servers to see if they are valid.
     */
    @Override
	protected void onStop()
	{
		super.onStop();
		
		this.unregisterReceiver(this.mProxyChangeActionReceiver);
		
		if (prefs_use_custom_proxy.isChecked())
		{
			if (!testCustomProxy(null, null))
			{
				prefs_use_custom_proxy.setChecked(false);
				prefs_custom_proxy.setText("");
				prefs_custom_proxy_port.setText("");
				
				Log.d(TAG, "Invalid ip address and/or port number. Cannot use custom proxy.");
				Toast.makeText(this, getString(R.string.prefs_use_custom_proxy_error), Toast.LENGTH_LONG).show();
			}
		}
		
		if (prefs_use_custom_mms.isChecked())
		{
			if (!testCustomMMSServer(null, null))
			{
				prefs_use_custom_mms.setChecked(false);
				prefs_custom_mms.setText("");
				prefs_custom_mms_port.setText("");
				
				Log.d(TAG, "Invalid ip address and/or port number. Cannot use custom MMS server.");
				Toast.makeText(this, getString(R.string.prefs_use_custom_mms_error), Toast.LENGTH_LONG).show();
			}
		}
	}
    
    /*
     * This checks if a PreferenceScreen has been clicked on and acts accordingly.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) 
    {
    	String key = preference.getKey();
    	
    	if (TOGGLE_ACTIVATE.equals(key))
    	{	
    		Intent sIntent = new Intent(this, Toggler.class);
    		sIntent.setAction(ACTION_ACTIVATE_ALL);
    		sendBroadcast(sIntent);
    	} 
    	else if (TOGGLE_DEACTIVATE.equals(key))
    	{
    		Intent sIntent = new Intent(this, Toggler.class);
    		sIntent.setAction(ACTION_DEACTIVATE_ALL);
    		sendBroadcast(sIntent);
    	}
    	else if (PREF_CREDITS.equals(key))
    	{
    		showCredits();
    	}
    	
    	return true;
    }
    
    /*
     * Used to temporarily disable some preferences while we
     * check for the state of the proxy/u2nl using the ASyncTask.
     */
    private void disableToggles()
    {
    	toggle_activate.setEnabled(false);
    	toggle_deactivate.setEnabled(false);
    	
    	prefs_autoswitch_enabled.setEnabled(false);
    	prefs_carrier_selection.setEnabled(false);
    	prefs_use_u2nl.setEnabled(false);
    	prefs_custom_settings.setEnabled(false);
    }
    
    /*
     * Re-enable preferences.
     */
    private void enableToggles()
    {
    	toggle_activate.setEnabled(true);
    	toggle_deactivate.setEnabled(true);

    	prefs_autoswitch_enabled.setEnabled(true);
    	prefs_carrier_selection.setEnabled(true);
    	prefs_use_u2nl.setEnabled(true);
    	prefs_custom_settings.setEnabled(true);
    }
    
    /*
     * ASyncTask helps update the current state of the proxy 
     * and u2nl when a change is made. (User activating/deactivating proxy/u2nl)
     * or Wifi Changed BroadcastReceiver toggles it.
     */
    private class checkStatus extends AsyncTask<Void, Void, Boolean> 
	{
    	String proxyStatus = getString(R.string.status_inactive);
    	String u2nlStatus = getString(R.string.status_inactive);
    	
    	protected void onPreExecute()
    	{
    		Log.d(TAG, "checking proxy/u2nl status");
    		setProgressBarIndeterminateVisibility(true);
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
    		setProgressBarIndeterminateVisibility(false);
    		proxy_status.setSummary(String.format(
    				getString(R.string.proxy_status_sry), 
    				proxyStatus, 
    				u2nlStatus));
    		
    		enableToggles();
    	}
	}
    
    /*
     * Checks if a proxy server is currently set
     */
    private boolean isProxyActive()
    {
    	Boolean state = false;
    	
    	String currentProxy = Settings.Secure.
    			getString(getContentResolver(), Settings.Secure.HTTP_PROXY);

    	Log.d(TAG, "currentProxy: " + currentProxy);

    	if (currentProxy != null)
    		if (!currentProxy.equals(""))
    			state = true;
    	
    	return state;
    }
    
    /*
     * Checks if the u2nl program is running. 
     * 
     * There might be a better way for this.
     */
    private boolean isU2NLActive()
    {
    	Boolean state = false;
    	
    	try {
			Process process = Runtime.getRuntime().exec("su 1000");
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
    
	/*
	 * Update the Preference summary with the used Proxy server/port
	 */
	private void updateCustomProxySummary(Object value, String proxy, String port)
	{
		String customProxy;
		String customPort;
		
		if (proxy != null)
			customProxy = proxy;
		else
			customProxy = prefs_custom_proxy.getText();
		
		if (port != null)
			customPort = port;
		else
			customPort = prefs_custom_proxy_port.getText();
		
		if (value.equals(true))
		{
			if (testCustomProxy(customProxy, customPort))
			{
					Log.d(TAG, "custom proxy is valid!");
				
	    			prefs_custom_proxy.setSummary(String.format(
	    					getString(R.string.prefs_custom_proxy_sryOn), 
	    					customProxy));
	    			prefs_custom_proxy_port.setSummary(String.format(
	    					getString(R.string.prefs_custom_proxy_port_sryOn), 
	    					customPort));
			} else {
				Log.d(TAG, "custom proxy is invalid!");
				
				prefs_custom_proxy.setSummary(getString(R.string.prefs_custom_proxy_invalid));
				prefs_custom_proxy_port.setSummary(getString(R.string.prefs_custom_proxy_invalid));
			}
	
		} else {
			prefs_custom_proxy.setSummary(String.format(
					getString(R.string.prefs_custom_proxy_sryOff), 
					DEFAULT_METRO_PROXY));
			prefs_custom_proxy_port.setSummary(String.format(
					getString(R.string.prefs_custom_proxy_port_sryOff), 
					DEFAULT_METRO_PROXY_PORT));
		}
	}
	
	private boolean testCustomProxy(String proxy, String port)
	{
		Boolean validProxy = true;
		
		String customProxy;
		String customPort;
		
		if (proxy !=null)
			customProxy = proxy;
		else
			customProxy = prefs_custom_proxy.getText();
		
		
		if (port != null)
			customPort = port;
		else
			customPort = prefs_custom_proxy_port.getText();
		
		if (!validateIP(customProxy))
		{
			Log.e(TAG, "Invalid ip address");
			validProxy = false;
		}
		
		try
		{
			int portInt = Integer.parseInt(customPort);
			
			if (portInt < 0 || portInt > 65535)
			{
				Log.e(TAG, "Invalid port number");
				validProxy = false;
			}
			
		} catch (NumberFormatException npe)
		{
			npe.printStackTrace();
			Log.e(TAG, "Invalid port number");
			validProxy = false;
		}
		
		return validProxy;
	}
	
	/*
	 * Update the Preference summary with the used MMS server/port
	 */
	private void updateCustomMMSSummary(Object value, String server, String port)
	{
		String customProxy;
		String customPort;
		
		if (server != null)
			customProxy = server;
		else
			customProxy = prefs_custom_mms.getText();
		
		if (port != null)
			customPort = port;
		else
			customPort = prefs_custom_mms_port.getText();
		
		if (value.equals(true))
		{
			if (testCustomProxy(customProxy, customPort))
			{
					Log.d(TAG, "custom MMS server is valid!");
				
					prefs_custom_mms.setSummary(String.format(
	    					getString(R.string.prefs_custom_mms_sryOn), 
	    					customProxy));
	    			prefs_custom_mms_port.setSummary(String.format(
	    					getString(R.string.prefs_custom_mms_port_sryOn), 
	    					customPort));
			} else {
				Log.d(TAG, "custom mms is invalid!");
				
				prefs_custom_mms.setSummary(getString(R.string.prefs_custom_mms_invalid));
				prefs_custom_mms_port.setSummary(getString(R.string.prefs_custom_mms_invalid));
			}
	
		} else {
			prefs_custom_mms.setSummary(String.format(
					getString(R.string.prefs_custom_mms_sryOff), 
					DEFAULT_METRO_MMS));
			prefs_custom_mms_port.setSummary(String.format(
					getString(R.string.prefs_custom_mms_port_sryOff), 
					DEFAULT_METRO_MMS_PORT));
		}
	}
	
	/*
	 * Method that helps validate the MMS server/port
	 */
	private boolean testCustomMMSServer(String server, String port)
	{
		Boolean validProxy = true;
		
		String customProxy;
		String customPort;
		
		if (server !=null)
			customProxy = server;
		else
			customProxy = prefs_custom_mms.getText();
		
		
		if (port != null)
			customPort = port;
		else
			customPort = prefs_custom_mms_port.getText();
		
		if (!validateIP(customProxy))
		{
			Log.e(TAG, "Invalid ip address");
			validProxy = false;
		}
		
		try
		{
			int portInt = Integer.parseInt(customPort);
			
			if (portInt < 0 || portInt > 65535)
			{
				Log.e(TAG, "Invalid port number");
				validProxy = false;
			}
			
		} catch (NumberFormatException npe)
		{
			npe.printStackTrace();
			Log.e(TAG, "Invalid port number");
			validProxy = false;
		}
		
		return validProxy;
	}
	
	private Preference.OnPreferenceChangeListener 
		customProxyCheckboxListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomProxySummary(newValue, null, null);
			
			return true;
		}
	};
	
	private Preference.OnPreferenceChangeListener 
		customProxyEditTextListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomProxySummary(true, newValue.toString(), null);
			
			return true;
		}
	};
	
	private Preference.OnPreferenceChangeListener 
		customProxyPortEditTextListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomProxySummary(true, null, newValue.toString());
			
			return true;
		}
	};
	
    private Preference.OnPreferenceChangeListener 
		customMMSCheckboxListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomMMSSummary(newValue, null, null);
			
			return true;
		}
	};
	
	private Preference.OnPreferenceChangeListener 
		customMMSEditTextListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomMMSSummary(true, newValue.toString(), null);
			
			return true;
		}
	};
	
	private Preference.OnPreferenceChangeListener 
		customMMSPortEditTextListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomMMSSummary(true, null, newValue.toString());
			
			return true;
		}
	};
    
	/*
	 * Method that helps validate IP addresses.
	 */
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
    
    /*
     * AlertDialog with special credits to special people :)
     */
    private void showCredits()
    {
    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle(this.getString(R.string.credits));
    	alertDialog.setMessage(this.getString(R.string.credits_txt));
    	alertDialog.setIcon(R.drawable.icon);
    	alertDialog.setButton(this.getString(R.string.credits_ok), new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {	
    			return;
    		}
    	});
    	alertDialog.show();
    }
}
