package sandra.libs.asr.asrlib;
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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * 
 * Abstract class for automatic speech recognition
 * 
 * Encapsulates the management of the speech recognition engine.
 * 
 * All the methods necessary to set up and run the speech recognizer are implemented in this class.
 * Only the methods for processing the ASR output are abstract, so that each app using the ASRLib can
 * specify a different behavior.
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 2.4, 11/24/13
 *
 */

public abstract class ASR extends Activity implements RecognitionListener{

	private static SpeechRecognizer myASR;
	Context ctx;
	
	private static final String LIB_LOGTAG = "ASRLIB";
	
	/**
	 * Creates the single SpeechRecognizer instance and assigns a listener
	 * @see CustomRecognitionListener.java
	 * @param ctx context of the interaction
	 * */
	public void createRecognizer(Context ctx) {
			this.ctx = ctx;
			PackageManager packManager = ctx.getPackageManager();
			
			// find out whether speech recognition is supported
			List<ResolveInfo> intActivities = packManager.queryIntentActivities(
					new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
			if (intActivities.size() != 0) {
				myASR = SpeechRecognizer.createSpeechRecognizer(ctx);
				myASR.setRecognitionListener(this);
			}
			else
				myASR = null;
	}

	/**
	 * Starts speech recognition
	 * @param languageModel Type of language model used (see Chapter 3 in the book for further details)
	 * @param maxResults Maximum number of recognition results
	 */
	public void listen(String languageModel, int maxResults) throws Exception{
	
		if((languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) || languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)) && (maxResults>=0)) {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

			// Specify the calling package to identify the application
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.getPackageName());
				//Caution: be careful not to use: getClass().getPackage().getName());

			// Specify language model
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

			// Specify how many results to receive. Results listed in order of confidence
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);  
    
			// Start recognition
			myASR.startListening(intent);
		}
		else {
			Log.e(LIB_LOGTAG, "Invalid params to listen method");
			throw new Exception("Invalid params to listen method"); //If the input parameters are not valid, it throws an exception
		}

	}

	/**
	 * Stops listening to the user
	 */
	public void stopListening(){
		myASR.stopListening();
	}
	
	/********************************************************************************************************
	 * This class implements the {@link android.speech.RecognitionListener} interface, 
	 * thus it implements its methods. However not all of them are interesting to us:
	 * ******************************************************************************************************
	 */

	/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onResults(android.os.Bundle)
	 */
	@Override
	public void onResults(Bundle results) {
		Log.d(LIB_LOGTAG, "ASR results provided");
		
		if(results!=null){
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {  //Checks the API level because the confidence scores are supported only from API level 14: 
																					//http://developer.android.com/reference/android/speech/SpeechRecognizer.html#CONFIDENCE_SCORES
				//Processes the recognition results and their confidences
				processAsrResults (results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION), results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES));
				//											Attention: It is not RecognizerIntent.EXTRA_RESULTS, that is for intents (see the ASRWithIntent app)
			}
			else {
				processAsrResults (results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION), null); 
			}
		}
		else
			processAsrError(SpeechRecognizer.ERROR_NO_MATCH);
	}

	/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onReadyForSpeech(android.os.Bundle)
	 */
	@Override
	public void onReadyForSpeech(Bundle arg0) {
		Log.i(LIB_LOGTAG, "Ready for speech");
		processAsrReadyForSpeech();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onError(int)
	 */
	@Override
	public void onError(int errorCode) {
		processAsrError(errorCode);
	}

	/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onBeginningOfSpeech()
	 */
	@Override
	public void onBeginningOfSpeech() {	}

	/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onBufferReceived(byte[])
	 */
	@Override
	public void onBufferReceived(byte[] buffer) { }
	
	/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onBeginningOfSpeech()
	 */
	@Override
	public void onEndOfSpeech() {}

	/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onEvent(int, android.os.Bundle)
	 */
	@Override
	public void onEvent(int arg0, Bundle arg1) {}

	/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onPartialResults(android.os.Bundle)
	 */
	@Override
	public void onPartialResults(Bundle arg0) {}

		/*
	 * (non-Javadoc)
	 * @see android.speech.RecognitionListener#onRmsChanged(float)
	 */
	@Override
	public void onRmsChanged(float arg0) {
	}
	
	/**
	 * Abstract method to process the recognition results 
	 * @param nBestList	List of the N recognition results
	 * @param nBestConfidences List of the N corresponding confidences
	 */
	public abstract void processAsrResults(ArrayList<String> nBestList, float [] nBestConfidences);	

	/**
	 * Abstract method to process the situation in which the ASR engine is ready to listen
	 */
	public abstract void processAsrReadyForSpeech();
	
	/**
	 * Abstract method to process error situations
	 * @param errorCode code of the error (constant of the {@link android.speech.SpeechRecognizer} class
	 */
	public abstract void processAsrError(int errorCode);
}