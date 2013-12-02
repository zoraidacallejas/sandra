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

import android.annotation.SuppressLint;
import java.util.ArrayList;

/**
 * Represents the semantic parsing for a phrase, including the position index that allows to determine
 * the group of the regular expression that should match the text that triggers each tag.
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.0, 08/23/13
 *
 */
class SemanticParsing {

	ArrayList<String[]> tags;
	String phraseId;
	int phrasePosition;
	
	SemanticParsing(String phraseId, ArrayList<String[]> tags, int position){
		this.phraseId = phraseId;
		this.tags = tags;
		phrasePosition = position;
	}
	
	
	void setPosition(int position){
		phrasePosition = position;
	}
	
	int getPosition(){
		return phrasePosition;
	}
	
	
	/**
	 * Returns a String with the matching phrases and semantic tags corresponding to the <code>keyword</code> in the
	 * indicated <code>position</code> or null if there is no match.
	 */
	@SuppressLint("DefaultLocale")
	String matches(int position, String keyword){
		String result = null;
		
		if(phrasePosition==position){
			for(String [] tag: tags)
			{
				if(keyword.toLowerCase().equals(tag[0].replaceAll("\\s","").toLowerCase())){  //No spaces
					result = phraseId+": "+tag[1];
					break;
				}
			}
		}
		
		return result;
	}

}