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
 * Represents an repeat element in the speech grammar (see chapter 6 for an example)
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.1, 08/23/13
 *
 */

class Repeat extends GrammarElement{

	ArrayList<GrammarElement> elements = new ArrayList<GrammarElement>();
	int min=0;
	int max=0;
	
	void add(GrammarElement element){
		elements.add(element);
	}

	ArrayList<GrammarElement> getElements(){
		return elements;
	}
	
	void setMin(int min){
		this.min = min;
	}
	
	void setMax(int max){
		this.max = max;
	}
	
	int getMin(){
		return min;
	}
	
	int getMax(){
		return max;
	}
	
	/**
	 * Sets the mininum and maximum number of repetitions
	 * @param conditions The two possibilities are:
	 * 						"n" -> Repeated exactly n times
	 * 						"m-n" -> Repeated between m and n times (both extremes included)
	 * @note The option "m-" (repeated m times or more), which can be usually found in speech grammars, is NOT SUPPORTED
	 * @throws GrammarException When the repeat condition cannot be parsed
	 */
	void setRepeatConditions(String conditions) throws GrammarException{
		String[] tokens = conditions.split("-");
		if(tokens.length!=2)
			throw new GrammarException("Could not parse grammar", "Invalid repeat condition");
		
		setMin(Integer.parseInt(tokens[0].trim()));
		setMax(Integer.parseInt(tokens[1].trim()));
	}


	@Override
	ArrayList<String[]> getSemanticTags() {
		ArrayList<String[]> semantics = new ArrayList<String[]>(elements.size());
		for(GrammarElement el: elements) {
			if(el.getSemanticTags()!=null)
				semantics.addAll(el.getSemanticTags());
		}
		
		if(semantics.isEmpty())
			return null;
		else
			return semantics;
	}
	
	/**
	 * Obtains the regular expression indicating the repeat condition in the appropriate format "{min,max}"
	 */
	@Override
	String getRegExpr(){
		
		String result ="";
		String regExprElements = "";
			
		ArrayList<GrammarElement> elements = ((Repeat) this).getElements();
			
		for(GrammarElement el: elements)
		{
			regExprElements+=el.getRegExpr();
		}
			
		result+="( "+regExprElements+" ){"+((Repeat) this).getMin()+","+((Repeat) this).getMax()+"}";
			
		return result;
			
	}
}
