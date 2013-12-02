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
* Exception to account for the problems that may appear in the FormFillLib.
* It allows a more fine-grained management of exceptions, and to provide
* a possible reason for them that can be communicated to the user
* 
* @author Zoraida Callejas
* @author Michael McTear
* @version 1.0, 08/18/13
*
*/

public class FormFillLibException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private String reason;

	public FormFillLibException(String message){
		super(message);
	}

	public FormFillLibException(String message, String reason){
		super(message);
		this.reason = reason;
	}
	
	public String getReason(){
		return reason;
	}
}
