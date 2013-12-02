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

package sandra.libs.nlu.nlulib;

import org.xmlpull.v1.XmlPullParserException;

import sandra.libs.asr.asrlib.ASR;
import sandra.libs.util.xmllib.RetrieveXMLTask;
import sandra.libs.util.xmllib.XMLAsyncResponse;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

/**
 * Processes the output of the speech recognizer either with a statistical or a handcrafted grammar
 * obtaining its semantic interpretation.
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 2.0, 09/20/13
 */	
public abstract class NLU extends ASR implements XMLAsyncResponse{

	private static final String LOGTAG = "MALUBANLU";
	private static final String KEY = "Zdu1JF28QXAYpxA1DcIpLSGeX2MnXzAq";	//Replace it with your Maluuba developer key
	private HandCraftedGrammar grammar = null;
		
	/*
	 * *****************************************************************
	 * HANDCRAFTED NLU GRAMMAR: 
	 * Step 1: Initialize the grammar (initializeHandCrafted)
	 * Step 2: Process a phrase (getResultsHandCrafted)
	 * *****************************************************************
	 */
	
	/**
	 * Parses the grammar into Java objects and converts them to a regular expression
	 * that it used in the other methods to validate phrases and obtain their semantics.
	 * 
	 * @param XMLGrammarContent	String with the handcrafted XML grammar
	 * @throws XmlPullParserException When the XML does not follow the valid format
	 * @throws GrammarException When there is any other exception //TODO
	 * @see Chapter 6 for the valid XML format for hand-crafted grammars
	 */
	public void initializeHandCrafted(String XMLGrammarContent) throws XmlPullParserException, GrammarException{
		grammar = new HandCraftedGrammar(XMLGrammarContent);	
	}
	
	/**
	 * Validates a phrase using the handcrafted grammar. If it is valid, it returns its semantic representation
	 * in the form a String with all the items and their semantic values. If the phrase is not valid
	 * according the grammar, then it returns null.
	 * 
	 * @see An example of a semantic parsing for a phrase according to a sample grammar in Chapter 6
	 * @param phrase String to be interpreted (usually it is the recognized phrase from the user's spoken input)
	 * @return Semantic interpretation if the phrase is valid, null if it is not.
	 * @throws GrammarException When there is a parsing error or the phrase is null.
	 */
	public String getResultsHandCrafted(String phrase) throws GrammarException{
		if(phrase!=null)
			return grammar.obtainSemantics(phrase);
		else
			throw new GrammarException("Parsing error", "The phrase to be parsed is not valid");

	}
	
	
	/*
	 * *****************************************************************
	 * STATISTICAL NLU GRAMMAR: 
	 * Step 1: Start service to process a phrase (startStatistical - connectToMaluuba)
	 * Step 2: Process results (processXMLContents - processResultsFromStatistical)
	 * *****************************************************************
	 */
	
	
	/**
	 * Initiates the connection to the statistical NLU processing service.
	 * 
	 * @param phrase String to be interprete
	 * @throws GrammarException If the phrase is null
	 */
	public void startStatistical(String phrase) throws GrammarException{
		/* We have created the method "startStatistical" instead of using 
		 * connectToMaluuba directly so that it is possible to consider
		 * different services for statistical NLU just by changing
		 * the code of this method maintaining the same header, which
		 * prevents for having to make changes in other parts of the code
		 */
		if(phrase!=null)
			connectToMaluuba(phrase);
		else
			throw new GrammarException("Parsing error", "The phrase to be parsed is not valid");
	}
	
	/**
	 * Processes the results of the Maluuba service: a String <code>semantics</code> with the semantic.
	 * representation of the phrase used to start the service (see method <code>startStatistical(phrase)</code>).
	 */
	public abstract void processResultsFromStatistical(String semantics);
		
	
	/**
	 * Initiates the connection to the Maluuba service for processing the phrase.
	 * 
	 * @param phrase String to be interpreted using the Maluuba service.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void connectToMaluuba(String phrase){
		
			//Url for semantic interpretation in the Maluuba API
			String url = "http://napi.maluuba.com/v0/interpret?phrase="+ phrase + "&apikey=" + KEY;
			
			//Start the connection...
			RetrieveXMLTask retrieveMaluuba = new RetrieveXMLTask();				       //AsyncTask to retrieve the output (semantics) from the URL
			retrieveMaluuba.delegate = (XMLAsyncResponse) this;							   //It is crucial in order to retrieve the data from the asynchronous task (see XMLLib)
			retrieveMaluuba.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, url);   //An Executor that can be used to execute tasks in parallel.
		
			
			//Check the status of the asynchronous task and print it in the log
			//		See reference here: http://developer.android.com/reference/android/os/AsyncTask.Status.html
			if(retrieveMaluuba.getStatus() == (AsyncTask.Status.PENDING)) {
		        //Indicates that the task has not been executed yet
				Log.i(LOGTAG, "Maluuba connection: Pending");
		    } else if(retrieveMaluuba.getStatus() == (AsyncTask.Status.RUNNING)) {
		        //Indicates that the task is running
		    	Log.i(LOGTAG, "Maluuba connection: Running");
		    } else if(retrieveMaluuba.getStatus() == (AsyncTask.Status.FINISHED)) {
		        //Indicates that AsyncTask.onPostExecute has finished
		    	Log.i(LOGTAG, "Maluuba connection: Finished");
		    } 
	}

	/**
	 * When the Maluuba service produces a result (the semantics of the phrase), it invokes the abstract method
	 * <code>processResultsFromStatistical</code>. 
	 */
	@Override
	public void processXMLContents(String semantics) {
		processResultsFromStatistical(semantics);
	}


}
