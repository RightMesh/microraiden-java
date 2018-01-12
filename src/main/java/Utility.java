import java.math.BigInteger;

import org.kocakosm.pitaya.security.Digest;
import org.kocakosm.pitaya.security.Digests;

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
    
    /**
     * Convert double number literal to BigInteger according to the decimal unit template. 
     * For example, the Ether has 18 decimal units, the template should be 1000000000000000000. 
     * The function can convert 3.141592653589793238 to 3141592653589793238 (BigInteger).
     * @param balance double number in literal
     * @param template literal starts with 1, followed by the number (decimal units) of zeros behind.
     * @return The BigInteger after the conversion.
     * @throws NumberFormatException
     */
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
    
    /**
     * Convert BigInteger to the byte array used as arguments of calling smart contract functions.
     * @param value the BigInteger to be changed to byte array.
     * @return the byte array sent to contract functions.
     */
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
	
	/**
	 * Calculate the SHA3 (a.k.a. Keccak) hash value of messageToBeHashed
	 * @param messageToBeHashed the message to be hashed in byte array
	 * @return
	 */
	public static byte[] getSHA3HashHex(byte[] messageToBeHashed) {
        Digest keccak256 = Digests.keccak256();
        keccak256.reset();
        keccak256.update(messageToBeHashed);
        return keccak256.digest();
    }
}
