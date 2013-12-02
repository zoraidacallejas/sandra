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

package sandra.examples.formfill.musicbrain;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.TreeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Parses a XML file with the response of a release search in MusicBrainZ
 * @see http://musicbrainz.org/doc/Development/XML_Web_Service/Version_2/Search#Release
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 2.0, 08/17/13
 */

public class MusicBrainParser{

    /**
     * Parses the results of the xml file into a sorted collection of Albums (see <code>Album.java</code>) sorted without duplicates. 
     * @param xml file containing the response of the MusicBrainZ web service
     * @see http://musicbrainz.org/doc/Development/XML_Web_Service/Version_2/Search#Release
     * @result collection of Albums sorted from most to least recent release date
     */
	static TreeSet<Album> parse(String xmlContent) throws XmlPullParserException, MusicBrainException{
		
		Album album = null;
        String tag=null;
        String tagContents=null;
        
        /*
         * The XML response from MusicBrain contains duplicates, when inserting them in the TreeSet, they will not be copied more than once. 
         * TreeSet uses the equal method to compare the new albums inserted in the collection, thus we have added our own code for "equal" in the Album class, 
         * so that two albums are considered equal if they have the same title and interpreter.
         * 
         * To sort the albums, we provide a custom comparator when creating the TreeSet (see AlbumComparator class) so that the albums are saved in order 
         * from most to least recent release date.
         */
        
        TreeSet<Album> albumList = new TreeSet<Album>(new AlbumComparator());
        
        int eventType;
                	
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  //May throw XMLPullParserException
		XmlPullParser parser = factory.newPullParser(); 					//May throw XMLPullParserException
		StringReader xmlReader = new StringReader(xmlContent);				
		
		try{
			parser.setInput(xmlReader); //We are reading the xml from a string, but we could read it directly from the streaminput
		}
		catch(XmlPullParserException ex){
			throw new MusicBrainException(ex.getMessage(), "VXML not accessible, check Internet connection and accesibility of the URL");
		}

		try{
			eventType = parser.getEventType();	//May throw a XMLPullParserException
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	                
	        	String tagname = parser.getName();
	            	
	            switch (eventType) {
		        	case XmlPullParser.START_TAG:
		        		
		        		if (tagname.equalsIgnoreCase("release")){
		        			album = new Album();
		        		} else if(tagname.equalsIgnoreCase("artist")) {
	        				tag="artist";
		        		} else if(tagname.equalsIgnoreCase("area")) {
	        				tag="area";
		        		} else if(tagname.equalsIgnoreCase("label")) {
	        				tag="label";
		        		}
	                break;
	 
	                case XmlPullParser.TEXT:
	                    tagContents = parser.getText();
	                break;
	 
	                case XmlPullParser.END_TAG:
	                	if(tagname.equalsIgnoreCase("release")) {
	                		albumList.add(album);
	            		} else if (tagname.equalsIgnoreCase("title")) {
	                        album.setTitle(tagContents);
	            		} else if (tagname.equalsIgnoreCase("date")) {
	                        album.setDate(tagContents);	//May throw ParseException
	                    } else if (tagname.equalsIgnoreCase("name") && tag.equals("artist")) {
	                        album.setInterpreter(tagContents);
	                    }else if (tagname.equalsIgnoreCase("name") && tag.equals("area")) {
	                        album.setCountry(tagContents);
	                    }else if (tagname.equalsIgnoreCase("name") && tag.equals("label")) {
	                        album.setLabel(tagContents);
	                    }
	                break;
	 
	                default:
	                    break;
	            }
	            
	            eventType = parser.next(); //May throw IOException or XMLPullParserException
	      }	
		} catch(XmlPullParserException ex){
			throw new MusicBrainException(ex.getMessage(), "VXML could not be read, check Internet connection and accesibility of the URL");
		} catch(IOException ex){
			throw new MusicBrainException(ex.getMessage(), "VXML could not be read, check Internet connection and accesibility of the URL");
		} catch(ParseException ex){
			throw new MusicBrainException(ex.getMessage(), "The dates retrieves from MusicBrainZ could not be parsed");
		}
		return albumList;
		
	}

		
}
