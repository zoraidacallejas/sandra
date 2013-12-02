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

package sandra.examples.tts.ttswithintent;

import java.util.Locale;

import sandra.examples.tss.ttswithintent.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * TTSBegin: Basic app with text to speech synthesis
 * 
 * Simple demo in which the user writes a text in a text field
 * and it is synthesized by the system when pressing a button.
 * 
 * The code for this app is self-contained: it uses an <code>Intent</code> 
 * for text to speech synthesis. The rest of the apps in the book employ a special 
 * <code>TTS</code> library (<code>TTSLib</code>).
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * 
 * @version 1.2, 07/17/13
 *
 * @see <code>TTSDemo</code> for the equivalent app using the <code>TTSLib</code> library
 */
public class TTSWithIntent extends Activity {

	private int TTS_DATA_CHECK = 12; 	// It is an integer value to be used as a checksum
	
	private TextToSpeech tts = null;
	
	private EditText inputText;
	private Button speakButton;

	/**
	 * Sets up the activity initializing the text to speech engine
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tts_intent);
			
		// Set up the speak button
		setButton();
			
		// Invoke the method to initialize text to speech
	    initTTS();
		
	    // Reference the edit text field
		inputText = (EditText) findViewById(R.id.input_text);
	}
	
	/**
	 * Sets up the listener for the button that the user
	 * must click to hear the obtained the synthesized message 
	 */
	private void setButton() {
		// Reference the speak button
		speakButton = (Button) findViewById(R.id.speak_button);

		// Set up click listener
		speakButton.setOnClickListener(new OnClickListener() {              
	        @Override 
	        public void onClick(View v) {  
	        	// Get the text typed in by the user
	        	String text = inputText.getText().toString(); 

	        	// If there is text, call the method speak() to speak it 
	        	if (text!=null && text.length()>0) {  
	        		tts.speak(text, TextToSpeech.QUEUE_ADD, null);  
	        	}  
	        	
	        }  
	    });

	}
	
	/**
	 * Initializes the text to speech engine
	 */
	private void initTTS() {
		//Disable speak button during the initialization of the text to speech engine
		disableSpeakButton();
		
		//Check if a the engine is installed, when the check is finished, the 
		//onActivityResult method is executed
		Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);  
		startActivityForResult(checkIntent, TTS_DATA_CHECK); 		
	}

	/**
	 * Callback from check for text to speech engine installed
	 *  If positive, then creates a new <code>TextToSpeech</code> instance which will be called when user 
	 *  clicks on the 'Speak' button
	 *  If negative, creates an intent to install a <code>TextToSpeech</code> engine
	*/	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		
		// Check that requestCode matches the checksum 
		if (requestCode == TTS_DATA_CHECK) { 

			// Check that the resultCode is CHECK_VOICE_DATA_PASS  
			//(it was the TTS which result is being processed and not any other activity)
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {  
				
				// Create a TextToSpeech instance  
				tts = new TextToSpeech(this, new OnInitListener() {
					public void onInit(int status) {
						if (status == TextToSpeech.SUCCESS) {
							// Display Toast				
							Toast.makeText(TTSWithIntent.this, "TTS initialized", Toast.LENGTH_LONG).show(); 
	
							// Set language to US English if it is available
							if (tts.isLanguageAvailable(Locale.US) >= 0)					
								tts.setLanguage(Locale.US);
						}
						enableSpeakButton();
					}
				}); 
			} else {  
				// Install missing data  
				PackageManager pm = getPackageManager();
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				ResolveInfo resolveInfo = pm.resolveActivity( installIntent, PackageManager.MATCH_DEFAULT_ONLY );

				if( resolveInfo == null ) {
					Toast.makeText(TTSWithIntent.this, "There is no TTS installed, please download it from Google Play", Toast.LENGTH_LONG).show(); 
				} else {
				   startActivity( installIntent );
				}
			}  
		}  
	}  

	/**
	 * Disables the speak button so that the user cannot click it while a message
	 * is being synthesized
	*/	
	private void disableSpeakButton() 
	{
		speakButton.setEnabled(false);
	}  		
	
	/**
	 * Enables the speak button so that the user can click on it to hear
	 * the synthesized message
	*/
	private void enableSpeakButton() 
	{
		speakButton.setEnabled(true);
	}

	/**
	 * Shuts down the TTS when finished
	*/	     
	@Override
	public void onDestroy() {
		if (tts != null) {
	       tts.stop();
	       tts.shutdown();
	    }
	    super.onDestroy();
	}

}

