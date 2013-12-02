/*
 *  Copyright 2013 Zoraida Callejas and Michael McTear
 * 
 *  This file is part of the Sandra (Speech ANDroid Apps) Toolkit, from the book:
 *  Voice Application Development for Android, Michael McTear and Zoraida Callejas, 
 *  PACKT Publishing 2013 <http://www.packtpub.com/voice-application-development-for-android/book>,
 *  <http://lsi.ugr.es/zoraida/androidspeechbook>
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>. 
 */

package sandra.libs.util.xmllib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

/**
 * Asynchronous task to retrieve the XML code from an URL. 
 * Parameters:  <input, progress, result>
 * 		- It receives a collection of Strings as input parameters
 * 		- It does not produce any type of progress values (void)
 * 		- It produces a String as a result of the background computation
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.3, 08/18/13
 */	

public class RetrieveXMLTask extends AsyncTask<String, Void, String> {
	
	public XMLAsyncResponse delegate=null; 	//Object employed to send the results back to the invoking activity
	
	/*
	 * Object used to account for the exceptions that may happen during the background computation.
	 * 
	 * It is not possible to throw Exceptions in the doInBackground method because it must have exactly
	 * the same header specified in the AsyncTask class (and thus we cannot add "throws Exception")
	 */
	public Exception exception = null;		


	/**
	 * Writes a string with the contents of the file in the specified URL
	 * Modified from the method with the same name in http://www.edumobile.org/android/android-programming-tutorials/data-fetching/ 
	 * 
	 * @note Pay attention to include the Internet permission in your manifest: (<uses-permission android:name="android.permission.INTERNET" /> )
	 * 
	 * @note We use HttpURLConnection (from Android), though it is also possible to use DefaultHttpClient (from Apache). For new apps it is recommended to use HttpURLconnection better
	 * (the android documentation redirects to this blog: http://android-developers.blogspot.com/2011/09/androids-http-clients.html) 
	 */
	private String saveXmlInString(String urlString)
	{
	    InputStream in = null;
	    int response = -1;
	    String result=null;
	                
	    URL url=null;
	    HttpURLConnection connection=null;
	    
	    try { 
	    	 url = new URL(urlString);
	    	 connection = (HttpURLConnection) url.openConnection();
	    	 connection.setAllowUserInteraction(false);
	    	 connection.setInstanceFollowRedirects(true);
	    	 connection.setRequestMethod("GET");
	    	 connection.connect();
	     	 response = connection.getResponseCode();
	     	
	     	 if (response == HttpURLConnection.HTTP_OK) {
	        	in = connection.getInputStream();                                
	     	 }
	        
	     	 result = readStreamToString(in);

	     	 /*
	 	     * http://developer.android.com/reference/java/net/HttpURLConnection.html
	 	     * Once the response body has been read, the HttpURLConnection should be closed by calling disconnect(). 
	 	     * Disconnecting releases the resources held by a connection so they may be closed or reused. 
	 	     */
	 	     connection.disconnect(); 
	    }
	    catch (Exception ex)
	    {
	    	exception = ex; 		
	    }
	    
	    return result;    
	}
	
	
	/**
	 * Creates a string with the contents read from an input stream
	 * Follows the method suggested here: http://stackoverflow.com/questions/2492076/android-reading-from-an-input-stream-efficiently
	 * @throws Exception When the inputstream is null or there is an error while reading it
	 */
	private String readStreamToString(InputStream in) throws IOException {
		
		if(in==null){
			throw new IOException("InputStream could not be read (in==null)");
		}
				
		BufferedReader bufRead = new BufferedReader(new InputStreamReader(in));
		StringBuilder text = new StringBuilder();
		String line;
		
		while ((line = bufRead.readLine()) != null) 
		    text.append(line); //May throw an IOException
	
		return text.toString();
		
	} 
	

	/**
	 * Sends the results back to the invoking activity using the AsyncResponse instance "delegate" (see the AsyncResponse class).
	 * 
	 * It is invoked when the background computation is finished. 
	 */
	@Override
	public void onPostExecute(String xml) {
    	if(exception==null)
    		delegate.processXMLContents(xml);
    	else
    		delegate.processXMLContents("NetworkException - "+exception.getMessage());	
    }


	/**
	 * Saves the contents of a file in the specified URL in a string
	 * @params urls urls[0] is the url provided by the user, urls[1] is a predefined url that can be used in case the first one is not available
	 */
	@Override
	public String doInBackground(String... urls) {

		String xml_contents;
		xml_contents = saveXmlInString(urls[0]);
		
		if(exception!=null)
			xml_contents = saveXmlInString(urls[1]);
		
		return xml_contents;
	}

 }