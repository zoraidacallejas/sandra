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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a music album
 * 
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.2, 08/18/13
 */	
public class Album {

	private String interpreter; //Artist
	private String title;		//Title
	private Date date;			//Release date
	private String country;		//Release country
	private String label;		//Label
	
	
	public String getInterpreter() {
		return interpreter;
	}
	public void setInterpreter(String interpreter) {
		this.interpreter = interpreter;
	}
	public Date getDate() {
		return date;
	}
	
	/**
	 * Saves the release date as a Date object. In order to do so, it tries to parse the string
	 * read from MusicBrainZ. The format used by MusicBrainZ is a String "yyyy-MM-dd"
	 * (e.g. 1980-12-31) or "yyyy" (e.g. 1980). If the date cannot be parsed in any of these two formats,
	 * an exception is thrown
	 * @param dateString Release date in String format
	 */
	public void setDate(String dateString) throws ParseException {
	    try{
	    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	    	this.date =  format.parse(dateString);
	    }catch (ParseException ex){
	    	SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.ENGLISH);
	    	this.date =  format.parse(dateString);
	    }
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Custom equals method to compare Albums. It considers two albums to be the same 
	 * if they have the same title and interpreter
	 */
	@Override
	public boolean equals(Object other){
		if(other==null)
			return false;
		else {
			if(this.getTitle().equals(((Album) other).getTitle()) && 
					this.getInterpreter().equals(((Album) other).getInterpreter()) )
				return true;
			else
				return false;
		}
	}
}