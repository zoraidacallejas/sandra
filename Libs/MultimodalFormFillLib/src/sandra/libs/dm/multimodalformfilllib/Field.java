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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParserException;

import sandra.libs.nlu.nlulib.GrammarException;
import sandra.libs.nlu.nlulib.HandCraftedGrammar;
import sandra.libs.util.xmllib.RetrieveXMLTask;
import sandra.libs.util.xmllib.XMLAsyncResponse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;


/**
* Represents a field in a dialogue. Contains the relevant information for processing a piece of information: name
* of the piece of information (e.g. destination), sentence used to prompt the user for it (e.g. "What is your destination?),
* the valid responses, and the sentences used if the system does not understand or does not hear what the user said (e.g.
* "I did not understand" / "I could not hear you". Also it contains the information provided by the user to fill the field
* (e.g. Paris).
*    
* It is a version of the class <code>sandra.libs.dm.formfilllib.Field</code> (chapter 5, FormFillLib project).
* Changes: <code>grammar</code> attribute, methods <code>retrieveGrammar</code>, <code>setGrammar</code>, <code>isvalid</code>, 
* and <code>processXMLContents</code>.
*
* @author Zoraida Callejas
* @author Michael McTear
* @version 1.3, 08/18/13
*
*/

@SuppressLint("NewApi")
public class Field implements XMLAsyncResponse{

	private String name = null; 							//Name of the field (e.g. "Destination")
	private String prompt = null;							//String used to prompt the user for the field (e.g. "What is your destination?")
	private String nomatch = "I did not understand";		//String used to tell the user that the system could not understand what they said
	private String noinput = "I did not hear anything";		//String used to tell the user that the system could not hear them
	private HandCraftedGrammar grammar = null;				//Grammar for speech recognition. If null, no grammar is considered
	private String value = null;							//Value for the field provided by the user (e.g. "Paris")
	
	private static final String LOGTAG = "FIELD";

	public void setValue(String value){
		this.value=value;
	}
	
	public String getValue(){
		return value;
	}
	
	public void setPrompt(String prompt){
		this.prompt=prompt;
	}
	
	public String getPrompt(){
		return prompt;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setNomatch(String nomatch){
		this.nomatch=nomatch;
	}
	
	public String getNomatch(){
		return nomatch;
	}
	
	public void setNoinput(String noinput){
		this.noinput=noinput;
	}
	
	public String getNoinput(){
		return noinput;
	}

	/**
	 * A field is complete if the prompt and name are not null, and thus it is possible to ask the user for the field.
	 */
	public boolean isComplete() {
		if(prompt==null || name==null)
			return false;
		return true;
	}
	
	/**
	 * A field is filled when the user has provided a valid value for it.
	 */
	public Boolean isFilled(){
		return value!=null;
	}

	
	/*
	 * ************************************************
	 * New for multimodality (new to chapter 8)
	 * ************************************************
	 */
	
	/**
	 * Sets the NLU grammar for the current field.
	 * @param uri URL to the xml file containing the grammar.
	 * @param ctx Context of the application. It is necessary in order to access the assets folder.
	 * @see Valid format for the grammar in chapter 6.
	 */
	public void setGrammar(String uri, Context ctx){
		retrieveGrammar(uri, ctx);
	}
	
	/**
	 * Starts the retrieval of the NLU grammar from the specified location.
	 * If the location is an URL, it access the Internet, if not, it tries to read it from the assets folder.
	 * When it is done, the <code>processXMLContents</code> method is invoked.
	 * @param location URL or path in the assets folder for the grammar file.
	 * @param ctx Context of the application. It is necessary in order to access the assets folder.
	 */
	private void retrieveGrammar(String location, Context ctx){
		
		//URL
		if(location.contains("http") || location.contains("www")) {
			RetrieveXMLTask retrieve = new RetrieveXMLTask();	//AsyncTask to retrieve the contents of the XML file from the URL
			retrieve.delegate = (XMLAsyncResponse) this;	//It is crucial in order to retrieve the data from the asynchronous task (see the AsyncResponse and RetrieveXMLTask classes)
			retrieve.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, location, location); //An Executor that can be used to execute tasks in parallel.
		
			
			//See reference here: http://developer.android.com/reference/android/os/AsyncTask.Status.html
			if(retrieve.getStatus() == (AsyncTask.Status.PENDING)) {
		        //Indicates that the task has not been executed yet
				Log.i(LOGTAG, "Grammar connection: Pending");
		    } else if(retrieve.getStatus() == (AsyncTask.Status.RUNNING)) {
		        //Indicates that the task is running
		    	Log.i(LOGTAG, "Grammar connection: Running");
		    } else if(retrieve.getStatus() == (AsyncTask.Status.FINISHED)) {
		        //Indicates that AsyncTask.onPostExecute has finished
		    	Log.i(LOGTAG, "Grammar connection: Finished");
		    } 
		//ASSESTS
		} else {
			try{
				processXMLContents(getContentFromAssets(location, ctx));
			} catch (IOException e) {
				grammar = null;
				Log.e(LOGTAG, "Grammar could not be initialized, using no grammar");
			}
		}
	} 
	
	/**
	 * Reads a file from the assets folder.
	 * @param filename Path inside the assets folder.
	 * @return File contents.
	 * @throws IOException If it is not possible to read the specified file.
	 */
	private String getContentFromAssets(String filename, Context ctx) throws IOException {
		
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		
		try{
			AssetManager assetManager = ctx.getAssets();
			InputStream inputStream;
			inputStream = assetManager.open(filename);
		 
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String text=null;
		     
			while ((text = reader.readLine()) != null) {
				contents.append(text).append(System.getProperty("line.separator"));
			}
	
			reader.close();
		} catch (IOException e) {
			reader.close();
			throw new IOException(e.getMessage());
		}
			
		return contents.toString();
	}
	
	/**
	 * Initializes the HandCraftedGrammar with the xml read. It is invoked when the <code>retrieveGrammar</code> finishes processing.
	 * If the grammar cannot be initialized, then it is not considered.
	 */
	@Override
	public void processXMLContents(String XMLGrammarContent) {
		
		try {
			grammar = new HandCraftedGrammar(XMLGrammarContent);
		} catch (XmlPullParserException e) {
		} catch (GrammarException e) {
			grammar = null;
			Log.e(LOGTAG, "Grammar could not be initialized, using no grammar");
		}
		
	}

	/**
	 * Checks whether a phrase recognized to fill the current field is valid according to the
	 * NLU grammar considered.
	 * @return	 True if there is no grammar or the grammar could not be used, or if there is grammar and the phrase matches it. 
	 * 			 False when the phrase does not match the grammar.
	 */
	public Boolean isvalid(String phrase){
		String semantics="NOTNULL";
		if(grammar!=null){
			try {
				semantics = grammar.obtainSemantics(phrase);
				if(semantics==null)
					Log.i(LOGTAG, "The recognition result "+phrase+" is not in the grammar");
			} catch (GrammarException e) {
				Log.e(LOGTAG, "Grammar could not be validated, accepting all values as valid (as if no grammar)");
			}
		}
		return semantics!=null;
	}
	
}
