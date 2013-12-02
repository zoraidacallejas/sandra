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

package sandra.examples.asr.asrwithlib;

import java.util.ArrayList;

import sandra.libs.asr.asrlib.ASR;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * ASRWithLib: Basic app with ASR using the <code>ASRLib</code>
 * 
 * Simple demo in which the user speaks and the recognition results
 * are showed in a list along with their confidence values
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 2.5, 11/24/13
 *
 */
public class ASRWithLib extends ASR {

	// Default values for the language model and maximum number of recognition results
	// They are shown in the GUI when the app starts, and they are used when the user selection is not valid
	private final static int DEFAULT_NUMBER_RESULTS = 10;
	private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM; 
	
	// Attributes
	private int numberRecoResults = DEFAULT_NUMBER_RESULTS; 
	private String languageModel = DEFAULT_LANG_MODEL; 
	
	private static final String LOGTAG = "ASRDEMO";
	
	/**
	 * Sets up the activity initializing the GUI
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.asrlib);
		
		showDefaultValues(); //Shows in the GUI the default values for the language model and the maximum number of recognition results
		setSpeakButton();
		createRecognizer(getApplicationContext());
	}
	
	/**
	 * Shows in the GUI the default values for the language model (checks radio button)
	 * and the maximum number of recognition results (shows the number in the text field)
	 */
	private void showDefaultValues(){
		//Show the default number of results in the corresponding EditText
		((EditText) findViewById(R.id.numResults_editText)).setText(""+DEFAULT_NUMBER_RESULTS);

		//Show the default number of 
		if(DEFAULT_LANG_MODEL.equals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM))
			((RadioButton) findViewById(R.id.langModelFree_radio)).setChecked(true);
		else
			((RadioButton) findViewById(R.id.langModelFree_radio)).setChecked(true);
	}
	
	/**
	 * Reads the values for the language model and the maximum number of recognition results
	 * from the GUI
	 */
	private void setRecognitionParams(){
		String numResults = ((EditText) findViewById(R.id.numResults_editText)).getText().toString();
		
		//Converts String into int, if it is not possible, it uses the default value
		try{
			numberRecoResults = Integer.parseInt(numResults);
		} catch(Exception e) {	
			numberRecoResults = DEFAULT_NUMBER_RESULTS;	
		}
		//If the number is <= 0, it uses the default value
		if(numberRecoResults<=0)
			numberRecoResults = DEFAULT_NUMBER_RESULTS;
		
		
		RadioGroup radioG = (RadioGroup) findViewById(R.id.langModel_radioGroup);
		switch(radioG.getCheckedRadioButtonId()){
			case R.id.langModelFree_radio:
				languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
				break;
			case R.id.langModelWeb_radio:
				languageModel = RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH;
				break;
			default:
				languageModel = DEFAULT_LANG_MODEL;
				break;
		}
	}
	
	/**
	 * Sets up the listener for the button that the user
	 * must click to start talking
	 */
	@SuppressLint("DefaultLocale")
	private void setSpeakButton() {
		//Gain reference to speak button
		Button speak = (Button) findViewById(R.id.speech_btn);
		
		changeButtonAppearanceToDefault();
		
		//Set up click listener
		speak.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Speech recognition does not currently work on simulated devices,
					//it the user is attempting to run the app in a simulated device
					//they will get a Toast
					if("generic".equals(Build.BRAND.toLowerCase())){
						Toast toast = Toast.makeText(getApplicationContext(),"ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
						toast.show();
						Log.d(LOGTAG, "ASR attempt on virtual device");						
					} else{
						try {
							setRecognitionParams(); //Read ASR parameters
							listen(languageModel, numberRecoResults); //Start listening
						} catch (Exception e) {
							Toast toast = Toast.makeText(getApplicationContext(),"ASR could not be started: invalid params", Toast.LENGTH_SHORT);
							toast.show();
							Log.e(LOGTAG, "ASR could not be started: invalid params");
						}
					}
				}
			});
	}

	/**
	 *  Shows the formatted best of N best recognition results (N-best list) from
	 *  best to worst in the <code>ListView</code>. 
	 *  For each match, it will render the recognized phrase and the confidence with 
	 *  which it was recognized.
	 *  
	 *  @param nBestList	    list of matches
	 *  @param nBestConfidence	confidence values (from 0 = worst, to 1 = best) for each match
	 */
	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {
		changeButtonAppearanceToDefault(); //Button has its default appearance (so that the user knows the app is not listening anymore)
		
		//Creates a collection of strings, each one with a recognition result and its confidence, e.g. "Phrase matched (conf: 0.5)"
		ArrayList<String> nBestView = new ArrayList<String>();
		
		if(nBestList!=null){
			for(int i=0; i<nBestList.size(); i++){
				if(nBestConfidences!=null){
					if(nBestConfidences[i]>=0)
						nBestView.add(nBestList.get(i) + " (conf: " + String.format("%.2f", nBestConfidences[i]) + ")");
					else
						nBestView.add(nBestList.get(i) + " (no confidence value available)");
				}
			}
		}
		
		//Includes the collection in the ListView of the GUI
		setListView(nBestView);
		
		//Adds information to log
		Log.d(LOGTAG, "There were : "+ nBestView.size()+" recognition results");
	}
	
	/**
	 * Includes the recognition results in the list view
	 * @param nBestView list of matches
	 */
	private void setListView(ArrayList<String> nBestView){
		
		// Instantiates the array adapter to populate the listView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nBestView);
        ListView listView = (ListView) findViewById(R.id.nbest_listview);
        listView.setAdapter(adapter);
	}

	/**
	 *  When the ASR is ready to listen, this method changes the appearance of the GUI button so that the user receives feedback
	 */
	@Override
	public void processAsrReadyForSpeech() {
		changeButtonAppearanceToListen();		
	}

	/**
	 * Changes the background color and text of the speech button to show that the app is listening
	 */
	private void changeButtonAppearanceToListen() {
		Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
		button.setEnabled(false); //Deactivates the button so that the user cannot press it while the app is recognizing
		button.setText(getResources().getString(R.string.speechbtn_listening)); //Changes the button's message to the text obtained from the resources folder
		button.setBackgroundColor(getResources().getColor(R.color.speechbtn_listening)); //Changes the button's background to the color obtained from the resources folder
	}
	
	/**
	 * Changes the background color and text of the speech button to show that the app is not listening
	 */
	private void changeButtonAppearanceToDefault() {
		Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
		button.setEnabled(true); //Deactivates the button so that the user cannot press it while the app is recognizing
		button.setText(getResources().getString(R.string.speechbtn_default)); //Changes the button's message to the text obtained from the resources folder
		button.setBackgroundColor(getResources().getColor(R.color.speechbtn_default));	//Changes the button's background to the color obtained from the resources folder
	}
	
	/**
	 * Provides feedback to the user (by means of a Toast) when the ASR encounters an error
	 */
	@Override
	public void processAsrError(int errorCode){
			
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
		Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(LOGTAG, "Error when attempting listen: "+ errorMessage);
		
	}
}