package sandra.examples.oneshot.voicelaunch;

/**
 * From: http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
 * @author Wikipedia
 * @version Retrieved July 2013
 *
 */
class LevenshteinDistance {

        private static int minimum(int a, int b, int c) {
                return Math.min(Math.min(a, b), c);
        }
 
        private static int levenshteinDistance(CharSequence str1, CharSequence str2) {
                int[][] distance = new int[str1.length() + 1][str2.length() + 1];
 
                for (int i = 0; i <= str1.length(); i++)
                        distance[i][0] = i;
                for (int j = 1; j <= str2.length(); j++)
                        distance[0][j] = j;
 
                for (int i = 1; i <= str1.length(); i++)
                        for (int j = 1; j <= str2.length(); j++)
                                distance[i][j] = minimum(
                                                distance[i - 1][j] + 1,
                                                distance[i][j - 1] + 1,
                                                distance[i - 1][j - 1]
                                                                + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                                                                : 1));
 
                return distance[str1.length()][str2.length()];
        }
        
        //Our addition to wikipedia's code in order to obtain the distance as a similarity measure from 0 to 1
        static double computeLevenshteinDistance(String a, String b){
        	double distance = levenshteinDistance(a,b);
        	double normalizedDistance = distance/Math.max(a.length(), b.length());	
        	return (1 - normalizedDistance);
        }
        
}

