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

import java.util.Comparator;
import java.util.Date;

/**
* Custom comparator for Album objects.
* 
* @author Zoraida Callejas
* @author Michael McTear
* @version 1.2, 08/18/13
*/	
class AlbumComparator implements Comparator<Album>{
	 
	/**
	 * Compares Albums by date. It can be used to sort albums from most to least recent release date
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
    @Override
    public int compare(Album album1, Album album2) {
		Date date1 = album1.getDate();
		Date date2 = album2.getDate();
		
		if(date1==null || date2==null)
			return -1;
		else
		{
	    	if (album1.getDate().before(album2.getDate()))
				return 1;
			else if (album1.getDate().after(album2.getDate()))
				return -1;
			else
				return 0;
    
		}
    }
} 
