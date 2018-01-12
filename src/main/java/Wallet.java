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

/**
 * 
 * @author david
 * This class creates a wallet with the account name.
 */
public class Wallet {
	private String accountName;
	private BigInteger nonce;
	private BigInteger etherBalance;
	private ECKey ecKeyPair;
	
	/**
	 * Create a wallet with the account name
	 * @param _accountName the account name used to retrieve the wallet.
	 * @throws Exception
	 */
	public Wallet(String _accountName) throws Exception {
		this.accountName=_accountName;
		this.ecKeyPair=getECKeyByName(accountName);
	}
	
	/**
	 * Get the nonce of the account
	 * @param httpAgent the HTTP agent used to get the nonce from a running peer via the RPC
	 * @return the nonce in BigInteger
	 * @throws IOException
	 */
	private BigInteger getNonce(Http httpAgent) throws IOException{
    	String queryNonceString="{\"method\":\"parity_nextNonce\",\"params\":[\"0x"+Hex.encodeHexString(ecKeyPair.getAddress())+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
    	String myNonceResult="";
    	try {
    		myNonceResult=(String)httpAgent.getHttpResponse(queryNonceString);
        }catch (IOException e) {
        	throw e;
        }
		return new BigInteger(myNonceResult.substring(2),16);
	}
	
	/**
	 * Get the balance of ether on the Ethereum network.
	 * @param httpAgent the HTTP agent used to get the Ether balance from a running peer via the RPC
	 * @return the Ether balance in BigInteger
	 * @throws IOException
	 */
	private BigInteger getEtherBalance(Http httpAgent) throws IOException{
		String queryEtherBalanceString="{\"method\":\"eth_getBalance\",\"params\":[\"0x"+Hex.encodeHexString(ecKeyPair.getAddress())+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        //System.out.println("The request string in getEtherBalance is "+requestString);
        String myEtherBalance="";
    	try {
    		myEtherBalance=(String)httpAgent.getHttpResponse(queryEtherBalanceString);
        }catch (IOException e) {
        	throw e;
        }
    	return new BigInteger(myEtherBalance.substring(2),16);
	}

	/**
	 * Retrieve the wallet by using the account name.
	 * @param accountName name of the account used to get the wallet
	 * @return the key pair of elliptic curve
	 * @throws DecoderException
	 * @throws FileNotFoundException
	 * @throws ParseException
	 * @throws IOException
	 */
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
	
	/**
	 * Update both nonce and etherBalance via RPC
	 * @param httpAgent httpAgent the HTTP agent used to get account information from a running peer via the RPC
	 * @throws IOException
	 */
	public void update(Http httpAgent) throws IOException{
		try {
			nonce=getNonce(httpAgent);
			etherBalance=getEtherBalance(httpAgent);
		}catch(IOException e){
			throw e;
		}
	}
	
	/**
	 * Get the account ID in Hex with "0x" prefix.
	 * @return the AccountID in HEX format with "0x" prefix
	 */
	public String getAccountID() {
		return "0x"+Hex.encodeHexString(ecKeyPair.getAddress());
	}
	
	/**
	 * Sign a message with this account's private key
	 * @param message the message in byte array to be signed
	 * @return the signature
	 */
	public byte[] signMessage(byte[] message) {
		return ecKeyPair.sign(message).toByteArray();
	}
	
	/**
	 * Sign a transaction with this account pricate key
	 * @param trans transaction to be signed
	 */
	public void signTransaction(Transaction trans) {
		trans.sign(ecKeyPair);
	}
	
	/**
	 * Get the nonce of this account.
	 * @return the nonce of this account in Ethereum network.
	 */
	public BigInteger nonce() {
		return nonce;
	}
	
	/**
	 * Get Ether balance of this account
	 * @return the balance in Ether of this account.
	 */
	public BigInteger etherBalance() {
		return etherBalance;
	}
	
	/**
	 * Get name of this account
	 * @return account name of this account.
	 */
	public String accountName() {
		return accountName;
	}
	
	/**
	 * Update nonce of this account via RPC
	 * @param httpAgent the HTTP agent send the RPC request
	 * @throws IOException
	 */
	public void updateNonce(Http httpAgent) throws IOException{
		try {
			nonce=getNonce(httpAgent);
		}catch(IOException e){
			throw e;
		}
	}
	
	/**
	 * Update ether balance of this account via RPC
	 * @param httpAgent the HTTP agent send the RPC request
	 * @throws IOException
	 */
	public void updateEtherBalance(Http httpAgent) throws IOException{
		try {
			etherBalance=getEtherBalance(httpAgent);
		}catch(IOException e){
			throw e;
		}
	}

	
	
}
