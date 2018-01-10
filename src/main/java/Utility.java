
public class Utility {
    /**
     * 
     * @param hexString string to be prepended zeros
     * @param totalWidth the width of the result
     * @return the required string with prepended zeros 
     */
	public static String prependingZeros(String hexString, int totalWidth) {
    	String result="";
    	while(result.length()+hexString.length()<totalWidth) {
    		result=result+"0";
    	}
    	return result+hexString;
    		
    }
    
    /**
     * Helper function to join many byte arrays together.
     * @param a the first byte array
     * @param b the second byte array
     * @return the merged array
     */
    public static byte[] concatenateByteArrays(byte[]... arrays)
    {
        int count = 0;
        for (byte[] array: arrays)
        {
            count += array.length;
        }

        // Create new array and copy all array contents
        byte[] mergedArray = new byte[count];
        int start = 0;
        for (byte[] array: arrays) {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }
        return mergedArray;
    }
}
