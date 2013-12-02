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

package sandra.examples.oneshot.voicesearch;

import java.util.ArrayList;
import java.util.Locale;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import sandra.libs.tts.TTS;
import sandra.libs.asr.asrlib.ASR;
import sandra.examples.oneshot.voicesearch.R;

/**
 * VoiceSearch: initiates a search query based on the words spoken by the user. 
 * 
 * It uses the <code>ASRLib</code> and <code>TTSLib</code> libraries for speech recognition.
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 2.5, 11/24/13
 *
 */
public class VoiceSearch extends ASR {

    private static final String LOGTAG = "VOICESEARCH";
    private TTS myTts; 	
	
	/**
	 * Sets up the activity initializing the GUI, the ASR and TTS
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Set layout
		setContentView(R.layout.voicesearch);
		
		//Set up the speech button
		setSpeakButton();
		
		//Initialize the speech recognizer
		createRecognizer(getApplicationContext());	
		
		//Initialize text to speech
	    myTts = TTS.getInstance(this);
	}

	/**
	 * Initializes the search button and its listener. When the button is pressed, a feedback is shown to the user
	 * and the recognition starts
	 */
	private void setSpeakButton() {
		// gain reference to speak button
		Button speak = (Button) findViewById(R.id.speech_btn);
		speak.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//If the user is running the app on a virtual device, they get a Toast
					if("generic".equals(Build.BRAND.toLowerCase(Locale.US))){
						Toast toast = Toast.makeText(getApplicationContext(),"ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
						toast.show();
						Log.e(LOGTAG, "ASR attempt on virtual device");						
					}
					else {
						startListening();
					}
				}
			});
	}
	
	/**
	 * Starts listening for any user input.
	 * When it recognizes something, the <code>processAsrResult</code> method is invoked. 
	 * If there is any error, the <code>processAsrError</code> method is invoked.
	 */
	private void startListening(){
		
		if(deviceConnectedToInternet()){
			try {
				//Show a feedback to the user indicating that the app has started to listen
				indicateListening();
				
				/*Start listening, with the following default parameters:
					* Recognition model = Free form, 
					* Number of results = 1 (we will use the best result to perform the search)
					*/
				listen(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM, 1); //Start listening
			} catch (Exception e) {
				Toast toast = Toast.makeText(getApplicationContext(),"ASR could not be started: invalid params", Toast.LENGTH_SHORT);
				toast.show();
				Log.e(LOGTAG, e.getMessage());
			}	
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),"Please check your Internet connection", Toast.LENGTH_SHORT);
			toast.show();
			Log.e(LOGTAG, "Device not connected to Internet");	
		}
	}

	/**
	 * Provides feedback to the user to show that the app is listening:
	 * 		* It changes the color and the message of the speech button
	 *      * It synthesizes a voice message
	 */
	private void indicateListening() {
		Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
		button.setText(getResources().getString(R.string.speechbtn_listening)); //Changes the button's message to the text obtained from the resources folder
		button.setBackgroundColor(getResources().getColor(R.color.speechbtn_listening)); //Changes the button's background to the color obtained from the resources folder
		myTts.speak(getResources().getString(R.string.initial_prompt)); 
	}
	
	/**
	 * Provides feedback to the user to show that the app is performing a search:
	 * 		* It changes the color and the message of the speech button
	 *      * It synthesizes a voice message
	 */
	private void indicateSearch(String criteria) {
		changeButtonAppearanceToDefault();
		myTts.speak(getResources().getString(R.string.searching_prompt)+criteria); 
	}
	
	/**
	 * Provides feedback to the user to show that the app is idle:
	 * 		* It changes the color and the message of the speech button
	 */	
	private void changeButtonAppearanceToDefault(){
		Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
		button.setText(getResources().getString(R.string.speechbtn_default)); //Changes the button's message to the text obtained from the resources folder
		button.setBackgroundColor(getResources().getColor(R.color.speechbtn_default));	//Changes the button's background to the color obtained from the resources folder		
	}
	
	/**
	 * Provides feedback to the user (by means of a Toast and a synthesized message) when the ASR encounters an error
	 */
	@Override
	public void processAsrError(int errorCode) {
	
		changeButtonAppearanceToDefault();
		
		String errorMessage;
		switch (errorCode) 
        {
	        case SpeechRecognizer.ERROR_AUDIO: 
	        	errorMessage = "Audio recording error"; 
	            break;
	        case SpeechRecognizer.ERROR_CLIENT: 
	        	errorMessage = "Client side error"; 
	            break;
	        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: 
	        	errorMessage = "Insufficient permissions" ; 
	            break;
	        case SpeechRecognizer.ERROR_NETWORK: 
	        	errorMessage = "Network related error" ;
	            break;
	        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:                
	            errorMessage = "Network operation timeout"; 
	            break;
	        case SpeechRecognizer.ERROR_NO_MATCH: 
	        	errorMessage = "No recognition result matched" ; 
	        	break;
	        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: 
	        	errorMessage = "RecognitionServiceBusy" ; 
	            break;
	        case SpeechRecognizer.ERROR_SERVER: 
	        	errorMessage = "Server sends error status"; 
	            break;
	        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: 
	        	errorMessage = "No speech input" ; 
	            break;
	        default:
	        	errorMessage = "ASR error";
	        	break;
        }
		
        Log.e(LOGTAG, "Error when attempting to listen: "+ errorMessage);
		Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
		
		try {
			myTts.speak(errorMessage,"EN");
		} catch (Exception e) {
			Log.e(LOGTAG, "English not available for TTS, default language used instead");
		}

	}

	@Override
	public void processAsrReadyForSpeech() { }

	/**
	 * Initiates a Google search intent with the results of the recognition
	 */
	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {
		
		if(nBestList!=null){
			if(nBestList.size()>0){
				String bestResult = nBestList.get(0); //We will use the best result
				indicateSearch(bestResult); //Provides feedback to the user that search is going to be started
				googleText(bestResult);
			}
		}
	}
	
	/**
	 * Starts a google query with the text
	 * @param criterion text to be used as search criterion
	 */
	public void googleText(String criterion)
	{
		if(deviceConnectedToInternet())
		{
			//Carries out a web search with the words recognized				
			PackageManager pm = getPackageManager();
			Intent intent = new Intent();
			intent.putExtra(SearchManager.QUERY, criterion);
			intent.setAction(Intent.ACTION_WEB_SEARCH);
			ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY );
	
			if( resolveInfo == null )
				Log.e(LOGTAG, "Not possible to carry out ACTION_WEB_SEARCH Intent");	
		}
		else {
			Toast.makeText(getApplicationContext(),"Please check your Internet connection", Toast.LENGTH_LONG).show(); //Not possible to carry out the intent
			Log.e(LOGTAG, "Device not connected to Internet");	
		}
	}
	
	/**
	 * Checks whether the device is connected to Internet (returns true) or not (returns false)
	 * From: http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
	 * @return
	 */
	public boolean deviceConnectedToInternet() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
	    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
	}
	
	/**
	 * Shuts down the TTS engine when finished
	 */   
	@Override
	public void onDestroy() {
		super.onDestroy();
		myTts.shutdown();
	}
} 
