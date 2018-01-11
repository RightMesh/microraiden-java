import java.math.BigInteger;

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
    
    public static BigInteger decimalToBigInteger(String balance, String template) throws  NumberFormatException{
    	try{
    		Double.parseDouble(balance);
    	}catch (NumberFormatException e) {
    		throw e;
    	}
    	BigInteger tempBalance=new BigInteger("0");
    	if(balance.indexOf(".")!=-1) {
	    	if(balance.length()-balance.indexOf(".")>19) {
	    		balance=balance.substring(0,balance.indexOf(".")+19);
	    	}
	    	String localAppendingZerosForTKN=template.substring(0, template.length()-balance.length()+1+balance.indexOf("."));
	    	tempBalance=new BigInteger(balance.replace(".", "")).multiply(new BigInteger(localAppendingZerosForTKN));
    	}else {
    		tempBalance=new BigInteger(balance,10).multiply(new BigInteger(template));
    	}
    	return tempBalance;
    }
    
	public static byte[] bigIntegerToBytes(BigInteger value) {
        if (value == null)
            return null;

        byte[] data = value.toByteArray();

        if (data.length != 1 && data[0] == 0) {
            byte[] tmp = new byte[data.length - 1];
            System.arraycopy(data, 1, tmp, 0, tmp.length);
            data = tmp;
        }
        return data;
    }
}
