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
 * 
 * Represents a rule in a speech grammar (see chapter 6 for an example)
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.2, 08/23/13
 *
 */
class Alternative extends GrammarElement{

	//Comprised of alternative GrammarElements
	ArrayList<GrammarElement> alternatives = new ArrayList<GrammarElement>(3); 
	
	void addAlternative(GrammarElement alternative){
		alternatives.add(alternative);
	}
	
	ArrayList<GrammarElement> getAlternatives(){
		return alternatives;
	}

	@Override
	ArrayList<String[]> getSemanticTags() {
		ArrayList<String[]> semantics = new ArrayList<String[]>(alternatives.size());
		
		for(GrammarElement el: alternatives) {
			if(el.getSemanticTags()!=null)
				semantics.addAll(el.getSemanticTags());
		}
		
		if(semantics.isEmpty())
			return null;
		else
			return semantics;
	}
	
	/**
	 * Obtains the regular expression corresponding to the alternative elements.
	 * It is build by adding the regular expressions of each element between parenthesis and
	 * separating them with "|"
	 */
	@Override
	String getRegExpr(){
		
		String result ="";

		ArrayList<GrammarElement> alternatives = ((Alternative) this).getAlternatives();
			
		for(int i=0; i<alternatives.size()-1; i++)
			result+=" ("+alternatives.get(i).getRegExpr()+") |";
		result+=" ("+alternatives.get(alternatives.size()-1).getRegExpr()+") ";

		
		return result;
	}
}
