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

package sandra.examples.vpa.derek;

import java.util.ArrayList;

import sandra.libs.asr.asrlib.ASR;
import sandra.libs.tts.TTS;
import sandra.libs.vpa.vpalib.Bot;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


/**
 * DEREK: Derek is a specialized VPA which knowledge base is encoded in AIML as a set of question-response pairs, similar to FAQs. 
 * It is able to respond to a rudimentary set of questions in AIML about Type 2 diabetes. Derek can answer questions about topics such as 
 * symptoms, causes, treatment, risks to children, and complications.
 * 
 * See Derek in Pandorabots: http://www.pandorabots.com/pandora/talk?botid=a80ce25abe344199
 * 
 * It uses the <code>ASRLib</code> and <code>TTSLib</code> libraries for speech recognition and synthesis, 
 * and <code>XMLLIb</code> for asynchronous access to xml files. 
 * 
 * @author Michael McTear
 * @author Zoraida Callejas
 * @version 2.0, 09/10/13
 *
 */
public class Derek extends ASR {
	private static final String LOGTAG = "JACK";
	private static final String BOTID = "f53ee2647e3443ef";
	
	private TTS myTts;
	private Button speakButton;
	
	private Bot bot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//Initialize GUI elements
		setSpeakButton();
		
		//Initialize the speech recognizer
		createRecognizer(getApplicationContext());	
				
		//Initialize text to speech
		 myTts = TTS.getInstance(this);
		
		 //Create bot
		 bot = new Bot(this, BOTID, myTts, "type 2 diabetes");
	
	}

	private void setSpeakButton() {
		speakButton = (Button) findViewById(R.id.speech_btn);
		speakButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					listen(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH, 1);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),"ASR could not be started: invalid params", Toast.LENGTH_SHORT).show();
					Log.e(LOGTAG, e.getMessage());
				} 
			}
		});
	}
	
	/**
	 * Provides feedback to the user when the ASR encounters an error
	 */
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
	        	errorMessage = "No matching message" ;
	        	break;
	        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: 
	        	errorMessage = "Input not audible";
	            break;
	        default:
	        	errorMessage = "ASR error";
	        	break;
        }

		try {
			myTts.speak(errorMessage,"EN");
		} catch (Exception e) {
			Log.e(LOGTAG, "English not available for TTS, default language used instead");
		}
		
			//If there is an error, shows feedback to the user and writes it in the log
	        Log.e(LOGTAG, "Error: "+ errorMessage);
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
	}

	@Override
	public void processAsrReadyForSpeech() {
		Toast.makeText(this, "I'm listening", Toast.LENGTH_LONG).show();
		
	}

	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] confidences) {
		String bestResult = nBestList.get(0);
		Log.d(LOGTAG, "Speech input: " + bestResult);
		// insert %20 for spaces in query
		bestResult = bestResult.replaceAll(" ", "%20");
		bot.initiateQuery(bestResult);
		
	}

	// Shut down TTS engine when finished
	@Override
	public void onDestroy() {
		myTts.shutdown();
		super.onDestroy();
	}
	
	// pressing back button doesn't go back to main activity, goes to home on mobile
	@Override
    public void onBackPressed() {
    	super.onBackPressed();
    	Intent intent=new Intent(getBaseContext(),Derek.class);       	
    	startActivity(intent);
    } 
}
