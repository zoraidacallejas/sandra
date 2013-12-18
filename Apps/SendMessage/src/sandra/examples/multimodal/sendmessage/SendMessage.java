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

package sandra.examples.multimodal.sendmessage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import sandra.libs.dm.multimodalformfilllib.Form;
import sandra.libs.dm.multimodalformfilllib.MultimodalDialogInterpreter;
import sandra.libs.dm.multimodalformfilllib.MultimodalException;
import sandra.libs.dm.multimodalformfilllib.VXMLParser;

import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * SendMessage: App that allows to provide several pieces of data using both the oral and visual modalities.
 *
 * 
 * It uses the <code>ASRLib</code> and <code>TTSLib</code> libraries for speech 
 * recognition and synthesis and the <code>MultimodalFormFillLib</code> for managing the interaction
 * and synchronizing the modalities (see Chapter 8).
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 2.0, 10/20/13
 *
 */
public class SendMessage extends MultimodalDialogInterpreter {
	
	private static final String LOGTAG = "SENDMESSAGE";
	Form oralForm = null;	//Contains the structure of the oral dialog

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_message);

		initializeGUI();	//Initializes the GUI elements
		startDialog();		//Starts the oral dialog
	}
	
	/**
	 * Sets the default values and listeners of the GUI elements.
	 */
	private void initializeGUI(){
		setGUIinitialValues();	//Set default values

		//Set the listeners
		setContactList();
		setSendButton();
		setNewButton();
		setAckCheckbox();
		setUrgencyRadioGroup();
		setMessageEditText();
	}
	
	/**
	 * Sets the correspondence between GUI and oral dialog and starts interpreting the oral form (initiates the oral dialog).
	 */
	private void startDialog(){
		initializeAsrTts();
		parseOralForm();
		setMultimodalCorrespondence();
		
		try{
			startInterpreting(oralForm);
		}
		catch(MultimodalException e){
			Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.e(LOGTAG,e.getMessage()+" > "+e.getReason());	
		}
	}
	
	/**
	 * Sets the initial values for the GUI elements.
	 */
	private void setGUIinitialValues(){
		ListView contactList = (ListView) findViewById(R.id.contact_listview);
		contactList.clearChoices();
		contactList.requestLayout();
		
		CheckBox ackCb = (CheckBox) findViewById(R.id.ack_checkbox);
		ackCb.setChecked(false);
		
		RadioGroup rg = (RadioGroup) findViewById(R.id.urgency_radiogroup);
		rg.check(R.id.normal);
		
		EditText messageEditTxt = (EditText) findViewById(R.id.message_edittxt);
		messageEditTxt.setText("");
	}
	
	/**
	 * Reads the VXML file containing the structure of the oral dialog from the assets folder
	 * and parses it into a Form.
	 */
	private void parseOralForm(){
		try {
			String vxmlContent = getContentFromAssets("sendmessage.vxml");
			if(vxmlContent!=null)
			{
				oralForm = VXMLParser.parseVXML(vxmlContent, this);
			}
			else{
				Toast.makeText(this,"The oral dialog cannot be started", Toast.LENGTH_SHORT).show();
				Log.e(LOGTAG,"VXML file could not be read from assets folder");	
			}		
		}
		catch(Exception e){
			Toast.makeText(this,"The oral dialog cannot be started", Toast.LENGTH_SHORT).show();
			Log.e(LOGTAG,"VXML file could not be interpreted");	
		}
	}

	/**
	 * Establishes the correspondence between the GUI elements and the Fields in the oral Form
	 */
	private void setMultimodalCorrespondence(){
		addCorrespondence(oralForm.getField(0), findViewById(R.id.contact_listview));
		addCorrespondence(oralForm.getField(1), findViewById(R.id.ack_checkbox));
		addCorrespondence(oralForm.getField(2), findViewById(R.id.urgency_radiogroup));
		addCorrespondence(oralForm.getField(3), findViewById(R.id.message_edittxt));
	}

	
	/**
	 * Adds the contact names to the corresponding list view in the GUI and
	 * establishes the synchronization with its corresponding oral field.
	 * 
	 * This is a mock app that does not actually use the contacts in the users' phone
	 * in order to do that, you can follow the instructions here to firstly generate
	 * the contact_grammar and populate the list view automatically:
	 * http://developer.android.com/training/contacts-provider/retrieve-names.html
	 */	
	void setContactList(){

	    ListView listView = (ListView) findViewById(R.id.contact_listview);
		
		ArrayList<String> contacts = new ArrayList<String>();
		contacts.add("My mother");
		contacts.add("My uncle");
		contacts.add("My brother");
		
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, contacts); //Necessary API level 11
		listView.setAdapter(mAdapter);
		listView.setSelector(new ColorDrawable(0));
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        
        listView.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				try {
					guiToOral(parentView);
				} catch (MultimodalException e) {
					Log.e(LOGTAG,"Unable to synchronize list view with its corresponding oral field");	
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {}
        });

	}
	
	/**
	 * Sets the listener for the radio group which is in charge of updating the corresponding oral field when a different
	 * ratio button is checked.
	 */
	void setUrgencyRadioGroup(){
		RadioGroup rg = (RadioGroup) findViewById(R.id.urgency_radiogroup);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup rg, int checkedid) {
				try{
					guiToOral(rg);	
				} catch (MultimodalException e) {
					Log.e(LOGTAG,"Unable to synchronize list view with its corresponding oral field");	
				}
			}	
		});
		
	}
	
	/**
	 * Sets the listener for the checkbox which is in charge of updating the corresponding oral field when it is
	 * checked or unchecked.
	 */
	void setAckCheckbox(){
		CheckBox ckb = (CheckBox) findViewById(R.id.ack_checkbox);
		ckb.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View cbox) {
				try{
					guiToOral(cbox);	
				} catch (MultimodalException e) {
					Log.e(LOGTAG,"Unable to synchronize list view with its corresponding oral field");	
				}
			}	
		});
		
	}
	
	/**
	 * Sets the listener for the edit text view which is in charge of updating the corresponding oral field when the 
	 * a text is written and the "enter" key is pressed.
	 */
	void setMessageEditText(){
		EditText txt = (EditText) findViewById(R.id.message_edittxt);
		txt.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
				try{
					if(event!=null) //if event!=null > The action was triggered by an enter key
									//see: http://developer.android.com/reference/android/widget/TextView.OnEditorActionListener.html
						guiToOral(tv);	
				} catch (MultimodalException e) {
					Log.e(LOGTAG,"Unable to synchronize list view with its corresponding oral field");
					return false;
				}
				
				return true;
			}
		});
	}
	
	/**
	 * Sets the onclicklistener in the send button, which invokes the <code>sendMessage</code> method.
	 */
	void setSendButton() {
		Button speak = (Button) findViewById(R.id.send_btn);
		speak.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sendMessage();
				}
		});
	}
	
	/**
	 * Sets the onclicklistener in the "new message" button, which restarts the dialog.
	 */
	void setNewButton() {
		Button speak = (Button) findViewById(R.id.new_btn);
		speak.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					restart();
				}
		});
	}
	
	/**
	 * Restarts the app by setting the default values for the GUI elements and restarting the 
	 * oral dialog.
	 */
	private void restart(){
		setGUIinitialValues();
		try{
			clearAllFields();
			startInterpreting(oralForm);
		}
		catch(MultimodalException e){
			Log.e(LOGTAG,e.getMessage()+" > "+e.getReason());	
		}
		
		
	}

	
	/** 
	 * This is a mock app that does not actually send the message, but instead it shows a Toast.
	 * 
	 * In order to make it functional, you can use the SMSManager for sending SMS, or an Intent with 
	 * ACTION_SEND and EXTRA_EMAIL to send e-mails.
	 */
	void sendMessage(){
		Toast.makeText(this,"Message sent", Toast.LENGTH_SHORT).show();
	}

	
	/**
	 * Reads the content of the file in the assets folder.
	 */
	private String getContentFromAssets(String filename) {
		
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		
		try {
			AssetManager assetManager = this.getAssets();
			InputStream inputStream = assetManager.open(filename);	 
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
	 * This is a mock app that does not really send a message. In a real setting it would invoke the <code>sendMessage()</code> 
	 * and will send the message.
	 */
	@Override
	public void processDialogResults(HashMap<String, String> arg0) {
		Toast.makeText(this,"The message was created successfully!", Toast.LENGTH_SHORT).show();
		Log.i(LOGTAG,"Message created successfully");	
	}
	
	/**
	 * When the app is destroyed (e.g. when the orientation of the device changes), the app is restarted.
	 */
	protected void onDestroy() {
		super.onDestroy();
		shutdownTts();
		initializeAsrTts();
		restart();
	}

}
