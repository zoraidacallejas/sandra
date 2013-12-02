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

/**
* Represents a form containing a dialogue.
* It is a simplification of VXML forms (http://www.vxml.org/frame.jsp?page=form.htm) that 
* considers them as a collection of fields (see the Field class).
* 
* It is a version of the class <code>sandra.libs.dm.formfilllib.Form</code> (chapter 5, FormFillLib project).
* Changes: <code>allFieldsFilled</code> method.
* 
* @author Zoraida Callejas
* @author Michael McTear
* @version 1.1, 08/13/13
*
*/

public class Form {

	//Collection of fields
	private ArrayList<Field> fields = new ArrayList<Field>();
	
	/**
	 * Adds a new field to the collection of fields of the form
	 * @throws Exception When the field is not complete (it does not have all the information needed to process it
	 */
	public void addField(Field f) throws MultimodalException{
		if(f.isComplete())
			fields.add(f);
		else
			throw new MultimodalException("The field is not complete: name or prompt missing");
	}
	
	/**
	 * Returns the field in the indicated position 
	 */
	public Field getField(int position){
		return fields.get(position);
	}

	/**
	 * Calculates the number of fields in the form
	 */
	public int numberOfFields(){
		return fields.size();		
	}
	
	/*
	 * ************************************************
	 * New for multimodality (new to chapter 8)
	 * ************************************************
	 */
	/**
	 * Indicates whether all fields are filled (all have assigned values)
	 * @return true if all are filled, false if any of them is not
	 */
	public Boolean allFieldsFilled(){
		int i = 0;
		
		while(i<fields.size() && fields.get(i).isFilled()){
			i++;
		}
		
		return (i==fields.size());
	}
}
