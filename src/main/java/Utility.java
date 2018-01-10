
public class Utility {
    public static String prependingZeros(String hexString, int totalWidth) {
    	String result="";
    	while(result.length()+hexString.length()<totalWidth) {
    		result=result+"0";
    	}
    	return result+hexString;
    		
    }
}
