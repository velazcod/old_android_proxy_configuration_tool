package net.geekherd.metropcs.proxyswitcher;

import java.io.DataOutputStream;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Proxy;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class Toggler extends BroadcastReceiver
{
	private Context context;
	private SharedPreferences preferences;
	
	private Boolean useCustomProxy;
	private Boolean useU2NL;
	
	private String customProxy;
	private String customProxyPort;
	
	private String hostname;
	private String proxy;
	private String port;
	
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		loadPreferences();
		
		String action = intent.getAction();
			
		if (action.equals(Configuration.ACTION_ACTIVATE_PROXY))
		{
			Log.d(Configuration.TAG, "ACTION_ACTIVATE_PROXY action");
			
			enableProxy();
			enableU2NL();
		}
		else if (action.equals(Configuration.ACTION_DEACTIVATE_PROXY))
		{
			Log.d(Configuration.TAG, "ACTION_DEACTIVATE_PROXY action");
			
			disableProxy();
			disableU2NL();
		}
	}
	
	private void loadPreferences()
	{
		useCustomProxy = preferences.
			getBoolean(Configuration.PREF_USE_CUSTOM_PROXY, false);
		
		customProxy = preferences.
			getString(Configuration.PREF_PROXY, Configuration.PREF_PROXY_DEFAULT);
		
		customProxyPort = preferences.
			getString(Configuration.PREF_PROXY_PORT, Configuration.PREF_PROXY_PORT_DEFAULT);
		
		useU2NL = preferences.getBoolean(Configuration.PREF_USE_U2NL, 
				Configuration.PREF_USE_U2NL_DEFAULT);
		
		if (useCustomProxy)
		{
			hostname = customProxy + ':' + customProxyPort;
			proxy = customProxy;
			port = customProxyPort;
		}
		else
		{
			hostname = Configuration.DEFAULT_PROXY + ':' + Configuration.DEFAULT_PROXY_PORT;
			proxy = Configuration.DEFAULT_PROXY;
			port = Configuration.DEFAULT_PROXY_PORT;
		}
	}
	
	private void enableProxy()
	{
		Log.d(Configuration.TAG, "Enabling proxy");
		
		ContentResolver res = context.getContentResolver();
				
		Settings.Secure.putString(res, Settings.Secure.HTTP_PROXY, hostname);
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
	private void disableProxy()
	{
		Log.d(Configuration.TAG, "Disabling proxy");
		
		ContentResolver res = context.getContentResolver();
		
		//setting an empty string for the hostname disables proxy
		Settings.Secure.putString(res, Settings.Secure.HTTP_PROXY, "");
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
	private void enableU2NL()
	{
		if (!useU2NL)
			return;
		
		Log.d(Configuration.TAG, "Enabling U2NL");
		
		Process process = null;
	    try {
			process = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		catch (java.lang.RuntimeException e)
		{
			Log.e(Configuration.TAG, "Error getting root access");
			e.printStackTrace();
			return;
		}
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
		
		try { os.writeBytes("iptables -P INPUT ACCEPT" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -P OUTPUT ACCEPT" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -P FORWARD ACCEPT" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -F" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -t nat -F" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -X" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -t nat -A OUTPUT -o rmnet0 -p 6 ! -d " + proxy + " -j REDIRECT --to-port " + port + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("u2nl " + proxy + " " + port + " 127.0.0.1 1025 >/dev/null 2>&1 &" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("exit\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { process.waitFor(); } catch (InterruptedException e) { Log.e(Configuration.TAG, "InterruptedException: " + e); e.printStackTrace(); }
		
	}
	
	private void disableU2NL()
	{
		if (!useU2NL)
			return;
		
		Log.d(Configuration.TAG, "Disabling U2NL");
		
		Process process = null;
	    try {
			process = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		catch (java.lang.RuntimeException e)
		{
			Log.e(Configuration.TAG, "Error getting root access");
			e.printStackTrace();
			return;
		}
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
		
		try { os.writeBytes("iptables -P INPUT ACCEPT" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -P OUTPUT ACCEPT" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -P FORWARD ACCEPT" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -F" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -t nat -F" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("iptables -X" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("kill `ps|grep u2nl|grep -v grep|awk '{print $2}'`" + "\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		
		try { os.writeBytes("exit\n"); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { Log.e(Configuration.TAG, "IOException: " + e); e.printStackTrace(); }
		try { process.waitFor(); } catch (InterruptedException e) { Log.e(Configuration.TAG, "InterruptedException: " + e); e.printStackTrace(); }
	}
}
