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

package sandra.libs.dm.formfilllib;

import java.util.ArrayList;
import java.util.HashMap;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import sandra.libs.asr.asrlib.ASR;
import sandra.libs.tts.TTS;

/**
* Dialog manager that follows a form-filling approach
* 
* @author Zoraida Callejas
* @author Michael McTear
* @version 1.4, 08/18/13
*/
public abstract class DialogInterpreter extends ASR{
	
	private TTS myTts;	//TTS engine
	
	private Form form;	//Contains the structure of the dialog to be interpreted
	
	private int currentPosition=0;	//Position of the field to be interpreted in the list of fields of the form (0 to form size-1)
	
	//Prompts in response to nomatch (user says something but the system does not understands) and noinput (the user does not say anything) events
	private String nomatch = "Sorry, I did not understand";
	private String noinput = "Sorry, I did not hear you";
	
	//Results of the interpretation of the dialog in the form of pairs <name of the field, value recognized>
	private HashMap<String, String> result = new HashMap<String, String>();
	
	private static final String LOGTAG = "DIALOGINTERPRETER";
	
	
	/**
	 * Initializes the ASR and TTS engines.
	 * @Note: It is not possible to do it in a constructor because we cannot get the context for recognition before oncreate
	 */
	public void initializeAsrTts(){
		//Initialize the speech recognizer
		createRecognizer(getApplicationContext());	
				
		//Initialize text to speech
		 myTts = TTS.getInstance(this);
	}
	
	
	/**
	 * Starts interpreting the dialog
	 * @param form Form containing the results of parsing a VXML file into Java objects
	 * @throws MultimodalException When the form is null (probably because the parseVXML method was not invoked before start interpreting)
	 */
	public void startInterpreting(Form form) throws FormFillLibException{
		if(form!=null){
			this.form=form;				//Dialog to be interpreted
			currentPosition=0;			//Initial field is in position 0
			interpretCurrentField();	//START INTERPRETING...
		} else {
			throw new FormFillLibException("The oral form could not be interpreted", "Check that the form was parsed before invoking the interpretation");
		}		
	}
	
	/**
	 * Interprets the current field:
	 * 	- Prompts the user for the information (e.g. "what is your destination?")
	 *  - Listens for the response
	 */
	private void interpretCurrentField(){	
		Field currentField = form.getField(currentPosition);
		playPrompt(currentField.getPrompt());
		listen();
	}
	
	/**
	 * Starts interpretation of the next field:
	 * 	- It computes which is the next field to be interpreted
	 *  - It starts its interpretation (see interpretCurrentField)
	 */
	private void moveToNextField(){
		
		//The position of the field to be interpreted (currentPosition) is moved forward
		//until either the field is not filled or there are no more fields to visit (endOfDialog is true)
		Boolean endOfDialog = false;
		
		while(form.getField(currentPosition).isFilled() && !endOfDialog){
			
			currentPosition++;
			
			if(currentPosition==form.numberOfFields()){
				endOfDialog=true;
				currentPosition=0;
			}			
		}
		
		//If the end of the dialog is reached, the results are processed
		if(endOfDialog){
			processDialogResults(result);
		}
		else //If not, then it interprets the next field
			interpretCurrentField();
	}
	

	/**
	 * ASR listens for the user response to a prompt
	 */
	private void listen(){
		
		try {
			listen(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH, 10); //Start listening
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),"ASR could not be started: invalid params", Toast.LENGTH_SHORT).show();
			Log.e(LOGTAG, e.getMessage());
		}	
	}

	/**
	 * Synthesizes a prompt
	 */
	private void playPrompt(String prompt) {
		try {
			myTts.speak(prompt,"EN");
		} catch (Exception e) {
			Log.e(LOGTAG, "English not available for TTS, default language used instead");
		}
		
	}

	/**
	 * Processes the results of ASR when recognition is successful
	 */
	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {

		Field currentField = form.getField(currentPosition);
		String value = nBestList.get(0); //Other results from the nBestList could be used as well

		currentField.setValue(value);
		result.put(currentField.getName(), value);
		moveToNextField();	
	}
	
	/**
	 * Provides feedback to the user (by means of a Toast and a synthesized message) when the ASR encounters an error
	 * 
	 * If the error is a nomatch or no input, then interprets the field again (asks the user again for the same information)
	 * If not, the dialog is stopped.
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
	        	errorMessage = nomatch ;  //Error message obtained from the VXML file
	        	break;
	        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: 
	        	errorMessage = noinput ;  //Error message obtained from the VXML file
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
		
		
		if(errorCode==SpeechRecognizer.ERROR_NO_MATCH || errorCode==SpeechRecognizer.ERROR_SPEECH_TIMEOUT){
			//If there is a nomatch or noinput, interprets the field again
			interpretCurrentField();
		}
		else{
			//If there is an error, shows feedback to the user and writes it in the log
	        Log.e(LOGTAG, "Error: "+ errorMessage);
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * Shuts down the TTS engine when finished
	 */   
	@Override
	public void onDestroy() {
		super.onDestroy();
		myTts.shutdown();
	}

	@Override
	public void processAsrReadyForSpeech() { }

	/**
	 * Abstract method to be implemented in the class that uses the <code>FormFillLib</code> in order to
	 * process the results of the dialog
	 * 
	 * @param result Pairs <name of the field, value recognized> for each field in the form
	 */
	public abstract void processDialogResults(HashMap<String, String> result);
}
