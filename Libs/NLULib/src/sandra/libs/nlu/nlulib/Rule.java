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
import java.util.HashMap;


/**
 * Represents a rule in a speech grammar (see chapter 6 for an example)
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.2, 08/23/13
 *
 */
class Rule extends GrammarElement{
	
	private String id;	//Rule id, parsed from the <rule> tag
	private ArrayList<GrammarElement> elements = new ArrayList<GrammarElement>(3); //Elements nested in the rule
										//Initial capacity of 3 elements, see here: http://docs.oracle.com/javase/1.5.0/docs/api/java/util/ArrayList.html	
	private String regularExpression = "";	//Regular expression corresponding to the rule
	
	void setId(String id){
		this.id = id;
	}
	
	String getId(){
		return id;
	}
	
	void addElement(GrammarElement element){
		elements.add(element);
	}
	
	/**
	 * Obtains the semantic tags associated with all the elements of the rule
	 * @param rules HashMap with all the grammar rules that have been already parsed. It is
	 * 		used to solve references to other rules within the current one
	 * @return ArrayList with the semantic representations of all elements. It is composed of 
	 * 		vectors containing 2 Strings: 
	 * 			0: text of the grammar element
	 * 			1: its semantic interpretation
	 */
	public ArrayList<String[]> getSemanticTags(HashMap<String, Rule> rules){
		
		//Semantic tags
		ArrayList<String[]> semanticTags = new ArrayList<String[]>(elements.size());
		
		for(GrammarElement el: elements){
			
			if(el.getSemanticTags()!=null) //Careful: addAll throws Exception if its argument is null
				semanticTags.addAll(el.getSemanticTags());
			
			//If the element is a rule reference, then the referred rule is solved and is semantic tags are added 
			if(el instanceof RuleReference){
				Rule referedRule = rules.get(((RuleReference) el).getRefId());
				if(referedRule.getSemanticTags(rules)!=null)
					semanticTags.addAll(referedRule.getSemanticTags(rules));
			}
		}
		
		if(semanticTags.isEmpty())
			return null;
		else
			return semanticTags;
	}
	
	/**
	 * Computes the regular expression corresponding to the rule by concatenating the
	 * expressions corresponding to its constituent elements. 
	 */
	void setRegularExpression(){
		for(GrammarElement element: elements){
			regularExpression += " "+element.getRegExpr();
		}
	}

	/**
	 * This method is inherited from GrammarElement but it is not used for obtaining the semantic tags.
	 * Instead, the <code>ArrayList<String[]> getSemanticTags(HashMap<String, Rule> rules)</code> method
	 * is used, which indicates the rules as an argument so that rule references can be solved and
	 * processed.
	 */
	@Override
	ArrayList<String[]> getSemanticTags(){
		return null;
	}

	@Override
	String getRegExpr() {
		return regularExpression;
	}

}
