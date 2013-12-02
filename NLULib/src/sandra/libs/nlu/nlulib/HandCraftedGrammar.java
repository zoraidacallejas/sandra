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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


import android.annotation.SuppressLint;
import android.util.Log;

/**
 * Handcrafted speech grammar (see chapter 6)
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 2.4, 10/20/13
 *
 */
@SuppressLint("NewApi")
public class HandCraftedGrammar {
	
	//The contents of the grammar are parsed into rule objects and stores into this HashMap. The keys are the rules ids
	private HashMap<String, Rule> rules = new HashMap<String, Rule>(); 	
	
	//The contents of the grammar are also parsed to a regular expression
	private String grammarRegExpression; 
	
	//The semantics associated to the rules are stored into this HashMap. The keys are the positions of each rule into the regular expression of the grammar (see Chapter 6)
	private HashMap<Integer, SemanticParsing> semantics = new HashMap<Integer, SemanticParsing>();
	
	//Id of the main rule
	private String mainPhraseId;
	
	
	private final String LOGTAG = "HANDCRAFTEDGRAMMAR";


	/************************************************************************************
	 * METHODS FOR PARSING THE GRAMMAR
	 ************************************************************************************/
	
	/**
	 * Constructor: parses the grammar and obtains the corresponding regular expression
	 * @param xmlContent String with the xml grammar
	 * @throws XmlPullParserException If the grammar format is not correct or it cannot be read
	 * @throws GrammarException If the grammar format is not correct
	 */
	public HandCraftedGrammar(String xmlContent) throws XmlPullParserException, GrammarException{
		parse(xmlContent);
		grammarRegExpression = computeRegularExpression();
	}
	
	/**
	 * Returns the attributes of the current tag (e.g. field name), or null if it has no attributes.
	 * The parser must be placed in the current tag before invoking this method
	 * 
	 * Idea modified from: http://stackoverflow.com/questions/4827168/how-to-parse-the-value-in-the-attribute-in-xml-parsing
	 */
	private HashMap<String,String> getAttributes(XmlPullParser parser) {
		
	    HashMap<String,String> attributes=null;
	    
	    int numAttributes = parser.getAttributeCount();
	    
	    if(numAttributes != -1) {
	    	attributes = new HashMap<String,String>(numAttributes);
	    	
	        for(int i=0; i<numAttributes; i++)
	            attributes.put(parser.getAttributeName(i), parser.getAttributeValue(i));
	    }
	    
	    return attributes;
	}
		
	/**
	 * Parses the XML grammar contained in the xmlContent String to a list of phrases
	 * @throws XmlPullParserException If the grammar format is not correct or it cannot be read
	 * @throws GrammarException If the grammar format is not correct
	 */
	private void parse(String xmlContent) throws XmlPullParserException, GrammarException {
		
        String tagContents = null;
        int eventType;
        
        ArrayList<GrammarElement> pendingElements = new ArrayList<GrammarElement>();
        
        Boolean simpleItem = false; 		//true -> it is a leaf node with no nested tags
        
        //Auxiliary variables
        RuleReference ruleref = null;
        Item item = null;
              
        //Parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); //May throw XmlPullParserException
		XmlPullParser parser = factory.newPullParser();					   //May throw XmlPullParserException
		StringReader xmlReader = new StringReader(xmlContent);
		
		try{
			parser.setInput(xmlReader); //We are reading the xml from a string, but we could read it directly from the streaminput
		}
		catch(XmlPullParserException ex){
			throw new GrammarException(ex.getMessage(), "Grammar not accessible");
		}

		try{
			eventType = parser.getEventType();	//May throw a XMLPullParserException
			
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	                
	        	String tagname = parser.getName();

	            switch (eventType) {
	        		
		        	case XmlPullParser.START_TAG:
		        		simpleItem=false;
		        		
		        		//<grammar> -- It is not transformed into objects but contains the id of the root rule
		        		if (tagname.equalsIgnoreCase("grammar")){
		        			mainPhraseId = getAttributes(parser).get("root");
		        			if(mainPhraseId==null)
		        				throw new GrammarException("Parsing error", "The attribute root is mandatory in <grammar>");
		        			
		        		//<rule> -- It must contain an id, and it may contain nested elements (pendingElements)
	            		} else if (tagname.equalsIgnoreCase("rule")){
		        			Rule phrase = new Rule();
		        			String id = getAttributes(parser).get("id");
		        			if(id!=null){
		        				phrase.setId(id);
		        				pendingElements.add(phrase);
		        			} else
		        				throw new GrammarException("Parsing error", "The attribute id is mandatory in <rule>");
		        		
		        		//<one-of> -- It does not contain attributes, but it may contain nested elements (pendingElements)
		        		} else if (tagname.equalsIgnoreCase("one-of")){
		        			Alternative alternativeElements = new Alternative();
		        			pendingElements.add(alternativeElements);
		        	
		        		//<ruleref> -- It must contain an uri, and it does not contain nested elements
		        		} else if (tagname.equalsIgnoreCase("ruleref")){	
	                        ruleref = new RuleReference();
	                        String uri = getAttributes(parser).get("uri");
	                        if(uri!=null)
	                        	ruleref.setRefId(uri);
	                        else
		        				throw new GrammarException("Parsing error", "The attribute uri is mandatory in <ruleref>");
	                    
	                    //<tag> -- It just contains text
	                    } else if (tagname.equalsIgnoreCase("tag")){
	                    	simpleItem=true;
	                    
	                    //<item>
		        		} else if (tagname.equalsIgnoreCase("item")){
		        				//If the <item> contains repeat conditions, it is parsed as a Repeat object (it may contain nested elements)
		        				String repeatConditions = getAttributes(parser).get("repeat");
			        			if(repeatConditions!=null){
			        				Repeat repeatElement = new Repeat();
		        					repeatElement.setRepeatConditions(repeatConditions);
		        					pendingElements.add(repeatElement);
			        			}
			        			//If not, it is parsed as an Item (it just contains text)
			        			else{
			        				simpleItem = true;
			        				item = new Item();
			        			}	
			            }
	                break;
	 
	                case XmlPullParser.TEXT:
	                    tagContents = parser.getText();
	                break;
	 
	                case XmlPullParser.END_TAG:
	                	
	                	//</rule>
	                	if (tagname.equalsIgnoreCase("rule")) {
	                		//Invalid situation: the rule has no nested elements (at least it must contain an <item>), the rule contains
	                		//nested rules
	                        if(pendingElements.isEmpty() || pendingElements.size()>1 || !(pendingElements.get(pendingElements.size()-1) instanceof Rule))
	                        	throw new GrammarException("Parsing error", "Ill-formed XML Grammar");
	                        else
	                        {
	                        	Rule ph = (Rule) pendingElements.remove(pendingElements.size()-1);
		                        rules.put(ph.getId(),ph); 
	                        }

	                    //</ruleref>
	            		} else if (tagname.equalsIgnoreCase("ruleref")) {

	                        if(!pendingElements.isEmpty()){
	                        	Object element = pendingElements.get(pendingElements.size()-1);
	                        	if(element instanceof Rule)
	                        		((Rule) element).addElement(ruleref);
	                        	else if(element instanceof Alternative)
	                        		((Alternative) element).addAlternative(ruleref);
	                        	else if(element instanceof Repeat)
	                        		((Repeat) element).add(ruleref);
	                        	
	                        } else //Invalid situation: it has nested elements
	                        	throw new GrammarException("Parsing error", "Ill-formed XML Grammar");
	                    
	                    //</tag>
	                	} else if(tagname.equalsIgnoreCase("tag")){
	                		item.setSemantic(tagContents);
	                		
	                    //</item>
	                    } else if (tagname.equalsIgnoreCase("item")){
	                    	
	                    	if(simpleItem)  // simple item
	                    	{
	                    		simpleItem=false;
		                    	item.setText(tagContents);
		                        
	                        	Object element = pendingElements.get(pendingElements.size()-1);
	                        	if(element instanceof Rule)
	                        		((Rule) element).addElement(item);
	                        	else if(element instanceof Alternative)
		                        	((Alternative) element).addAlternative(item);
	                        	else if(element instanceof Repeat)
	                        		((Repeat) element).add(item);
		                        
	                    	}
	                    	else {	//item with repetitions

	                    		if(pendingElements.isEmpty() || !(pendingElements.get(pendingElements.size()-1) instanceof Repeat)) //Invalid situations: no nested elements or nested repetition
		                    		throw new GrammarException("Parsing error", "Ill-formed XML Grammar");
		                    	else {
		                    		Repeat repeat = (Repeat) pendingElements.remove(pendingElements.size()-1);
		                    		Object element = pendingElements.get(pendingElements.size()-1);
	                        		if(element instanceof Rule)
		                        		((Rule) element).addElement(repeat);
		                        	else if(element instanceof Alternative)
			                        	((Alternative) element).addAlternative(repeat);
		                        	else if(element instanceof Repeat)
		                        		((Repeat) element).add(repeat);
		                        }	
	                    		
	                    	}

	        			//</one-of>	
	                    } else if(tagname.equalsIgnoreCase("one-of")){
	                    	if(pendingElements.isEmpty() || !(pendingElements.get(pendingElements.size()-1) instanceof Alternative)) //Invalid situations: no nested elements or nested one-of
	                    		throw new GrammarException("Parsing error", "Ill-formed XML Grammar");
	                    	else {
	                    		Alternative alternative = (Alternative) pendingElements.remove(pendingElements.size()-1);
	                    		Object element = pendingElements.get(pendingElements.size()-1);
                        		if(element instanceof Rule)
	                        		((Rule) element).addElement(alternative);
	                        	else if(element instanceof Alternative)
		                        	((Alternative) element).addAlternative(alternative);
	                        	else if(element instanceof Repeat)
	                        		((Repeat) element).add(alternative);
	                        }		
	                    }
	                break;
	 
	                default:
	                    break;
	            }
	            
	            eventType = parser.next(); //May throw a XMLPullParseException or a IOException
	      }
	        
		} catch(XmlPullParserException ex){
			throw new GrammarException(ex.getMessage(), "Grammar could not be read, check the format");
		} catch(IOException ex){
			throw new GrammarException(ex.getMessage(), "Grammar could not be read, check Internet connection and accesibility of the URL");
		}   
	}
	
	/**
	 * Transforms the list of phrases obtained from parsing a XML grammar into a regular expression
	 * @return a String containing the regular expression
	 * @throws GrammarException
	 */
	private String computeRegularExpression() throws GrammarException{
		String result;
		
		for(Rule ph: rules.values())
			ph.setRegularExpression();				

		result = solveReferences();
		
		Log.i(LOGTAG, "Regular expression of the grammar: "+result);
		
		return result;
	}
	
	/** 
	 * It solves the references in a regular expression corresponding to a grammar
	 * @return Regular expression
	 * @throws GrammarException Parsing error
	 */
	private String solveReferences() throws GrammarException{
		Rule mainPhrase = rules.get(mainPhraseId);
		String regExpr = mainPhrase.getRegExpr();


		while(regExpr.contains("xxREFxx")){ //There might be several levels of references...
			
			//Solve reference in current level	
			Pattern p = Pattern.compile("(xxREFxx)(\\w+)(xx)");
			Matcher m = p.matcher(regExpr);

	        while(m.find())
	        {	
		        String id = m.group(2); //group 2 (group 0 is xxREFxxidxx, group 1 is xxREFxx, group 2 is id, group 3 is xx)
		        if(rules.get(id)==null)
		        	throw new GrammarException("Parsing error", "There is an incorrect rule reference in the grammar: "+id);
		        else{
		        	String exprUpToCurrentRef = regExpr.substring(0, regExpr.indexOf(m.group(0))); //Regular expression up to the current reference		        	
		        	int numOpenParenthesis = exprUpToCurrentRef.length() - exprUpToCurrentRef.replace("(", "").length(); //Number of parenthesis in the regular expression up to the current reference

		        	if(regExpr.indexOf(m.group(0))==regExpr.lastIndexOf(m.group(0))){ //Not to consider semantics for referenced rules (just inside the referee)
			        	if(rules.get(id).getSemanticTags(rules)!=null){
			        		SemanticParsing sp = new SemanticParsing(id, rules.get(id).getSemanticTags(rules), numOpenParenthesis+1); //+1 because the 0 position is for the whole sentence
			        		semantics.put(Integer.valueOf(sp.getPosition()), sp);
			        	}
		        	}	
			        String reference = rules.get(id).getRegExpr();
			        regExpr = regExpr.replace("xxREFxx"+id+"xx", "("+reference+")");
			        	
			        recalculateParenthesis(reference, numOpenParenthesis+1);
			        
					m = p.matcher(regExpr);
		        }
	        }
		}

        return regExpr;
	}
	
	/**
	 * Recalculates the positions of the groups in the regular expression when a rule reference is solved
	 * (see chapter 6)
	 */
	private void recalculateParenthesis(String reference, int referencePosition){
    	int numParenthesisInRef = reference.length() - reference.replace("(", "").length();
    	Object [] positions = semantics.keySet().toArray();
    	Arrays.sort(positions);
    	    
    	int i = positions.length-1;
    	int pos=(Integer) positions[i];
    			
    	while(i>=0 && pos>referencePosition){
        		SemanticParsing sp = semantics.get(pos);
        		semantics.remove(pos);
    			sp.setPosition(referencePosition+1+numParenthesisInRef);
    			semantics.put(sp.getPosition(), sp);
    			i--;
    			pos=(Integer) positions[i];
    	}	
	}
	
	/************************************************************************************
	 * METHODS TO VALIDATE A PHRASE AND OBTAIN ITS SEMANTIC REPRESENTATION
	 ************************************************************************************/
	
	/**
	 * Checks whether the <code>utterance</code> is valid according to the handcrafted grammar.
	 * If it is valid, it returns its semantic interpretation, if not it returns null
	 * @throws GrammarException It the grammar was not initialized in advance
	 */
	@SuppressLint("DefaultLocale")
	public String obtainSemantics(String utterance) throws GrammarException{
		if(grammarRegExpression==null) //Make sure that the grammar is initialized
			throw new GrammarException("Grammar not initialized","Attempt to use grammar without initializing it");
		else
		{
			String semantic = "";
			Pattern p = Pattern.compile(grammarRegExpression.replaceAll("\\s","").toLowerCase()); //To ignore whitespace and case
	        Matcher m = p.matcher(utterance.replaceAll("\\s","").toLowerCase()); //To ignore whitespace and case
	        
	        if(m.matches()) //If the phrase matches the regular expression of the grammar...
	        							//Beware not to use find instead of matches, matches accepts only exact match while find would accept "newyork" in "newyork areg"
	        {	//...then it obtains its semantic representation
	        	int numGroups = m.groupCount();
	        	for(int i=0; i<numGroups; i++){
	        		String sem = getMatchingSemanticTag(i, m.group(i));
	        		if(sem != null)
	        			semantic += sem+"\r\n";
	        	}	        	
	            return semantic;
	        }
	        else
	        	return null;
		}
	}
	
	/**
	 * Obtains the semantic tag for a recognized keyword in the phrase number <code>group</code>
	 */
	private String getMatchingSemanticTag(int group, String keyword){
		String match="";
		for(SemanticParsing sem: semantics.values()){
			String m = sem.matches(group, keyword);
			if(m!=null)
				match += m+" ";
		}
		
		if(match.equals(""))
			return null;
		else	
			return match;
	}


}
