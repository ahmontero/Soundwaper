package com.soundcloud.adc.net;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.util.Log;


/**
 * 
 * Manage all the networks stuff
 * 
 */
public class NetManager {
	
	// XML  --> http://api.soundcloud.com/tracks?consumer_key=J4D4ODBNeuHeXTQlbwUp3A&q=djquano
	// JSON --> http://api.soundcloud.com/tracks.json?consumer_key=J4D4ODBNeuHeXTQlbwUp3A&q=djquano
	
	private static AbstractHttpClient httpClient;

    private static final int CONNECTION_TIMEOUT = 10 * 1000;

    private static final int MAX_CONNECTIONS = 6;

    private static final String HTTP_USER_AGENT = "Android/Soundwaper";

    private static final String TAG = NetManager.class.getSimpleName();
	
	static {
        setupHttpClient();
    }

    public HttpResponse getHTTPResponse(URL tracksUrl) throws IllegalArgumentException{
        HttpGet request = null;
        HttpResponse response = null;

        if (tracksUrl == null){
            throw new IllegalArgumentException("Tracks URL was null");
        }
        
        try {//"http://waveforms.soundcloud.com/v5rXPhwTWiNy_m.png"
        	request = new HttpGet(tracksUrl.toURI());
        } catch (URISyntaxException e) {
            Log.e(TAG, "Could not create GetRequest: " + e.getMessage(), e);
        }  catch (NullPointerException e) {
            Log.e(TAG, "Could not create GetRequest as URL is null", e);
            throw new IllegalArgumentException(e);
        }
        
        try {
            response = httpClient.execute(request);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Client Protocol exception: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, "IOException exception: " + e.getMessage(), e);
        }

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Log.i(TAG, "Response code:[" + HttpStatus.SC_OK + "] Msg:["
                    + response.getStatusLine().getReasonPhrase() + "] Type:["
                    + response.getEntity().getContentType() + "] length:["
                    + response.getEntity().getContentLength() + "]");
        } else {
            Log.e(TAG, "Unsuccessful Connection response: " + response.getStatusLine().getStatusCode());
        }
        return response;
    }

    private static void setupHttpClient() {
    	
    	 BasicHttpParams httpParams = new BasicHttpParams();

         ConnManagerParams.setTimeout(httpParams, CONNECTION_TIMEOUT);
         ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(MAX_CONNECTIONS));
         ConnManagerParams.setMaxTotalConnections(httpParams, MAX_CONNECTIONS);
         
         HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setUserAgent(httpParams, HTTP_USER_AGENT);

         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

         ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
         httpClient = new DefaultHttpClient(cm, httpParams);
    }   
}