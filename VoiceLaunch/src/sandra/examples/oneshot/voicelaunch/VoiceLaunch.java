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

package sandra.examples.oneshot.voicelaunch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.language.Soundex;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import sandra.libs.tts.TTS;
import sandra.libs.asr.asrlib.ASR;
import sandra.examples.oneshot.voicelaunch.R;

/**
 * VoiceLaunch: it launches the app mentioned by the user 
 * 
 * It uses the <code>ASRLib</code> and <code>TTSLib</code> libraries for speech recognition,
 * as well a library from <code>Apache commons</code>: http://commons.apache.org/proper/commons-codec/index.html
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 4.1, 08/07/13
 *
 */
public class VoiceLaunch extends ASR {

    private static final String LOGTAG = "VOICELAUNCH"; //Tag for log file
    
    /*
     * The similarity between each app name and the input of the user is measures from 0 (totally disimilar)
     * to 1 (identical). This threshold is used to determine which apps are considered (the ones which names
     * are similar with a value higher than the threshold).
     */
    
    //Enumeration with the 2 possibilities for calculating similarity
    private enum SimilarityAlgorithm{ORTHOGRAPHIC, PHONETIC};
    
    //Default values
    private static SimilarityAlgorithm DEFAULT_ALGORITHM = SimilarityAlgorithm.ORTHOGRAPHIC;
    private static float DEFAULT_THRESHOLD = 0; //From 0 to 1
    
    //Declaration of attributes
    private float similarityThreshold = DEFAULT_THRESHOLD;
    private SimilarityAlgorithm similarityCalculation = DEFAULT_ALGORITHM;

    //Single TTS object for the app
    private TTS myTts; 	
   
	/**
	 * When the activity is created, all elements are initialized: GUI, TTS and ASR
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Initializes the GUI showing the default values for the parameters
		setContentView(R.layout.voicelaunch); //Attention: remember to always include this line of code, otherwise the 
												//objects in your GUI will be null!
		initializeGUI();
		
		//Initializes the speech recognizer
		createRecognizer(getApplicationContext());	
		
		//Initializes the text-to-speech engine
	    myTts = TTS.getInstance(this);

		//Set up the speech button
		setSpeakButton();
	}
	
	/*************************************************************************************************************************************
	 * 			GUI-related methods
	 *************************************************************************************************************************************/
	
	/**
	 * Initializes the GUI showing the default value for the similarity threshold
	 * and the default algorithm for calculating similarity
	 */
	private void initializeGUI(){
		
		//Checks the radiobutton of the default similarity algorithm
		switch(DEFAULT_ALGORITHM){
			case ORTHOGRAPHIC:
				((RadioButton) findViewById(R.id.orthographic_radio)).setChecked(true);
				break;
			case PHONETIC:
				((RadioButton) findViewById(R.id.phonetic_radio)).setChecked(true);
				break;
			default:
				((RadioButton) findViewById(R.id.orthographic_radio)).setChecked(true);
				break;
		}
		
		//Shows the default similarity threshold in the seekBar
		initializeSeekBar();
		
		//Speech button appearance
		changeButtonAppearanceToDefault();
	}
	
	private void initializeSeekBar(){
		final SeekBar seekT= (SeekBar) findViewById(R.id.threshold_seekBar);
	    seekT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	        @Override
	        public void onStopTrackingTouch(SeekBar seekBar) {
	        }
	        @Override
	        public void onStartTrackingTouch(SeekBar seekBar) {
	        }
	        @Override
	        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	        	seekT.setThumb(writeOnDrawable(R.drawable.barpointershadow, String.format("%.1f",seekBarValueToFloat(progress))));
	        }
	     });

		seekT.setMax(10);	//SeekBar does not admit decimals, so instead of having it from 0 to 1, we will use it from 0 to 10
		seekT.setProgress(floatToSeekBarValue(DEFAULT_THRESHOLD)); 

	}
	/**
	 * Changes from the interval [0, 10] used in the seekbar, to the interval [0, 1] used for the similarity value
	 */
	private float seekBarValueToFloat(int seekValue){
	    return seekValue * 0.1f;
	}
	/**
	 * Changes from the interval [0, 1] used for the similarity value, to the interval [0, 10] used in the seekbar
	 */
	private int floatToSeekBarValue(float floatValue){
		return (int) (floatValue*10);
	}
	
	/**
	 * Writes a text in a drawable. We will use this method to show the similarity value in the seekbar
	 *  See stackoverflow http://stackoverflow.com/questions/6264543/draw-on-drawable?rq=1
	 */
	@SuppressWarnings("deprecation")
	private BitmapDrawable writeOnDrawable(int drawableId, String text){

	    Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

	    Paint paint = new Paint(); 
	    paint.setStyle(Style.FILL);  
	    paint.setColor(Color.BLACK); 
	    paint.setTextSize(10); 

	    Canvas canvas = new Canvas(bm);
	    canvas.drawText(text, bm.getWidth()/4, bm.getHeight()/2, paint);

	    return new BitmapDrawable(bm);
	}
	
	/**
	 * Initializes the search button and its listener. When the button is pressed, a feedback is shown to the user
	 * and the recognition starts
	 */
	private void setSpeakButton() {
		// gain reference to speak button
		Button speak = (Button) findViewById(R.id.speech_btn);
		speak.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//If the user is running the app on a virtual device, they get a Toast
					if("generic".equals(Build.BRAND.toLowerCase(Locale.US))){
						Toast toast = Toast.makeText(getApplicationContext(),"ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
						toast.show();
						Log.e(LOGTAG, "ASR attempt on virtual device");						
					}
					else {
							startListeningForApps();
					}
				}
			});
	}
	
	/**
	 * Reads the values for the similarity threshold and algorithm from the GUI
	 */
	private void readGUIParameters(){
		//String selectedThreshold = ((EditText) findViewById(R.id.threshold_editText)).getText().toString();
		try{
			similarityThreshold = seekBarValueToFloat(((SeekBar) findViewById(R.id.threshold_seekBar)).getProgress());
		} catch(Exception e) {	
			similarityThreshold = DEFAULT_THRESHOLD;
			 Log.e(LOGTAG, "The similarity threshold selected could not be used, using the default value instead");
		}
		
		RadioGroup radioG = (RadioGroup) findViewById(R.id.measure_radioGroup);
		switch(radioG.getCheckedRadioButtonId()){
			case R.id.orthographic_radio:
				similarityCalculation = SimilarityAlgorithm.ORTHOGRAPHIC;
				break;
			case R.id.phonetic_radio:
				similarityCalculation = SimilarityAlgorithm.PHONETIC;
				break;
			default:
				similarityCalculation = DEFAULT_ALGORITHM;
				Log.e(LOGTAG, "The similarity algorithm selected could not be used, using the default algorithm instead");
				break;
		}
	}
	
	/**
	 * Provides feedback to the user to show that the app is listening:
	 * 		* It changes the color and the message of the speech button
	 *      * It synthesizes a voice message
	 */
	private void indicateListening() {
		Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
		button.setText(getResources().getString(R.string.speechbtn_listening)); //Changes the button's message to the text obtained from the resources folder
		button.setBackgroundColor(getResources().getColor(R.color.speechbtn_listening)); //Changes the button's background to the color obtained from the resources folder
		myTts.speak(getResources().getString(R.string.initial_prompt)); 
		setListView(new ArrayList<String>()); //Clean result list 
	}
	
	/**
	 * Provides feedback to the user to show that the app is performing a search:
	 * 		* It changes the color and the message of the speech button
	 *      * It synthesizes a voice message
	 */
	private void indicateLaunch(String appName) {
		changeButtonAppearanceToDefault();
		myTts.speak("Launching "+appName); 
    	Toast.makeText(getBaseContext(), "Launching "+appName, Toast.LENGTH_LONG).show(); //Show user-friendly name
	}
	
	/**
	 * Provides feedback to the user to show that the app is idle:
	 * 		* It changes the color and the message of the speech button
	 */	
	private void changeButtonAppearanceToDefault(){
		Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
		button.setText(getResources().getString(R.string.speechbtn_default)); //Changes the button's message to the text obtained from the resources folder
		button.setBackgroundColor(getResources().getColor(R.color.speechbtn_default));	//Changes the button's background to the color obtained from the resources folder		
	}

	/**
	 * Shows the matching apps and their similarity values on the GUI
	 * @param sortedApps It is a tree map in which the last element is the most similar, and the first the least
	 */
	private void showMatchingNames(ArrayList<MyApp> sortedApps){
		ArrayList<String> result = new ArrayList<String>();
		for(MyApp app: sortedApps){
			result.add(app.getName()+" (Similarity: "+String.format("%.2f", app.getSimilarity())+")");
			//Drawable icon = getPackageManager().getApplicationIcon(app[1]);	
		}
		setListView(result);
	}
	
	/**
	 * Includes the recognition results in the list view
	 * @param nBestView list of matches
	 */
	private void setListView(ArrayList<String> matchingApps){
		
		// Instantiates the array adapter to populate the listView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matchingApps);
        ListView listView = (ListView) findViewById(R.id.matchingapps_listview);
        listView.setAdapter(adapter);
	}
	
	/*************************************************************************************************************************************
	 * Text-processing methods to compare name of apps
	 *************************************************************************************************************************************/
	
	/**
	 * Obtains a collection with information about the apps which name is similar to what was recognized from the user. The collection is sorted
	 * from most to least similar.
	 * @param recognizedName Name of the app recognized from the user input
	 * @return A collection of instances of MyApp. MyApp is an auxiliary class that we have created (see at the bottom of this file), to store
	 * information about the apps retreived, concretely: name, package name and similarity to recognized name. If no apps are found, it returns
	 * an empty list.
	 */
	private ArrayList<MyApp> getSimilarAppsSorted(String recogizedName) 
	{
        MyApp app;
        Double similarity=0.0;
               
        ArrayList<MyApp> similarApps = new ArrayList<MyApp>();

        PackageManager packageManager = getPackageManager();   
        List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);
        
        //For all apps installed in the device...
        for (int i=0; i<apps.size(); i++) {
            PackageInfo packInfo = apps.get(i);
            
            //Gets application name
            String name = packInfo.applicationInfo.loadLabel(packageManager).toString();
            
            //Gets package name
            String packageName = packInfo.packageName;

            //Measures similarity of the app's name with the user input
            switch(similarityCalculation){       		
            	case ORTHOGRAPHIC:
            		similarity = compareOrthographic(normalize(recogizedName), normalize(name));
            		break;
            	
            	case PHONETIC:
            		similarity = comparePhonetic(normalize(recogizedName), normalize(name));
            		break;
            		
            	default:
            		similarity = compareOrthographic(normalize(recogizedName), normalize(name)); 
            		break;	
            }
            
            //Adds the app to the collection if the similarity is higher than the threshold
            if(similarity > similarityThreshold) {
                app = new MyApp(name, packageName, similarity);
            	similarApps.add(app);
            }
        }
       
        //Sorts apps from least to most similar, in order to do this, we use our own comparator,
        //using the "AppComparator" class, which is defined as a private class at the end of the file
        Collections.sort(similarApps, new AppComparator());

        for(MyApp aux: similarApps)
        	Log.i(LOGTAG, "Similarity: "+aux.getSimilarity()+", Name: "+aux.getName()+", Package: "+aux.getPackageName());
        
        return similarApps;
	}

	
	/**
	 * Normalizes a text
	 * @param text
	 * @return the input text without spaces and in lower case
	 */
	private String normalize(String text){
		return text.trim().toLowerCase(Locale.US);
	}
	
	
	/**
	 * Compares the names using the Levenshtein distance, which is the minimal number of characters you have to replace, 
	 * insert or delete to transform string a into string b.
	 * We have used a computation of this distance provided by Wikipedia.
	 * @return similarity from 0 (minimum) to 1 (maximum)
	 */
	private double compareOrthographic(String a, String b){
		return LevenshteinDistance.computeLevenshteinDistance(a, b);
	}	
		
	/**
	 * Compares the names using their phonetic similarity, using the soundex algorithm.
	 * We have used an implementation of this algorithm provided by Apache.
	 * Attention: it only works for English
	 */
	private double comparePhonetic(String recognizedApp, String nameApp){		
	    Soundex soundex = new Soundex();
	    
	    //Returns the number of characters in the two encoded Strings that are the same. 
	    //This return value ranges from 0 to the length of the shortest encoded String: 0 indicates little or no similarity, 
	    //and 4 out of 4 (for example) indicates strong similarity or identical values. 
	    double sim=0;
		try {
			sim = soundex.difference(recognizedApp, nameApp);
		} catch (Exception e) {
			Log.e(LOGTAG, "Error during soundex encoding. Similarity forced to 0");
			sim = 0;
		}
	    return sim/4;
	}

	
	
	
	/*************************************************************************************************************************************
	 * ASR processing methods
	 *************************************************************************************************************************************/
	
	

	/**
	 * Listens for names of apps. Depending of the recognition results, the methods processError, processReadyForSpeech 
	 * or processResults will be invoked.
	 * Speech recognition is carried out in US English because it is the only language for which phonetic similarity
	 * works
	 */
	private void startListeningForApps() {
		try{
			indicateListening();
			//Recognition model = Free form, Number of results = 1 (we will use the best result to perform the search)
			listen(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM, 1); //Start listening
		} catch (Exception e) {
			Toast toast = Toast.makeText(getApplicationContext(),"ASR could not be started: invalid params", Toast.LENGTH_SHORT);
			toast.show();
			changeButtonAppearanceToDefault();
			Log.e(LOGTAG, "ASR could not be started: invalid params");
		}
	}
	
	/**
	 * When recognition is successful, it obtains the best recognition result (supposedly the name of an app),
	 * and sorts all apps installed on the device according to the similarity of their names to the one recognized
	 * (considering only the ones similar above a threshold).
	 * Then, it launches the app with highest similarity. If the similarities are all bellow the defined threshold, no app is launched and the user
	 * gets a feedback message in a Toast
	 */
	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {
		
		if(nBestList!=null){
			if(nBestList.size()>0){
				//Obtains the best recognition result
				String bestResult = nBestList.get(0);
				
				//Read the values for the similarity parameters from the GUI
				readGUIParameters();
		        
				//Obtains the apps installed in the device sorted from most to least similar name regarding the user input
				//String[] = [0] = name, [1] = package, [2] = similarity
				ArrayList<MyApp> sortedApps = getSimilarAppsSorted(bestResult);  
				
				//Shows the matching apps and their similarity values in a list
				showMatchingNames(sortedApps);
				        
				//Launches the best matching app (if there is any)
				if(sortedApps.size()<=0)
				{
					Toast toast = Toast.makeText(getApplicationContext(),"No app found with sufficiently similar name", Toast.LENGTH_SHORT);
					toast.show();
					Log.e(LOGTAG, "No app has a name with similarity > "+similarityThreshold);
				}
				else
					launchApp(sortedApps.get(0));
			}
		}
	}
		
	/**
	 * Launches the app indicated. 
	 * @param app see the MyApp class defined at the end of this file
	 */
	private void launchApp(MyApp app) {
        Intent launchApp = this.getPackageManager().getLaunchIntentForPackage(app.getPackageName()); //Launch by package name
        if (null != launchApp) {
            try {  
            	indicateLaunch(app.getName());
            	Log.i(LOGTAG, "Launching "+app.getName());
            	startActivity(launchApp);
            	//VoiceLaunch.this.finish();
            } catch (Exception e) {  
            	Toast.makeText(getBaseContext(), app.getName()+" could not be launched", Toast.LENGTH_LONG).show(); //Show user-friendly name
            	Log.e(LOGTAG, app.getName()+" could not be launched");
            }                       
        }
    }

	
	/**
	 * When the back button is pressed, the app starts again
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent= new Intent(getBaseContext(),VoiceLaunch.class);       	
		startActivity(intent);
	}
	
	/**
	 * Shuts down the TTS engine when finished
	 */   
	@Override
	public void onDestroy() {
		super.onDestroy();
		myTts.shutdown();
	}
	
	/**
	 * Provides feedback to the user (by means of a Toast and a synthesized message) when the ASR encounters an error
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
		changeButtonAppearanceToDefault();
		try {
			myTts.speak(errorMessage, "EN");
		} catch (Exception e) {
			Log.i(LOGTAG, "Selected language not available, using the device's default");
		}
        Log.e(LOGTAG, "Error when attempting listen: "+ errorMessage);
	}

	@Override
	public void processAsrReadyForSpeech() { }

	
	
	
	/***************************************************************************************************************************
	 * Auxiliary classes
	***************************************************************************************************************************/
	
	/**
	 * Represents each app to be considered for launching.
	 */
	private class MyApp {
		private String name; 			//User-friendly name
		private String packageName;		//Full name
		private double similarity;		//Similarity of its user-friendly name with the recognized input
		
		MyApp(String name, String packageName, double similarity){
			this.name = name;
			this.packageName = packageName;
			this.similarity = similarity;
		}
		
		String getName(){ return name; }
		String getPackageName(){ return packageName; }
		double getSimilarity(){ return similarity; }

	}
	
	/**
	 * Comparator for apps considering the similarity of their names to the recognized input.
	 */
	private class AppComparator implements Comparator<MyApp>{
		 
	    @Override
	    public int compare(MyApp app1, MyApp app2) {
			return (- Double.compare(app1.getSimilarity(), app2.getSimilarity())); // Multiply by -1 to get reverse ordering (from most to least similar)
	    }
	} 
}
	

