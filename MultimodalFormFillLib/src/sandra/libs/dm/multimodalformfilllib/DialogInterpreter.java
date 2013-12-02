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

package sandra.libs.dm.multimodalformfilllib;

import java.util.ArrayList;
import java.util.HashMap;

import sandra.libs.asr.asrlib.ASR;
import sandra.libs.tts.TTS;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

/**
* Dialog manager that follows a form-filling approach and synchronizes the values recognized
* with the corresponding elements in the GUI
* 
* It is a version of the class <code>sandra.libs.dm.formfilllib.DialogInterpreter</code> (chapter 5, FormFillLib project).
* Changes: methods <code>moveToNextField</code> and <code>processAsrResults</code>
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
		//Initialize text to speech
		myTts = TTS.getInstance(getApplicationContext());
		
		//Initialize the speech recognizer
		createRecognizer(getApplicationContext());	

	}
	
	
	/**
	 * Starts interpreting the dialog
	 * @param form Form containing the results of parsing a VXML file into Java objects
	 * @throws MultimodalException When the form is null (probably because the parseVXML method was not invoked before start interpreting)
	 */
	public void startInterpreting(Form form) throws MultimodalException{
		if(form!=null){
			this.form=form;				//Dialog to be interpreted
			currentPosition=0;			//Initial field is in position 0
			interpretCurrentField();	//START INTERPRETING...
		} else {
			throw new MultimodalException("The oral form could not be interpreted", "Check that the form was parsed before invoking the interpretation");
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
	 *  
	 * CHANGED WITH RESPECT TO FORMFILLLIB (chapter 5)
	 * Changes:
	 * 	- Computation of next field is circular: after the last one, it comes the first one.
	 *  - endOfDialog = true when all fields are filled
	 */
	private void moveToNextField(){
		
		//The position of the field to be interpreted (currentPosition) is moved forward
		//until either the field is not filled or there are no more fields to visit (endOfDialog is true)
		Boolean endOfDialog = false;
		
		while(form.getField(currentPosition).isFilled() && !endOfDialog){
			
			currentPosition = (currentPosition+1)%form.numberOfFields();
			
			if(form.allFieldsFilled()){
				endOfDialog=true;
				currentPosition=0;
			}				
		}
		
		//If the end of the dialogue is reached, the results are processed
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
	 * Synthesizes a prompt. If it is not possible, it creates a toast with the content of the message
	 */
	private void playPrompt(String prompt) {
		try {
			myTts.speak(prompt, "EN");
		} catch (Exception e) {
			Toast.makeText(this, prompt, Toast.LENGTH_LONG).show();
			Log.e(LOGTAG, "English not available for TTS, default language used instead");
		}
		
	}

	/**
	 * Processes the results of ASR when recognition is successful
	 * 
	 * CHANGED WITH RESPECT TO FORMFILLLIB (chapter 5)
	 * Changes:
	 * 	- If any of the recognized values is valid for the field (according to the grammar), it is saved as the current value, and it is synchronized with the GUI
	 * 	- If not, the field is interpreted again
	 * 
	 */
	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {

		Field currentField = form.getField(currentPosition);
		
		int i=0;
		Boolean validValue = false;
		
		//Search in the n best ASR recognition results for a valid value according to the field's grammar
		while(i<nBestList.size() && !validValue){
			validValue = currentField.isvalid(nBestList.get(i));
			i++;
		}

		//If there is a valid value...
		if(validValue) {
			currentField.setValue(nBestList.get(i-1)); //... it is set as the current value for the field
			result.put(currentField.getName(), nBestList.get(i-1));
			try {
				oralToGui(currentField);			//and it is synchronized with the GUI
			} catch (MultimodalException e) {
				Toast.makeText(this, e.getReason(), Toast.LENGTH_LONG).show();
				Log.e(LOGTAG, e.getReason());
			}
			moveToNextField();
		}
		//If not...
		else {
			interpretCurrentField(); //... the field is interpreted again
		}
	}
	
	/**
	 * Provides feedback to the user (by means of a Toast and a synthesized message) when the ASR encounters an error
	 * 
	 * If the error is a nomatch or no input, then interprets the field again (asks the user again for the same information)
	 * If not, the dialog is stopped.
	 * 
	 * CHANGED WITH RESPECT TO FORMFILLLIB (chapter 5)
	 * Changes:
	 * 	- before starting to interpret the current field again, it checks whether it has been filled in the GUI
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
			//If there is a nomatch or noinput, interprets the field again.
			if(form.getField(currentPosition).isFilled())
				moveToNextField(); //Necessary for multimodal apps
									//in which the field can be filled in the GUI while the oral dialog tries to prompt again for the information
			else
				interpretCurrentField();
		}
		else{
			//If there is an error, shows feedback to the user and writes it in the log
	        Log.e(LOGTAG, "Error: "+ errorMessage);
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
		}
	}
	
	
	//TODO
	public void clearAllFields(){
		for(int i=0; i<form.numberOfFields(); i++)
			form.getField(i).setValue(null);
	} 
	
	//TODO
	public void shutdownTts(){
		myTts.shutdown();
	}

	@Override
	public void processAsrReadyForSpeech() { }

	public abstract void processDialogResults(HashMap<String, String> result);
	public abstract void oralToGui(Field field) throws MultimodalException;
}
