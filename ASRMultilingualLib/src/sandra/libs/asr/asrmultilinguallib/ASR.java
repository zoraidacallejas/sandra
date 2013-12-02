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
package sandra.libs.asr.asrmultilinguallib;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
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
 * Abstract class for ASR
 * It encapsulates the management of the ASR engine.
 * It is abstract because it has an abstract method for processing of the ASR result
 * that must be implemented in a non-abstract subclass)
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 3.0, 09/04/13
 *
 */

//TODO: Check available languages for recognition (for the multilingual Chapter):
// Code here: http://stackoverflow.com/questions/10538791/how-to-set-the-language-in-speech-recognition-on-android/10548680#10548680

//TODO control this @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) //Confidence scores are not supported in previos versions
public abstract class ASR  extends Activity implements RecognitionListener{

	private static SpeechRecognizer myASR;
	Context ctx;
	
	private static final String LIB_LOGTAG = "ASR_MULTILINGUAL_LIB";
	
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
	 * CHANGED FOR CHAPTER 7
	 * Starts speech recognition
	 * @param languageModel Type of language model used (see Chapter 3 in the book for further details)
	 * @param maxResults Maximum number of recognition results
	 */
	public void startASR(String language, String languageModel, int maxResults){
	
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify the calling package to identify the application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.getPackageName());
			//Caution: be careful not to use: getClass().getPackage().getName());

		// Specify language model
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

		// Specify how many results to receive. Results listed in order of confidence
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);  
		
		//NEW:  Specify language
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
		
    	myASR.startListening(intent);
	}
	
	/**
	 * Checks the availability of the specified language for ASR. If it is available it starts recognition, if not it throws an exception
	 */
	public void listen(final Locale language, final String languageModel, final int maxResults) throws Exception 
    {
		if((languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) || languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)) && (maxResults>=0)) 
		{

	        OnLanguageDetailsListener andThen = new OnLanguageDetailsListener() //From https://github.com/gast-lib (see the OnLanguageDetailsListener class)
	        {
	            @Override
	            public void onLanguageDetailsReceived(LanguageDetailsChecker data)
	            {
	                //Do a best match
	                String recognitionLanguage = data.matchLanguage(language); 
	                if(recognitionLanguage!=null)
	                	startASR(recognitionLanguage, languageModel, maxResults);
	            }
	        };
	
	        Intent detailsIntent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
	        LanguageDetailsChecker checker = new LanguageDetailsChecker(andThen);	//From https://github.com/gast-lib (see the LanguageDetailsChecker class)
	        sendOrderedBroadcast(detailsIntent, null, checker, null,Activity.RESULT_OK, null, null);
		
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
	 * thus it implement its methods. However not all of them were interesting to us this time:
	 * ******************************************************************************************************
	 */

	@SuppressLint("InlinedApi")
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