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
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
* Interprets a multimodal dialog in which the oral and visual modalities are synchronized.
* The oral dialog is controlled using a restricted VXML file (see chapter 5), in which
* each field is linked to an element in the GUI (as explained in chapter 7)
*
* @author Zoraida Callejas
* @author Michael McTear
* @version 1.3, 08/18/13
*
*/
public abstract class MultimodalDialogInterpreter extends DialogInterpreter{

	//The dialog is comprised of an array of multimodal elements (pairs of voice+visual fields)
	ArrayList<MultimodalElement> elements = new ArrayList<MultimodalElement>();
	
	/**
	 * Links a visual and oral field
	 */
	public void addCorrespondence(Field oralField, View guiField){
		elements.add(new MultimodalElement(oralField, guiField));
	}
	
	
	/**
	 * Obtains the oral field corresponding to a visual element
	 * @param guiField Visual element
	 * @return The oral field or null if the current GUI element has no correspondence
	 */
	private Field getOralField(View guiField){
		for(MultimodalElement element: elements){
			if(element.containsGuiField(guiField))
				return element.getOralField();
		}
		
		return null;
	}
	
	/**
	 * Obtains the visual element corresponding to an oral field
	 * @param oralField Oral field
	 * @return The visual field or null if the current oral field has no correspondence
	 */
	private View getGuiField(Field oralField){
		for(MultimodalElement element: elements){
			if(element.containsOralField(oralField))
				return element.getGuiField();
		}
		
		return null;
	}
	
	/**
	 * Synchronizes the oral and visual modalities in the direction: oral -> visual.
	 * That is, it can be used to fill a visual element when its corresponding oral field has been filled
	 */
	public void oralToGui(Field oralField) throws MultimodalException{
		View guiField= getGuiField(oralField);
		String value = oralField.getValue();
		
		if(guiField instanceof ListView)
			setListValue((ListView) guiField, value);
		else if (guiField instanceof RadioGroup)
			setRadioButton((RadioGroup) guiField, value);
		else if (guiField instanceof CheckBox)
			setCheckBox((CheckBox) guiField, value);
		else if (guiField instanceof TextView)
			setTextView((TextView) guiField, value);
		else 
			throw new MultimodalException("Invalid GUI element");
	}

	/**
	 * Synchronizes the oral and visual modalities in the direction: visual -> oral.
	 * That is, it can be used to fill the oral field when its corresponding visual element has been filled
	 */
	public void guiToOral(View guiField) throws MultimodalException{
		Field oralField= getOralField(guiField);
		String value; 
		
		if(guiField instanceof ListView)
			value = ((ListView) guiField).getSelectedItem().toString();
		
		else if (guiField instanceof RadioGroup) {
			int checkedId = ((RadioGroup) guiField).getCheckedRadioButtonId();
			View rb = ((RadioGroup) guiField).findViewById(checkedId);
			int radioId = ((RadioGroup) guiField).indexOfChild(rb);
		    RadioButton btn = (RadioButton) ((RadioGroup) guiField).getChildAt(radioId);
		    value = (String) btn.getText();
		}
		
		else if (guiField instanceof TextView)
			value = ((TextView) guiField).getText().toString();
		
		else if (guiField instanceof CheckBox) {
			if(((CheckBox) guiField).isChecked())
				value="true";
			else
				value="false";
		}
		
		else 
			throw new MultimodalException("Invalid GUI element");	
		
		oralField.setValue(value);
	}
	
	/**
	 * Provided a value gathered in the oral interface, the corresponding value is selected
	 * in a list view in the GUI
	 * @param list  Reference to the GUI list
	 * @param value Value of the oral field
	 * @throws MultimodalException If there is no such value in the list
	 */
	private void setListValue (ListView list, String value) throws MultimodalException{
		
		ListAdapter adapter = list.getAdapter();
		int position = 0;
		
		while (position<adapter.getCount() && !adapter.getItem(position).toString().toLowerCase().equals(value)){
			position++;
		}

		if(position == AdapterView.INVALID_POSITION)
			throw new MultimodalException("There is no value in the GUI corresponding to "+value);


		list.setItemChecked(position, true);	
	}
	
	/**
	 * Provided a value gathered in the oral interface, the corresponding value is selected
	 * in a radio group view in the GUI
	 * @param rg  Reference to the GUI radio group
	 * @param value Value of the oral field
	 * @throws MultimodalException If there is no such value in the radio group
	 */
	private void setRadioButton (RadioGroup rg, String value) throws MultimodalException{
		int position = 0;
		
		RadioButton button = (RadioButton) rg.getChildAt(position);
	    
		while (position<rg.getChildCount() && !((String) button.getText()).toLowerCase().equals(value)){
			position++;
			button = (RadioButton) rg.getChildAt(position);
		}

		if(position==rg.getChildCount())
			throw new MultimodalException("There is no value in the GUI corresponding to "+value);
		
		button.setChecked(true);
		
	}

	/**
	 * Provided a value gathered in the oral interface, the corresponding value is shown
	 * in a text view in the GUI
	 * @param tv  Reference to the GUI text view
	 * @param value Value of the oral field
	 * @throws MultimodalException If there is no such value in the radio group
	 */
	private void setTextView (TextView tv, String value){
		tv.setText(value);
	}
	
	/**
	 * Provided a value gathered in the oral interface, the corresponding value is selected
	 * in a checkbox in the GUI
	 * @param ch  Reference to the GUI checkbox
	 * @param value Value of the oral field
	 * @throws MultimodalException If there is no such value in the radio group
	 */
	private void setCheckBox (CheckBox ch, String value){
		if(value.equals("yes"))
			ch.setChecked(true);
		else
			ch.setChecked(false);
	}
	
	
	/**
	 * Represent a pair of oral and visual elements in the interface that retrieve the same piece
	 * of information from the user
	 */
	private class MultimodalElement {
	
		Field oralField;
		View guiField;
		
		MultimodalElement(Field oralField, View guiField){
			this.oralField = oralField;
			this.guiField = guiField;
		}
		
		Field getOralField(){
			return oralField;
		}
		
		View getGuiField(){
			return guiField;
		}
		
		Boolean containsOralField(Field field){
			if(oralField==field)
				return true;
			else
				return false;
		}
		
		Boolean containsGuiField(View field){
			if(guiField==field)
				return true;
			else
				return false;
		}
	}

}