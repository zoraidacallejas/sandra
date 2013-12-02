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

package sandra.examples.formfill.musicbrain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import org.xmlpull.v1.XmlPullParserException;

import sandra.examples.formfill.musicbrain.R;
import sandra.libs.dm.formfilllib.DialogInterpreter;
import sandra.libs.dm.formfilllib.Form;
import sandra.libs.dm.formfilllib.FormFillLibException;
import sandra.libs.dm.formfilllib.VXMLParser;
import sandra.libs.util.xmllib.RetrieveXMLTask;
import sandra.libs.util.xmllib.XMLAsyncResponse;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


/**
 * MusicBrain: App that engages in a form-filling dialog in which the user provides a word,
 * and a start and end date, and the app shows a list of all music albums in the MusicBrainZ
 * database that contain the word in the title and were released between the dates provided
 * 
 * It uses the <code>ASRLib</code> and <code>TTSLib</code> libraries for speech recognition
 * and synthesis, and the <code>FormFillLib</code> for dialog management. 
 * 
 * The structure of the form-filling dialog is indicated in a vxml file (see Chapter 5),
 * which is retrieved from the web using the <code>XMLLib</code> library.
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 3.0, 08/18/13
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MusicBrain extends DialogInterpreter implements XMLAsyncResponse{

	private static final String LOGTAG = "MUSICBRAIN";
	
	//URL with the vxml file that contains the structure of the dialog
	private static final String URL_VXML = "http://lsi.ugr.es/zoraida/androidspeechbook/code/sandra/examples/formfill/musicbrain.vxml";
	private static final String DEFAULT_URL_VXML = "http://lsi.ugr.es/zoraida/androidspeechbook/code/sandra/examples/formfill/musicbrain.vxml";
	
	//Contains the albums that correspond to the user query
	HashMap<String,String> albumData;

	/**
	 * Initializes the GUI and starts the spoken dialog
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_fill_music_brain);	
		setRestartButton();
		
		//Start the interpretation of the VXML file
		startDialog();
	}
	
	
	/**
	 * Initializes the restart button and its listener. When the button is pressed, the activity is 
	 * started from the scratch. 
	 * It is useful to carry out another interaction after a previous
	 * one has been completed, or to restart the app after a problem with the Internet connection.
	 */
	private void setRestartButton() {
		Button speak = (Button) findViewById(R.id.restartbtn);
		speak.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setListView(new ArrayList<String>()); 	//Clear listView
					setTextView(true);						//Clear textView
					startDialog();		//Start interpreting the VXML file again
				}
			});
	}
	
	/**
	 * Initializes the ASR and TTS engines and retrieves the vxml file from the web.
	 * When the file has been successfully retrieved, the <code>processXMLContents</code> method is invoked,
	 * which parses and interprets the dialog
	 */
	@SuppressLint("NewApi")
	void startDialog(){
		try{
			initializeAsrTts();
			retrieveXML(URL_VXML, DEFAULT_URL_VXML);
		} catch (Exception e) {
			Log.e(LOGTAG, "Internet connection error");
			createAlert("Connnection error", "Please check your Internet connection").show();
		}
	}
	
	/**
	 * Retrieves the contents of an XML file in the specified url. If the first url (url) is not accessible,
	 * the second url (url_default) is used.
	 * When the file has been successfully retrieved, the <code>processXMLContents</code> method is invoked,
	 * which parses and interprets the dialog
	 */
	public void retrieveXML(String url, String url_default){
		RetrieveXMLTask retrieveXML = new RetrieveXMLTask();	//AsyncTask to retrieve the contents of the XML file fro mthe URL
		retrieveXML.delegate = this;	//It is crucial in order to retrieve the data from the asyncrhonous task (see the AsyncResponse and RetrieveXMLTask classes)

		
		/*
		 * The string corresponding to the XML file is retrieved with an asynchronous task, and thus
		 * is executed in the background. When this process is finished, the "processXMLContents" method
		 * is invoked (see below).
		 */
		retrieveXML.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, url_default); //An Executor that can be used to execute tasks in parallel.
		
		
		//See reference here: http://developer.android.com/reference/android/os/AsyncTask.Status.html
		if(retrieveXML.getStatus() == (AsyncTask.Status.PENDING)) {
            //Indicates that the task has not been executed yet
			Log.i(LOGTAG, "VXML reading: Pending");
        } else if(retrieveXML.getStatus() == (AsyncTask.Status.RUNNING)) {
            //Indicates that the task is running
        	Log.i(LOGTAG, "VXML reading: Running");
        } else if(retrieveXML.getStatus() == (AsyncTask.Status.FINISHED)) {
            //Indicates that AsyncTask.onPostExecute has finished
        	Log.i(LOGTAG, "VXML reading: Finished");
        } 
	}
	
	/**
	 * It is invoked when a VXML file has been read from a URL.
	 * It parses and interprets the VXML code received in the input string.
	 * 
	 * @param xmlContent VXML code containing the structure of the form-filling dialog
	 */
	@Override
	public void processXMLContents(String xmlContent) {

		if(!xmlContent.contains("musicbrainz")){
			Form form;
			
				
				try {
					form = VXMLParser.parseVXML(xmlContent);
					startInterpreting(form);
				} catch (XmlPullParserException ex) {
					Log.e(LOGTAG, "Error parsing the VXML file: "+ex.getMessage());
					createAlert("Parsing error", "Please check your Internet connection").show();
				} catch (FormFillLibException ex) {
					Log.e(LOGTAG, ex.getMessage());
					createAlert("Parsing error", ex.getReason()).show();
				}
		}
		else{
				parseMusicResults(xmlContent);			
		}
	}
	
	
	/**
	 * Parses the content of the XML response from the MusicBrainZ web service.
	 * The result from the parsing is a collection of albums already sorted by dates and without duplicates
	 * (see the explanation in <code>MusicBrainParser.java</code>). These albums contain the word uttered by the user
	 * in their title. This collection is filtered to keep only the albums released between the dates 
	 * indicated by the user. The filtered collection is shown in the listView of the GUI  
	 * 
	 * @param xmlContent Result from the query to the MusicBrainZ service
	 */
	void parseMusicResults(String xmlContent){

		TreeSet<Album> results;
		
			try {
				results = MusicBrainParser.parse(xmlContent);
				showResults(filterAlbums(results));
			} catch (XmlPullParserException ex) {
				Log.e(LOGTAG, "Error parsing the VXML file: "+ex.getMessage());
				createAlert("Parsing error", "The VXML may be not accessible or ill-formed").show();
			} catch (MusicBrainException ex) {
				Log.e(LOGTAG, ex.getMessage());
				createAlert("Parsing error", ex.getReason()).show();
			}		
	}
	
	/**
	 * From the list of Albums returned by the MusicBrainZ web service (the ones that have
	 * the word specified by the user in their title), it keeps the ones that were released
	 * between the dates indicated by the user
	 * 
	 * If the years recognized are not valid (are not in the form YYYY, e.g. 1997), then the
	 * albums are not filtered
	 * 
	 * @param albumList List of the albums in MusicBrainZ which title contains the word recognized
	 */
	private TreeSet<Album> filterAlbums(TreeSet<Album> albumList){
		
		TreeSet<Album> filteredList = new TreeSet<Album>(new AlbumComparator());
		
		try{
			SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.ENGLISH);
			Date initialDate = format.parse(albumData.get("initialyear")); 
			Date finalDate = format.parse(albumData.get("finalyear"));	
		
			if(initialDate.after(finalDate) || initialDate.after(new Date())){
				throw new ParseException("The initial date is posterior to the final date or today", 0);
			}
			for(Album album: albumList){
				if(album.getDate()!=null) {
					if(album.getDate().after(initialDate) && album.getDate().before(finalDate)){
						filteredList.add(album);
					}
				}
			}
		} catch (ParseException ex) {
			/*
			 * The exception is raised when the dates recognized are not in the form YYYY (e.g. 2010).
			 * It is important to take into account that the ASR was not restricted, thus it is possible
			 * that the best recognition result is not even a date (e.g. the recognizer might recognize
			 * "say something" instead of "2010".
			 */
			filteredList = albumList;
			Log.e(LOGTAG, "The album list could not be filtered by date");
		}
		
		return filteredList;
	}
	
	/**
	 * Formats the results and shows them in the GUI list view
	 * @param albums List of the albums that meet the user query
	 */
	private void showResults(TreeSet<Album> albums){
		
		ArrayList<String> albumInfo = new ArrayList<String>();
		for(Album album: albums)
		{
			/*
			 * Takes into account that in the XML provided by the web service, not all the albums
			 * have all the data available (e.g. some do not indicate date or label) 
			 */
			if(album.getDate()!=null)
			{
				if(album.getLabel()!=null)
					albumInfo.add("\""+album.getTitle()+"\" by "+album.getInterpreter()+ " ("+album.getLabel()+", "+new SimpleDateFormat("yyyy", Locale.ENGLISH).format(album.getDate())+")");
				else
					albumInfo.add("\""+album.getTitle()+"\" by "+album.getInterpreter()+ " ("+new SimpleDateFormat("yyyy", Locale.ENGLISH).format(album.getDate())+")");
			}
			else
			{
				if(album.getLabel()!=null)
					albumInfo.add("\""+album.getTitle()+"\" by "+album.getInterpreter()+ " ("+album.getLabel()+")");
				else
					albumInfo.add("\""+album.getTitle()+"\" by "+album.getInterpreter());
			}
		}
		
		setTextView(false); //TextView shows the data used to query the web service (blank=false)
		setListView(albumInfo); //ListView shows the results retrieved from the web service
		
	}
	
	private void setTextView(Boolean blank){
		
		
		TextView textview = (TextView) findViewById(R.id.resultTxt);
		String message;
		
		if(!blank){
			/*
			 * Takes into account that the dates might not have been used for filtering the 
			 * results if they could not be parsed as years 
			 */
			message = "Albums containing the word \""+albumData.get("query")+"\"";
			
			try{
				SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.ENGLISH);
				format.parse(albumData.get("initialyear"));
				format.parse(albumData.get("finalyear"));
				
				message += " released from "+albumData.get("initialyear")+" to "+albumData.get("finalyear");
			}
			catch (Exception ex){
				message += " with any release date (the dates indicated could not be processed)";
			}
		}
		else
			message = getResources().getString(R.string.resulttxt); //Shows the default message for the textview
		
		textview.setText(message);
	}

	/**
	 * Once the VXML file has been interpreted, the results obtained are saved in "albumData",
	 * and the web service of MusicBrainZ is queried
	 */
	@Override
	public void processDialogResults(HashMap<String, String> result) {
		Log.i(LOGTAG, "Dialogue end. The results are: "+result);
		albumData = result;
		retrieveXML("http://www.musicbrainz.org/ws/2/release/?query=release:"+albumData.get("query"), "http://www.musicbrainz.org/ws/2/release/?query=release:Android");
	}
	
	/**
	 * Includes the results in the list view
	 */
	private void setListView(ArrayList<String> albumInfo){
		
		// Instantiates the array adapter to populate the listView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albumInfo);
        ListView listView = (ListView) findViewById(R.id.resultList);
        listView.setAdapter(adapter);
	}

	/**
	 * Creates an alert dialog in which the user must click OK to continue.
	 * It is used in the class mainly to provide feedback about errors. The errors during parsing
	 * are mainly due to problems with the Internet connection, so if the users click "ok" they
	 * are aware of this fact and will try to solve it
	 * 
	 * More about dialogs here: http://developer.android.com/guide/topics/ui/dialogs.html
	 * 
	 * @param title Title of the dialog window
	 * @param message Message of the dialog
	 * @return alert dialog to be shown
	 */
	private AlertDialog createAlert(String title, String message){
		
		//Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		//Chain together various setter methods to set the dialog characteristics
		builder.setMessage(message);
		builder.setTitle(title);

		//Add the button
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User clicked OK button
		           }
		       });
		// Create the AlertDialog
		return builder.create();
	}
}
