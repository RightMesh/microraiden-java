import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Wallet {
	private String accountName;
	private BigInteger nonce;
	private BigInteger etherBalance;
	private ECKey ecKeyPair;
	public Wallet(String _accountName) throws Exception {
		this.accountName=_accountName;
		this.ecKeyPair=getECKeyByName(accountName);
	}
	private BigInteger getNonce(Http httpAgent) throws IOException{
    	String queryNonceString="{\"method\":\"parity_nextNonce\",\"params\":[\"0x"+Hex.encodeHexString(ecKeyPair.getAddress())+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
    	String myNonceResult="";
    	try {
    		myNonceResult=httpAgent.getHttpResponse(queryNonceString);
        }catch (IOException e) {
        	throw e;
        }
		return new BigInteger(myNonceResult.substring(2),16);
	}
	private BigInteger getEtherBalance(Http httpAgent) throws IOException{
		String queryEtherBalanceString="{\"method\":\"eth_getBalance\",\"params\":[\"0x"+Hex.encodeHexString(ecKeyPair.getAddress())+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        //System.out.println("The request string in getEtherBalance is "+requestString);
        String myEtherBalance="";
    	try {
    		myEtherBalance=httpAgent.getHttpResponse(queryEtherBalanceString);
        }catch (IOException e) {
        	throw e;
        }
    	return new BigInteger(myEtherBalance.substring(2),16);
	}

	private ECKey getECKeyByName(String accountName) throws DecoderException,FileNotFoundException,ParseException,IOException{
    	JSONParser parser = new JSONParser();
    	JSONObject jobj=new JSONObject();
        try {     
        	jobj = (JSONObject)parser.parse(new FileReader(accountName+".pkey"));
            
        } catch (FileNotFoundException e) {
        	throw e;	
        } catch (ParseException e) {
        	throw e;
        } catch (IOException e) {
        	throw e;
        }
        try{
            return ECKey.fromPrivate(Hex.decodeHex(((String) jobj.get("privkey")).toCharArray()));
        }catch (DecoderException e) {
        	throw e;
        } 
    }
	public void update(Http httpAgent) throws IOException{
		try {
			nonce=getNonce(httpAgent);
			etherBalance=getEtherBalance(httpAgent);
		}catch(IOException e){
			throw e;
		}
	}
	public String getAccountID() {
		return "0x"+Hex.encodeHexString(ecKeyPair.getAddress());
	}
	
	public byte[] signMessage(byte[] message) {
		return ecKeyPair.sign(message).toByteArray();
	}
	public void signTransaction(Transaction trans) {
		trans.sign(ecKeyPair);
	}
	public BigInteger nonce() {
		return nonce;
	}
	public BigInteger etherBalance() {
		return etherBalance;
	}
	public String accountName() {
		return accountName;
	}
	public void updateNonce(Http httpAgent) throws IOException{
		try {
			nonce=getNonce(httpAgent);
		}catch(IOException e){
			throw e;
		}
	}
	public void updateEtherBalance(Http httpAgent) throws IOException{
		try {
			etherBalance=getEtherBalance(httpAgent);
		}catch(IOException e){
			throw e;
		}
	}

	
	
}
