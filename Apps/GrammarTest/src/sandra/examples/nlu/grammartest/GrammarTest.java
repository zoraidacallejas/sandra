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

package sandra.examples.nlu.grammartest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import sandra.libs.nlu.nlulib.GrammarException;
import sandra.libs.nlu.nlulib.NLU;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * GrammarTest: Application to test the possibilities for natural language processing
 * of the <code>NLULib</code>.
 * 
 * The user chooses whether to employ statistical or handcrafted grammars. The handcrafted
 * grammar is in the assets folder, and the statistical processing using the Maluuba service (see chapter 6). 
 * Then he can speak or write a text and the semantic interpretation of the recognized phrase is shown in the GUI.
 * 
 * It uses the <code>ASRLib</code> library for speech recognition, and the <code>NLULib</code> 
 * and <code>XMLLIb</code> for asynchronous access to xml files. 
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 3.0, 08/27/13
 *
 */

public class GrammarTest extends NLU {

	private static final String LOGTAG = "GRAMMARTEST";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grammar_test);
		
		//Initialize the speech recognizer
		createRecognizer(getApplicationContext());
		
		//Initialize the handcrafted grammar
		try {
			initializeHandCrafted(getGrammarContent());
		} catch (XmlPullParserException e) {
			Log.e(LOGTAG, e.getMessage());
		} catch (GrammarException e) {
			e.printStackTrace();
			Log.e(LOGTAG, e.getMessage());
		}
		
		setButtonText();
		setButtonASR();
		indicateNotListening();
	}
	
	/**
	 * Processes the text in the GUI with a handcrafted grammar
	 */
	void processTextWithHandCrafted(){
		try {
			String phrase = ((EditText) findViewById(R.id.phrase)).getText().toString();
			String semantics = getResultsHandCrafted(phrase);
			
			if(semantics!=null) {
	        	((TextView) findViewById(R.id.txtViewResult)).setText(getString(R.string.result_ok)+ "\r\n\r\n" + semantics);
	        	Log.i(LOGTAG, phrase+" IS in the grammar - Semantic: "+semantics);
	        }
	        else {
	        	((TextView) findViewById(R.id.txtViewResult)).setText(R.string.result_bad);
	        	Log.i(LOGTAG, phrase+" IS NOT in the grammar");
	        }
	        
		} catch (GrammarException e) {
			Toast.makeText(this,"Error processing the recognized phrase", Toast.LENGTH_SHORT).show();
			Log.e(LOGTAG,"Error with handcrafted grammar: "+e.getMessage());
		}
		
	}
	
	/**
	 * Processes the text in the GUI with a statistical grammar
	 */
	void processTextWithStatistical(){
		String phrase = ((EditText) findViewById(R.id.phrase)).getText().toString();
		try {
			startStatistical(phrase);
		} catch (GrammarException e) {
			Toast.makeText(this,"Error connecting with the Maluuba service", Toast.LENGTH_SHORT).show();
			Log.e(LOGTAG, "Maluuba could not be used: "+e.getMessage());
		}
	}
	
	/**
	 * Processes the recognition results with a handcrafted grammar
	 */
	void processASRWithHandCrafted(ArrayList<String> nBestList){
		try {
			String guiTxt = "";
			int i=1;
			for(String recognizedPhrase: nBestList){
				String semantics = getResultsHandCrafted(recognizedPhrase);
				if(semantics!=null) {
		        	guiTxt += i+") "+recognizedPhrase+": "+getString(R.string.result_ok)+ "\r\n\r\n" + semantics+ "\r\n";
		        	Log.i(LOGTAG, recognizedPhrase+" IS in the grammar - Semantic: "+semantics);
		        }
		        else {
		        	guiTxt += i+") "+recognizedPhrase+": "+getString(R.string.result_bad)+ "\r\n\r\n";
		        	Log.i(LOGTAG, recognizedPhrase+" IS NOT in the grammar");
		        }
				i++;
			}

			((TextView) findViewById(R.id.txtViewResult)).setText(guiTxt);
			
		} catch (GrammarException e) {
			Toast.makeText(this,"Error processing the recognized phrase", Toast.LENGTH_SHORT).show();
			Log.e(LOGTAG,"Error with handcrafted grammar: "+e.getMessage());
		}	
	}
	
	/**
	 * Processes the recognition results with a statistical grammar
	 * @param nBestList n best recognition results
	 */
	void processASRWithStatistical(ArrayList<String> nBestList){
		for(String recognizedPhrase: nBestList)
			try {
				startStatistical(recognizedPhrase);
			} catch (GrammarException e) {
				Toast.makeText(this,"Error connecting with the Maluuba service", Toast.LENGTH_SHORT).show();
				Log.e(LOGTAG, "Maluuba could not be used: "+e.getMessage());
			}
	}

	/**
	 * Sets the listener for the text button so that speech recognition starts when it is clicked
	 */
	private void setButtonText() {
		Button txt = (Button) findViewById(R.id.txtButton);
		txt.setBackgroundColor(getResources().getColor(R.color.asr_btn_default));	
		txt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					RadioGroup radioG = (RadioGroup) findViewById(R.id.grammar_radioGroup);
				
					switch(radioG.getCheckedRadioButtonId()){
						case R.id.handCrafted_radio:
							processTextWithHandCrafted();
						break;
						
						case R.id.statistical_radio:			
							processTextWithStatistical();
						break;
					}
				}
			});
	}
	
	/**
	 * Sets the listener for the ASR button so that speech recognition starts when it is clicked
	 * When recognition ends, it invokes <code>processAsrError</code> or <code>processAsrResult</code>
	 */
	private void setButtonASR() {
		Button speak = (Button) findViewById(R.id.asrButton);
		indicateNotListening();
		speak.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((EditText) findViewById(R.id.phrase)).setText("");
					try {
						listen(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH, 10); //Start listening
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(),"ASR could not be started: invalid params", Toast.LENGTH_SHORT).show();
						Log.e(LOGTAG,"ASR could not be started");
					}
				}
			});
	}

	/**
	 * Reads the content of the grammar file from the assets folder
	 */
	private String getGrammarContent() {
		
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		
		try {
			AssetManager assetManager = this.getAssets();
			InputStream inputStream = assetManager.open("sample_grammar.xml");	 
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String text=null;
	     
			while ((text = reader.readLine()) != null) {
				contents.append(text).append(System.getProperty(
					"line.separator"));
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
		}}
		
		return contents.toString();
	}

	/**
	 * Shows an ASR error message
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
	        	errorMessage = "No matching phrase";
	        	break;
	        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: 
	        	errorMessage = "No input detected";
	            break;
	        default:
	        	errorMessage = "ASR error";
	        	break;
        }

		indicateNotListening();
		Log.e(LOGTAG, "Error: "+ errorMessage);
		Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
	}

	
	/**
	 * It provides feedback to the user to show that the app is listening changing the color and the message of the speech button
	 */
	private void indicateListening() {
		Button button = (Button) findViewById(R.id.asrButton); //Obtains a reference to the button
		button.setText(getResources().getString(R.string.asr_btn_listening)); //Changes the button's message to the text obtained from the resources folder
		button.setBackgroundColor(getResources().getColor(R.color.asr_btn_listening)); //Changes the button's background to the color obtained from the resources folder
	}
	
	/**
	 * It provides feedback to the user to show that the app is listening changing the color and the message of the speech button
	 */
	private void indicateNotListening(){
		Button button = (Button) findViewById(R.id.asrButton); //Obtains a reference to the button
		button.setText(getResources().getString(R.string.asr_btn_default)); //Changes the button's message to the text obtained from the resources folder
		button.setBackgroundColor(getResources().getColor(R.color.asr_btn_default));	//Changes the button's background to the color obtained from the resources folder		
	}
	
	/**
	 * Changes the appearence of the speech button to show feedback to the user so that they know that the app is listening
	 */
	@Override
	public void processAsrReadyForSpeech() {
		indicateListening();
	}

	/**
	 * Uses a statistical or handcrafted grammar to process the best speech recognition hypothesis
	 */
	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {
		indicateNotListening();
		
		RadioGroup radioG = (RadioGroup) findViewById(R.id.grammar_radioGroup);
		
		switch(radioG.getCheckedRadioButtonId()){
			case R.id.handCrafted_radio:
				processASRWithHandCrafted(nBestList);
			break;
			
			case R.id.statistical_radio:			
				processASRWithStatistical(nBestList);
			break;
		}
	}

	/**
	 * Shows in the GUI the semantic parsing obtained from the statistical grammar
	 */
	@Override
	public void processResultsFromStatistical(String semantics) {
		semantics = semantics.replace(",", ",\r\n");
		((TextView) findViewById(R.id.txtViewResult)).setText(semantics);
    	Log.i(LOGTAG, "Semantics from Maluuba: "+semantics);
	}

	
}
