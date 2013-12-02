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

package sandra.libs.dm.formfilllib;

/**
* Represents a field in a dialogue. Contains the relevant information for processing a piece of information: name
* of the piece of information (e.g. destination), sentence used to prompt the user for it (e.g. "What is your destination?),
* the valid responses, and the sentences used if the system does not understand or does not hear what the user said (e.g.
* "I did not understand" / "I could not hear you". Also it contains the information provided by the user to fill the field
* (e.g. Paris).
*    
* It is a simplification (see chapter 5) of VXML fields (http://www.vxml.org/frame.jsp?page=field.htm)
* 
* @author Zoraida Callejas
* @author Michael McTear
* @version 1.3, 08/18/13
*
*/
public class Field {

	private String name = null; 							//Name of the field (e.g. "Destination")
	private String prompt = null;							//String used to prompt the user for the field (e.g. "What is your destination?")
	private String nomatch = "I did not understand";		//String used to tell the user that the system could not understand what they said
	private String noinput = "I did not hear anything";		//String used to tell the user that the system could not hear them
	private String value = null;							//Value for the field provided by the user (e.g. "Paris")
	

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
	 * A field is complete if the prompt and name are not null, and thus it is possible to ask the user for the field
	 */
	public boolean isComplete() {
		if(prompt==null || name==null)
			return false;
		return true;
	}
	
	/**
	 * A field is filled when the user has provided a valid value for it
	 */
	public Boolean isFilled(){
		return value!=null;
	}
	
}
