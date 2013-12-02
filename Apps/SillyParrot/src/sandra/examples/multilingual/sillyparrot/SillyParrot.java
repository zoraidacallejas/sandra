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

package sandra.examples.multilingual.sillyparrot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import sandra.libs.asr.asrmultilinguallib.ASR;
import sandra.libs.tts.TTS;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class SillyParrot extends ASR{

	private String selectedLanguage = Locale.getDefault().getDisplayLanguage(); //Selected language is by default the one in the device
	private TTS myTts;  //The TTS cannot be retrieved here because there would be no Context to pass as argument
	HashMap<String, String> locales = new HashMap<String, String>(); //Key =  name of the language, value = locale
	
	private static final String LOGTAG = "SILLYPARROT";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.silly_parrot);
		createRecognizer(this);
		myTts = TTS.getInstance(this);
		setLocalesConsidered();
		setSpeakButton();
	}
	
	
	private void setSpeakButton() {
		
		//Reference the speak button
		Button speakButton = (Button) findViewById(R.id.speakButton);
		
		//Set up click listener
		speakButton.setOnClickListener(new OnClickListener() {              
	        @Override 
	        public void onClick(View v) {  
	        	try {
					listen(new Locale(selectedLanguage), RecognizerIntent.LANGUAGE_MODEL_FREE_FORM, 1);
				} catch (Exception e) {
					//An exception is raised when the languageCode cannot be used, and the default locale is selected
					Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
					toast.show();
				}
	        }  
	     }); 
	}
	
	@SuppressLint("DefaultLocale")
	private void setLocalesConsidered(){
	
		locales.put("English (US)", "en_US");
		locales.put("French (France)", "fr_FR");
		locales.put("Spanish (Spain)", "es_ES");
		locales.put("German (Germany)", "de_DE");
		locales.put("Italian (Italy)", "it_IT");
		
		ArrayList<String> names = new ArrayList<String>(locales.keySet()); //In order to show them in alphabetical order
		Collections.sort(names);
		setLocaleList(names);
		
	}

	/**
	 * Sets up the locale selection list and a listener for the locale choice
	 */	 
	 private void setLocaleList(ArrayList<String> languages) {
		// Gets the reference to the locale listview
        final ListView listView = (ListView) findViewById(R.id.languagesListView);
        
        //Only 1 item can be active at a time
        listView.setChoiceMode(1);
        
	    //Select the first item by default
	    listView.setItemChecked(0, true); 
	    selectedLanguage = locales.get((String) listView.getItemAtPosition(0));
	 
        // Instantiates the array adapter to populate the listView
        // The layout android.R.layout.simple_list_item_single_choice creates a radio button for each item
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, languages);
	    listView.setAdapter(adapter);
	    listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                selectedLanguage = locales.get((String) listView.getItemAtPosition(position));
                listView.setItemChecked(position, true); 
            }
        });

	}

	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {
		
		try {
			myTts.speak(nBestList.get(0), selectedLanguage); //We will specify language, but not country  
		} catch (Exception e) {
			//An exception is raised when the languageCode cannot be used, and the default locale is selected
			Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
			toast.show();
		} 
		
	}

	@Override
	public void processAsrReadyForSpeech() {
		Toast.makeText(this, "Ready to listen!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void processAsrError(int errorCode) {
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
	        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: 
	        	errorMessage = "RecognitionServiceBusy" ; 
	            break;
	        case SpeechRecognizer.ERROR_SERVER: 
	        	errorMessage = "Server sends error status"; 
	            break;
	        case SpeechRecognizer.ERROR_NO_MATCH: 
	        	errorMessage = "No matching phrase";
	        	break;
	        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: 
	        	errorMessage = "No input detected";
	            break;
	        default:
	        	errorMessage = "ASR error";
	        	break;
        }


		Log.e(LOGTAG, "Error: "+ errorMessage);
		Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
	}

}


		
		
		
		