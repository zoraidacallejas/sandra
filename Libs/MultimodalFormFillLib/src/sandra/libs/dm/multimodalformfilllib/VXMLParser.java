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

package sandra.libs.dm.multimodalformfilllib;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


import android.content.Context;

/**
 * Parses the contents of a VXML file and creates a Form object with a collection of Field objects.
 * 
 * It is a version of the class <code>sandra.libs.dm.formfilllib.VXMLParser</code> (chapter 5, FormFillLib project).
 * Changes: parses also a <grammar> element in the method <code>parseVXML</code> method.
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.3, 08/18/13
 *
 */
public class VXMLParser{

	/**
	 * Returns the attributes of the current tag (e.g. field name), or null if it has no attributes.
	 * The parser must be placed in the current tag before invoking this method
	 * 
	 * Idea from: http://stackoverflow.com/questions/4827168/how-to-parse-the-value-in-the-attribute-in-xml-parsing (code changed)
	 */
	private static HashMap<String,String> getAttributes(XmlPullParser parser) {
		
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
	 * Parses the string corresponding to a VXML file into a Form and several Fields.
	 * @param vxmlContent The contents of the file, which must be in the following format:
	 * 
	 * <form id = "flight">
	 * 		<field name="destination">
	 * 			<prompt>where would you like to travel to?</prompt>
	 * 			<nomatch> I did not understand your destination </nomatch>
	 * 			<noinput> I am sorry, I did not hear your clearly </noinput>
	 * 			<grammar src="http://myweb.com/grammars/destination_grammar.xml"/>
	 * 		</field>
	 * 		
	 * 		<field name="date">
	 * 			<prompt>what day would you like to travel?</prompt>
	 * 			<nomatch> I did not understand the date </nomatch>
	 * 			<noinput> I am sorry, I did not hear your clearly </noinput>
	 * 			<grammar src="http://myweb.com/grammars/date_grammar.xml"/>
	 * 		</field>
	 * </form> 
	 * 
	 * The nesting of the tags must be strictly as in the example, though the order
	 * of the prompt, grammar, nomatch and noinput elements inside a field is not important
	 * 
	 * The prompt field is mandatory. 
	 * 
	 * If there are more than 1 form in the file, only the last one will be taken into account. 
	 * 
	 * @param ctx Context of the application. It is necessary in order to access the assets folder to retrieve grammars in case they are stored there and not in the Internet
	 * @throws Exception If there are errors during parsing, mainly because of ill-formed files that do not follow the previous indications
	 */
	public static Form parseVXML(String vxmlContent, Context ctx) throws XmlPullParserException, MultimodalException {
				
		Form form = null;
        Field field = null;
        String tagContents = null;
        HashMap<String, String> attributes = null;
        int eventType;
        
        
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); //May throw XmlPullParserException
		XmlPullParser parser = factory.newPullParser();					   //May throw XmlPullParserException

		
		StringReader xmlReader = new StringReader(vxmlContent);
		
		try{
			parser.setInput(xmlReader); //We are reading the xml from a string, but we could read it directly from the streaminput
		}
		catch(XmlPullParserException ex){
			throw new MultimodalException(ex.getMessage(), "VXML not accessible, check Internet connection and accesibility of the URL");
		}

		try{
			eventType = parser.getEventType();	//May throw a XMLPullParserException
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	                
	        	String tagname = parser.getName();
	            	
	            switch (eventType) {
		        	case XmlPullParser.START_TAG:
		        		
		        		if (tagname.equalsIgnoreCase("field")){
		        			field = new Field();
		                    attributes = getAttributes(parser);
		                    field.setName(attributes.get("name"));
		                    
	                    } else if (tagname.equalsIgnoreCase("grammar")){
	                    	attributes = getAttributes(parser);
	                    	if(attributes.get("src")!=null)
	                    		field.setGrammar(attributes.get("src"), ctx);		        		
	            		} else if (tagname.equalsIgnoreCase("form"))
	                        form = new Form();
	                break;
	 
	                case XmlPullParser.TEXT:
	                    tagContents = parser.getText();
	                break;
	 
	                case XmlPullParser.END_TAG:
	                    if (tagname.equalsIgnoreCase("prompt")) {
	                        field.setPrompt(tagContents);
	                    } else if (tagname.equalsIgnoreCase("nomatch")){
	                    	field.setNomatch(tagContents);
	                    } else if (tagname.equalsIgnoreCase("noinput")){
	                    	field.setNoinput(tagContents);
	                    } else if (tagname.equalsIgnoreCase("field")){
	                    	form.addField(field);	//May throw a FormFillLibException if the field is not complete
	                    }
	                break;
	 
	                default:
	                    break;
	            }
	            
	            eventType = parser.next(); //May throw a XMLPullParseException or a IOException
	      }
	        
		} catch(XmlPullParserException ex){
			throw new MultimodalException(ex.getMessage(), "VXML could not be read, check Internet connection and accesibility of the URL");
		} catch(IOException ex){
			throw new MultimodalException(ex.getMessage(), "VXML could not be read, check Internet connection and accesibility of the URL");
		}    
     return form;
	}
		
}
