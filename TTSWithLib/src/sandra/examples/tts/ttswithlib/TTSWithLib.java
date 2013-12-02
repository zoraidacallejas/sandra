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

package sandra.examples.tts.ttswithlib;

import java.util.ArrayList;
import java.util.Locale;

import sandra.libs.tts.TTS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


/**
 * TTSDemo: Basic app with text to speech synthesis using the <code>TTSLib</code>
 * 
 * Simple demo in which the user writes a text in a text field
 * and it is synthesized by the system when pressing a button.
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * 
 * @version 2.6, 11/24/13
 *
 * @see <code>TTSBegin</code> for the equivalent app without the <code>TTSLib</code> library
 * (note: <code>TTSBegin</code> does not allow to set the language).
 */
public class TTSWithLib extends Activity {

	private TTS myTts; 					//Single instance of the TTS for this activity. See TTSLib/TTS.java
	String languageCode;			 	//Language code for locale
	
	String LOGTAG = "TTSWithLib";
	ArrayList<String> locales = new ArrayList<String>();

	/**
	 * Sets up the activity initializing the text to speech engine
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Set layout
		setContentView(R.layout.tts_lib);
		
		//Set up the locales considered and populate the list view
		setLocalesConsidered();
		
		//Set up the speak button
		setButtons();
			
		//Initialize text to speech retrieving the singleton instance (see TTSLib)
	    myTts = TTS.getInstance(this);
	}
	
	/**
	 * Builds an ArrayList with the names of the locales considered. The first one is the device's default locale,
	 * and it also includes EN and ES. This way, the user will be able to choose between a maximum of 3 languages:
	 * the default, English and Spanish, and a minimum of 2 if the default locale is Spanish or English.
	 * These options are presented in the GUI through a <code>ListView</code> comprised of a single-option radio 
	 * button group in which the device's locale is checked by default
	 */
	@SuppressLint("DefaultLocale")
	private void setLocalesConsidered(){

		String defaultLocaleInDevice = Locale.getDefault().getLanguage().toUpperCase();
		locales.add(defaultLocaleInDevice); //First option is the default locale (checked by default in the method setLocaleList)
		
		if(!defaultLocaleInDevice.equals("EN"))
			locales.add("EN");
		
		if(!defaultLocaleInDevice.equals("ES"))
			locales.add("ES");
		
		setLocaleList();
		
	}

	/**
	 * Sets up the locale selection list and a listener for the locale choice
	 */	 
	 private void setLocaleList() {
		// Gets the reference to the locale listview
        final ListView listView = (ListView) findViewById(R.id.locale_listview);
	 
        // Instantiates the array adapter to populate the listView
        // The layout android.R.layout.simple_list_item_single_choice creates a radio button for each item
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, locales);
	    listView.setAdapter(adapter);
	    listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                languageCode = (String) listView.getItemAtPosition(position);
            }
        });
	    
	    //Select the first item by default
	    listView.setItemChecked(0, true); 
	    languageCode = (String) listView.getItemAtPosition(0);
	}

	/**
	 * Sets up the click listener of the buttons
	 */
	private void setButtons(){
		setSpeakButton();
		setStopButton();
	}
	
	/**
	 * Sets up the click listener for the speak button, so that it synthesizes
	 * the text introduced by the user
	 */
	private void setSpeakButton() {
	
		//Reference the speak button
		Button speakButton = (Button) findViewById(R.id.speak_button);
		
		//Set up click listener
		speakButton.setOnClickListener(new OnClickListener() {              
	        @Override 
	        public void onClick(View v) {  
	        	//Get the text typed in by the user
	        	EditText inputText = (EditText) findViewById(R.id.input_text);
	        	String text = inputText.getText().toString(); 

	        	//If there is text, call the method speak() to speak it 
	        	if (text!=null && text.length()>0) 
	        	{
	        		try {
						myTts.speak(text, languageCode); //We will specify language, but not country  
					} catch (Exception e) {
						//An exception is raised when the languageCode cannot be used, and the default locale is selected
						Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
						toast.show();
					} 
	        	}
	        }  
	     }); 		
	}
	
	/**
	 * Sets up the click listener for the stop button, so that it stops 
	 * the synthesizer if it is speaking
	 */
	private void setStopButton() {
		
		//Reference the speak button
		Button resumeButton = (Button) findViewById(R.id.stop_button);

		//Set up click listener
		resumeButton.setOnClickListener(new OnClickListener() {              
	        @Override 
	        public void onClick(View v) {  
	        	//Stop the synthesis
	        	myTts.stop(); 
	        }  
	     }); 

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

