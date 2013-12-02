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

package sandra.examples.multilingual.parrot;

import java.util.ArrayList;
import java.util.Locale;

import sandra.libs.asr.asrmultilinguallib.ASR;
import sandra.libs.tts.TTS;

import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * Parrot: App that synthesizes back what the user says
 * 
 * It adapts to the language in the device for the oral interaction
 * and the GUI. It considers Spanish, French, Canadian English and General English (by default)
 * 
 * It uses the <code>ASRMultiligualLib</code> and <code>TTSLib</code> libraries for speech 
 * recognition and synthesis.
 * 
 * To automatically adapt to the devices' locale, its uses different string.xml file
 * in localized values folders (see "res" folder) -> See Chapter 7
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 3.0, 08/18/13
 *
 */
public class Parrot extends ASR{

	private static final String LOGTAG = "PARROT";
	
	private String selectedLanguage = Locale.getDefault().getDisplayLanguage(); //Selected language is by default the one in the device
	private TTS myTts; //The TTS cannot be retrieved here because there would be no Context to pass as argument
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parrot);
		createRecognizer(this);
		myTts = TTS.getInstance(this);
		setSpeakButton();
	}
	
	/**
	 * Sets a listener that starts ASR.
	 */
	private void setSpeakButton() {
		
		//Reference the speak button
		Button speakButton = (Button) findViewById(R.id.speakButton);
		
		//Set up click listener
		speakButton.setOnClickListener(new OnClickListener() {              
	        @Override 
	        public void onClick(View v) {  
	        	try {
	        		selectedLanguage = Locale.getDefault().getDisplayLanguage();
					listen(new Locale(selectedLanguage), RecognizerIntent.LANGUAGE_MODEL_FREE_FORM, 1);
				} catch (Exception e) {
					//An exception is raised when the languageCode cannot be used, and the default locale is selected
					Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
					toast.show();
				}
	        }  
	     }); 
	}

	/**
	 * When ASR is succesful, then the best recognition result is synthesized.
	 */
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
