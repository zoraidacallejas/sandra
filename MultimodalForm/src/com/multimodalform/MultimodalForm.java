package com.multimodalform;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import org.xmlpull.v1.XmlPullParserException;

import com.formfilllib.Form;
import com.formfilllib.MultimodalDialogInterpreter;
import com.multimodalform.R;
import com.formfilllib.VXMLParser;
import com.xmllib.AsyncResponse;
import com.xmllib.RetrieveXMLTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MultimodalForm extends MultimodalDialogInterpreter implements AsyncResponse{

	private static final String LOGTAG = "MULTIMODALFORM";
	
	//URL with the vxml file that contains the structure of the dialog
	private static final String URL_VXML = "http://www.lab.inf.uc3m.es/~dgriol/musicbrain.vxml";
	private static final String DEFAULT_URL_VXML = "http://www.lab.inf.uc3m.es/~dgriol/musicbrain.vxml";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multimodal_form);	
		setRestartButton();
		
		//Start the interpretation of the VXML file
		startDialog();
	}
	
	
	/**
	 * Initializes the restart button and its listener. When the button is pressed, the activity is 
	 * started from the scratch. It is useful to carry out another interaction after a previous
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
	 * Starts retrieving the dialog information, when it is ready, the processFinish method is invoked,
	 * which parses and interprets the dialog (see processFinish)
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
	 * Reads the contents of an XML file in the specified url. If the first url (url) is not accessible,
	 * the second url (url_default) is used.
	 */
	public void retrieveXML(String url, String url_default){
		RetrieveXMLTask retrieveXML = new RetrieveXMLTask();	//AsyncTask to retrieve the contents of the XML file fro mthe URL
		retrieveXML.delegate = this;	//It is crucial in order to retrieve the data from the asyncrhonous task (see the AsyncResponse and RetrieveXMLTask classes)

		
		/*
		 * The string corresponding to the XML file is retrieved with an asynchronous task, and thus
		 * is executed in the background. When this process is finished, the "processFinish" method
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
	 * It is invoked when the VXML file has been read from the URL.
	 * It parses and interprets the VXML code received in the input string
	 */
	@Override
	public void processFinish(String xmlContent) {
		
		if(!xmlContent.contains("musicbrainz")){
			Form form;
			
				
				try {
					form = VXMLParser.parseVXML(xmlContent);
					startInterpreting(form);
				} catch (XmlPullParserException ex) {
					Log.e(LOGTAG, "Error parsing the VXML file: "+ex.getMessage());
					createAlert("Parsing error", "Please check your Internet connection").show();
				} catch (MultimodalException ex) {
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
	 * (see the explanation in MusicBrainParser.java). These albums contain the word uttered by the user
	 * in their title. This collection is filtered to keep only the albums released between the dates 
	 * indicated by the user. The filtered collection is shown in the listView of the GUI  
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
	 * the word specified by the user in their title), it keeps the ones that were release
	 * between the dates indicated by the user
	 * 
	 * If the years recognized are not valid (are not in the form YYYY, e.g. 1997), then the
	 * albums are not filtered
	 */
	private TreeSet<Album> filterAlbums(TreeSet<Album> albumList){
		
		TreeSet<Album> filteredList = new TreeSet<Album>(new AlbumComparator());
		
		try{
			SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.ENGLISH);
			Date initialDate = format.parse(albumData.get("initialyear"));
			Date finalDate = format.parse(albumData.get("finalyear"));
		
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

	//TODO: See why this appeared in the code
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.form_fill_music_brain, menu);
		return true;
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

