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
 * Represents the reference to a rule in the grammar (see chapter 6 for an example)
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.1, 08/23/13
 *
 */
class RuleReference extends GrammarElement {

	String refId;
	
	void setRefId(String id){
		refId = id;
	}
	
	String getRefId(){
		return refId;
	}

	/**
	 * There is no semantic associated to the rule reference itself, but to the referred rule
	 * The semantic of the referred rule is obtained in the Rule class, once the reference
	 * is solved
	 */
	@Override
	ArrayList<String[]> getSemanticTags() {
		return null;
	}
	
	/**
	 * For the regular expression of a rule reference we use a pattern that contains the id of 
	 * the referred rule: "xxREFxxidxx"
	 */
	@Override
	String getRegExpr(){
		
		String result ="";
		String id= ((RuleReference) this).getRefId();
		result = " xxREFxx"+id+"xx ";		
		
		return result;
	}
}
