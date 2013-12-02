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

import java.util.ArrayList;

/**
 * Represents an item in the speech grammar (see chapter 6 for an example)
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.2, 08/23/13
 *
 */

class Item extends GrammarElement{

	String text=null;
	String semantic=null;
	
	void setText(String txt) throws GrammarException{
		text = txt;
	}
	
	void setSemantic(String tag){
		semantic = tag; 
	}
	
	String getText(){
		return text;
	}
	

	@Override
	ArrayList<String[]> getSemanticTags() {
		ArrayList<String[]> result = null;
		
		if(text!=null && semantic!=null)
		{
			String [] tags = new String[2];
			tags[0] = text;
			tags[1] = semantic;
			
			result = new ArrayList<String[]>(1);
			result.add(tags);
		}
		
		return result;
		
	}
	
	@Override
	String getRegExpr(){
		
		return ((Item) this).getText();
	}
}
