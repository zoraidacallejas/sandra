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

package sandra.libs.vpa.vpalib;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import sandra.libs.tts.TTS;
import sandra.libs.util.xmllib.RetrieveXMLTask;
import sandra.libs.util.xmllib.XMLAsyncResponse;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

/**
* Chatbot/VPA that uses the technology of Pandorabots to understand the user queries and provide information
* in a specialized or general topic
*
* @author Michael McTear
* @author Zoraida Callejas
* @version 2.0, 09/10/13
*
*/

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Bot implements XMLAsyncResponse{
	
	private static final String LOGTAG = "BOTSPEECH";
	
	String id = "a80ce25abe344199";	//Id of the agent in Pandorabots
	String specializedTopic=null;	//Whether the bot can hold a generic or specialized conversation
	String queryText=null;			//Query to be performed
	Exception exception=null;		//If there is an exception when obtaining the results from Pandorabots, it is saved here. 
										//This way it is the class that uses the bot who has the responsibility to manage the exception (e.g. show a message to the user)
	
	Class<Activity> viewerActivity;
	Activity ctx;
	TTS myTts;
	
	/**
	 * Constructor for a bot with a specialized topic
	 * @param ctx 	Context for the creation of the bot
	 * @param id	Id of the bot in the Pandorabots service
	 * @param myTts	TTS engine already initialized
	 * @param specializedTopic	Topic of the conversation with the bot
	 */
	public Bot(Activity ctx, String id, TTS myTts, String specializedTopic){
		this.ctx=ctx;
		this.id=id;
		this.myTts=myTts;
		this.specializedTopic = specializedTopic;
	}
	
	/**
	 * Constructor for a bot with a generic topic
	 * @param ctx 	Context for the creation of the bot
	 * @param id	Id of the bot in the Pandorabots service
	 * @param myTts	TTS engine already initialized
	 */
	public Bot(Activity ctx, String id, TTS myTts){
		this.ctx=ctx;
		this.id=id;
		this.myTts=myTts;
	}
	
	/**
	 * Sends a text corresponding to the user input to the bot on the Pandorabots site. 
	 * @param query user input
	 */
	public void initiateQuery(String query){
		
		RetrieveXMLTask retrieveXML = new RetrieveXMLTask();	//AsyncTask to retrieve the contents of the XML file fro mthe URL
		retrieveXML.delegate = this;	//It is crucial in order to retrieve the data from the asyncrhonous task (see the AsyncResponse and RetrieveXMLTask classes)

		String fullQuery;
		
		//Check if it is a DBPedia query
		if(query.contains("dbpedia")) {
			// Insert %20 for spaces in query
			query = query.replaceAll(" ", "%20");
			fullQuery = query;
			Log.i(LOGTAG, "Query to DBPedia: "+fullQuery);
		}
		// It not, it is a general query to Pandorabots
		else {
			// insert %20 for spaces in query
			query = query.replaceAll(" ", "%20");
			
			//Uses AIML files from A.L.I.C.E
			fullQuery = "http://www.pandorabots.com/pandora/talk-xml?input="+ query + "&botid=" + id;
		}
		
		/*
		 * Start a background asynchronous query to Pandorabots, 
		 * When this process is finished, the "processXMLContents" method is invoked (see below).
		 */
		retrieveXML.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fullQuery, fullQuery); //An Executor that can be used to execute tasks in parallel.
		
		
		if(retrieveXML.getStatus() == (AsyncTask.Status.PENDING)) {
            //Indicates that the task has not been executed yet
			Log.i(LOGTAG, "Connecting to Pandorabots: Pending");
        } else if(retrieveXML.getStatus() == (AsyncTask.Status.RUNNING)) {
            //Indicates that the task is running
        	Log.i(LOGTAG, "Connecting to Pandorabots: Running");
        } else if(retrieveXML.getStatus() == (AsyncTask.Status.FINISHED)) {
            //Indicates that AsyncTask.onPostExecute has finished
        	Log.i(LOGTAG, "Connecting to Pandorabots: Finished");
        } 
	}
	
	/**
	 * Processes the results from Pandorabots in response to the query in the method <code>initiateQuery</code>
	 */
	@Override
	public void processXMLContents(String result) {
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource s = new InputSource(new StringReader(result));
			Document doc = dBuilder.parse(s);
	
			doc.getDocumentElement().normalize();
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
	
			// Result is from query to Bot on Pandorabots
			if(result.contains("<that>"))
			{
	            String output = (String) xpath.evaluate("//that", doc,XPathConstants.STRING);
	            output = output.replaceAll("<br> ", " ");
	            processOutput(output);
			}
			// Result is from query to DBPedia
			else {
				if(result.contains("dbpedia")){
	            
	                // check if there is a description text
					if(result.contains("<Description>"))
					{
						String description = (String) xpath.evaluate("//Description", doc, XPathConstants.STRING);
						description = description.replaceAll("<br> ", " ");
						myTts.speak(description, "EN");
					} else {
					// if there is no description text the query is sent to Google search
					// specializedTopic is set to null as this value is not required for this search	
			            Log.i(LOGTAG,"No result from dbpedia");
						specializedTopic=null;
						myTts.speak("Looking up Google", "EN");
						googleQuery(queryText, specializedTopic);
					}		
				}
			}
		}catch(Exception e){
			exception = e;
		}
	}

	/**
	 * Parses the response from the Pandora service
	 * @param output
	 * @throws Exception when the bot is not able to synthesize a message or the result cannot be parsed
	 */
	public void processOutput(String output) throws Exception {
		
		if(output!=null)
		{
			/*
			 * When <oob> tags are present, we assume that they have been marked up in the AIML 
			 * file with one of the tags: <search>, <launch>, <url>, and <dial>. 
			 */
			if (output.contains("<oob>")){
		        String[] parts = output.split("</oob>");
		        String oob = parts[0];
		        String textToSpeak = parts[1];
		        
		        String oobContent = oob.split("<oob>")[1].split("</oob>")[0];
		
		        Log.d(LOGTAG,"OOB: "+oobContent);
		        Log.d(LOGTAG,"Text: "+textToSpeak);
		
		        process_oobContent(oobContent,textToSpeak);
		        
			/*
			 * If it does not, the only task for the bot is to parse and synthesize the response. 
			 * For this, it must extract the message contained within the <that> tag. 
			 */    
	        } else
				myTts.speak(output, "EN");
		}
		else
			throw new Exception("Invalid result from the bot");
	} 

	/**
	 * Processes the contents of the oob tag and carries out the corresponding action: synthesizing a message, carrying
	 * out a web search, launching a web site, launching an app or dialing the phone
	 * @param oobContent
	 * @param textToSpeak
	 * @throws Exception 
	 */
	private void process_oobContent(String oobContent, String textToSpeak) throws Exception 
	{
		myTts.speak(textToSpeak, "EN");
		
		String query;

		if (oobContent.contains("<url>"))
			// perform a web search
			if (oobContent.contains("<search>"))
			{					 
					queryText = oobContent.split("<search>")[1].split("</search>")[0];
					Log.d(LOGTAG,"queryText= "+queryText);
					doSearch(queryText);
			}
			
			// request to launch a web site named in input
			else
			{
				query = oobContent.split("<url>")[1].split("</url>")[0];
				launchUrl(query);
			}
					
		// request to launch an app
		if (oobContent.contains("<launch>"))
		{ String app;
			 app = oobContent.split("<launch>")[1].split("</launch>")[0];

			 launchApp(app);
		}
		
		// request to launch phone
		if (oobContent.contains("<phone>"))
		{ 
			Intent intent = new Intent(Intent.ACTION_DIAL);
			ctx.startActivity(intent);	
		}
	}
	
	/**
	 * Carries out a web search in dbpedia
	 * @param searchText
	 */
	private void doSearch(String searchText) {
		searchText = searchText.replaceAll(" ", "+");
		String searchEngine = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch";
		String dbpediaQuery = searchEngine + "?QueryString="+searchText + "&MaxHits=1";
		Log.d(LOGTAG,"Query to DBPedia: "+dbpediaQuery);
		initiateQuery(dbpediaQuery);
	}
	
	/**
	 * Performs a Google search query. The value of specialized topic is appended to words in query
	 * 
	 */
	private void googleQuery(String googleSearchText, String specializedTopic){
		
		if(specializedTopic!=null && !googleSearchText.contains(specializedTopic))
		{
			googleSearchText = specializedTopic + " " +googleSearchText ;
			Log.d(LOGTAG," Google Search Text: "+googleSearchText);
		}
		
		// insert + for spaces in query
		googleSearchText = googleSearchText.replaceAll(" ", "+");
		String searchEngine = "https://www.google.com/search";
		String queryString = searchEngine + "?source=ig&rlz=&q="+googleSearchText;
		Log.d(LOGTAG,"Query String: "+queryString);

		launchUrl(queryString);
	}

	/**
	 * Launches an app
	 * @param app name of the app
	 * @throws Exception when the app cannot be launched
	 */
	private void launchApp(String app) throws Exception {
		PackageManager pm = ctx.getPackageManager();
        Intent launchApp = pm.getLaunchIntentForPackage(app);
        if (launchApp!=null) 
        	ctx.startActivity(launchApp);  
        else
        	throw new Exception ("Unable to launch "+app+" app");
    }
	
	/**
	 * Launches a web page
	 * @param url url to the web page
	 */
	private void launchUrl(String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		ctx.startActivity(browserIntent);
	}

}
