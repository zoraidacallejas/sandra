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
 * Abstraction of the elements in a hand-crafted grammar, specifies
 * the methods that are common to all the different element types.
 * To be used as a superclass of all the classes that represent the different
 * elements (rules, items, repeats, ruleReferences and alternatives).
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.2, 08/23/13
 *
 */
abstract class GrammarElement {

	/**
	 * Obtains the regular expression corresponding to the grammar element 
	 * @return string that contains the regular expression
	 */
	abstract String getRegExpr();

	/**
	 * Obtains the semantic tags associated with the current element (and subelements)
	 * @return ArrayList with the semantic representations. It is composed of 
	 * 		vectors containing 2 Strings: 
	 * 			0: text of the grammar element
	 * 			1: its semantic interpretation
	 * 		   Null if there is no semantic representation
	 */
	abstract ArrayList<String[]> getSemanticTags();
	
}
