import java.lang.reflect.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * CLI for the MicroRaiden client
 */
public class MicroRaiden {
	private static final String rpcAddress="http://localhost:8545";
	private static final String channelManagerAddr="0x4913f12d38c04094cF1E382d0ffEEf3036eCCa32";
	private static final String customTokenAddr="0x0fC373426c87F555715E6fE673B07Fe9E7f0E6e7";
	private static final String toAddr="0xF4ABFf26965D10E2162d26FEaf7E16349fA201fF";
	private static final String tokenAbi="[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"mint\",\"outputs\":[],\"payable\":true,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"multiplier\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint8\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"transferFunds\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"version\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner_address\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"},{\"name\":\"_data\",\"type\":\"bytes\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"},{\"name\":\"_spender\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[{\"name\":\"initial_supply\",\"type\":\"uint256\"},{\"name\":\"token_name\",\"type\":\"string\"},{\"name\":\"token_symbol\",\"type\":\"string\"},{\"name\":\"decimal_units\",\"type\":\"uint8\"}],\"payable\":false,\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_num\",\"type\":\"uint256\"}],\"name\":\"Minted\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
    private static final String appendingZerosForETH="1000000000000000000";
    private static final String appendingZerosForRMT="1000000000000000000";
    private static BigInteger gasPrice=new BigInteger("5000000000");
    private static final int INTERVAL_CHECK_TRANS_DONE=5000;
    private static boolean running=false;
    private String mytransactionID = null;
    private final Object lock = new Object();

    public void newTransactionID(String id) {
        synchronized (lock) {
        	mytransactionID = id;
        }
    }

    public String getTransactionID() {
        synchronized (lock) {
            String temp = mytransactionID;
            mytransactionID = null;
            return temp;
        }
    }
   
	public MicroRaiden() {
        //should probably create an eth account with priv / pub keys
        //for doing the signing in the constructor
        
        //another option is we load the account based on what is
        //already saved in the folder
        
        //we need to think about what we should do if the sender / recv
        //are located in the same folder, or different folders and how
        //to store the files
    }
    
    /**
     * Create a new ethereum account to be used for testing microraiden
     * channels. Stores the account in the same folder where the
     * program is run. Note - there is no encryption on this private key
     * so it should be used for anything real!!
     * @param accountFile - the name of the output file for the account
     */
    public void createAccount(String accountFile) {
        ECKey keyPair = new ECKey();
        String address = new String(Hex.encodeHex(keyPair.getAddress()));
        System.out.println("Generated new account: 0x" + address);
        byte[] priv = keyPair.getPrivKeyBytes();
        
        try {
            OutputStream os = new FileOutputStream(accountFile + ".pkey");
            JSONObject obj=new JSONObject();
            obj.put("privkey", new String(Hex.encodeHex(priv)));
            obj.put("address", address);
            os.write(obj.toJSONString().getBytes());
            os.close();
        } catch (IOException e) {
            System.out.println("Couldn't write to file: " + accountFile + " " + e.toString());
        }
    }
    
    public void createAccountByPrivateKey(String accountFile, String privateKeyHex) {
    	if(privateKeyHex.startsWith("0x")) {
    		privateKeyHex=privateKeyHex.substring(2);
    	}
        ECKey keyPair = new ECKey();
        try{
        	keyPair=ECKey.fromPrivate(Hex.decodeHex(privateKeyHex.toCharArray()));
        }catch(DecoderException e) {
        	System.out.println("Couldn't create ECKey with privateKeyHex = " + privateKeyHex);
        }
        String address = new String(Hex.encodeHex(keyPair.getAddress()));
        System.out.println("Generated new account: 0x" + address);
        byte[] priv = keyPair.getPrivKeyBytes();
        
        try {
            OutputStream os = new FileOutputStream(accountFile + ".pkey");
            JSONObject obj=new JSONObject();
            obj.put("privkey", new String(Hex.encodeHex(priv)));
            obj.put("address", address);
            os.write(obj.toJSONString().getBytes());
            os.close();
        } catch (IOException e) {
            System.out.println("Couldn't write to file: " + accountFile + " " + e.toString());
        }
    }
    
    public void loadAccountAndSignRecoverable(String accountName, String msgHashHex){
    	if(msgHashHex.startsWith("0x")) {
    		msgHashHex=msgHashHex.substring(2);
    	}
    	byte [] msgHashBytes=new byte[0];
    	try {
    		msgHashBytes=Hex.decodeHex(msgHashHex.toCharArray());
    	}catch (DecoderException e) {
        	System.out.println("Couldn't convert msgHashHex = " + msgHashHex + " to byte array.");
        } 
    	JSONParser parser = new JSONParser();
    	ECKey keyPair = new ECKey();
    	String privateKeyHex=new String();
        try {     
            Object obj = parser.parse(new FileReader(accountName+".pkey"));

            JSONObject jsonObject =  (JSONObject) obj;

            privateKeyHex = (String) jsonObject.get("privkey");
            keyPair=ECKey.fromPrivate(Hex.decodeHex(privateKeyHex.toCharArray()));
            
        } catch (FileNotFoundException e) {
        	System.out.println("Couldn't locate account file " + accountName + ".pkey");
        } catch (ParseException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        } catch (DecoderException e) {
        	System.out.println("Couldn't create ECKey with privateKeyHex = " + privateKeyHex);
        } catch (IOException e) {
        	
        }
        String signature=keyPair.sign(msgHashBytes).toHex();
        System.out.println("The signed recoverable signature is 0x" + signature + ".");
    }
    /**
     * 
     */
    public void signMessageRecoverable(String privateKey, String message) {
    	
    }
    /**
     * Reads all of the account files in the directory and lists which
     * are usable for testing channels with. Looks for files with the
     * .pkey extension.
     */
    public void listAccounts() {
        File dir = new File(".");
        File[] filesList = dir.listFiles();
        for (File file : filesList) {
            if (file.isFile()) {
                if(file.getName().contains(".pkey")) {
                    //System.out.println(file.getName());
                    try {
                        InputStream is = new FileInputStream(file.getName());
                        byte[] priv = IOUtils.toByteArray(is);
                        is.close();
                        ECKey keyPair = ECKey.fromPrivate(priv);
                        String address = new String(Hex.encodeHex(keyPair.getAddress()));
                        System.out.println("0x" + address);
                    } catch(IOException ex) {
                        System.out.println("Couldn't read from file: " + file.getName() + " " + ex.toString());
                    }
                }
            }
        }
    }
    
    /**
     * Lists the balances for a particular account and any balance
     * in the channel with the given remote peer
     */
    public void listBalances(String account, String remotePeer) {
        
    }
    
    public void createChannel(String senderAccount, String receiverAccount, String deposit) {
        System.out.println("CREATE CHANNEL");
    }
    
    public void getTokenBalance(String accountName){
    	JSONParser parser = new JSONParser();
    	JSONObject jobj=new JSONObject();
    	ECKey keyPair = new ECKey();
        try {     
        	jobj = (JSONObject)parser.parse(new FileReader(accountName+".pkey"));
            
        } catch (FileNotFoundException e) {
        	System.out.println("Couldn't locate account file " + accountName + ".pkey");
        	return;
        } catch (ParseException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return;
        } catch (IOException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return;
        }
        String  privateKeyHex = (String) jobj.get("privkey");
        try{
            keyPair=ECKey.fromPrivate(Hex.decodeHex(privateKeyHex.toCharArray()));
        }catch (DecoderException e) {
        	System.out.println("Couldn't create ECKey with privateKeyHex = " + privateKeyHex);
        	return;
        } 
        
        String address = "0x"+new String(Hex.encodeHex(keyPair.getAddress()));
    	CallTransaction.Contract contract = new CallTransaction.Contract(tokenAbi);
        CallTransaction.Function balanceOf = contract.getByName("balanceOf");
        byte [] functionBytes=balanceOf.encode(address);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(rpcAddress);
        String requestString = "{\"method\":\"eth_call\"," +
                "\"params\":[" +
                "{" +
                "\"to\":\""+customTokenAddr+"\"," +
                "\"data\":\""+"0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(functionBytes))+"\"" +
                "}," +
                "\"latest\"" +
                "]," +
                "\"id\":42,\"jsonrpc\":\"2.0\"}";
      //System.out.println("The request string in getTokenBalance is "+requestString);
    	String myTokenBalance="";
    	try {
            StringEntity params = new StringEntity(requestString);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            System.out.println("About to execute transaction via RPC: "+request.getURI().toString());
            CloseableHttpResponse response = httpClient.execute(request);
            System.out.println("Finished to execute transaction.");
            myTokenBalance=new BasicResponseHandler().handleResponse(response);
            response.close();
            httpClient.close();
            //System.out.println("The response from RPC is "+response+".");
            jobj=(JSONObject)parser.parse(myTokenBalance);
            //System.out.println("The JSON Object is "+jobj.toJSONString());
            for (Object key1 : jobj.keySet()) {
                if (((String)key1).equalsIgnoreCase("result")) {
                	myTokenBalance=(String) jobj.get(key1);
                }
            }
        }catch (UnsupportedEncodingException e) {
        	System.out.println("UnsupportedEncodingException: "+e);
        }catch (ClientProtocolException e) {
        	System.out.println("ClientProtocolException: "+e);
        }catch (IOException e) {
        	System.out.println("IOException: "+e);
        }catch(ParseException e) {
        	System.out.println("ParseException: "+e);
        }catch (NumberFormatException e){
        	System.out.println("NumberFormatException="+e);
        }
    	System.out.println("Token balance of "+accountName+"("+address+")"+" is "+new Float(new BigInteger(myTokenBalance.substring(2),16).floatValue()/(new BigInteger(appendingZerosForETH,10).floatValue())).toString()+" TKN");
    	
    }
    
    public void buyToken(String accountName,String amountOfEther){
    	JSONParser parser = new JSONParser();
    	JSONObject jobj=new JSONObject();
    	ECKey keyPair = new ECKey();
        try {     
        	jobj = (JSONObject)parser.parse(new FileReader(accountName+".pkey"));
            
        } catch (FileNotFoundException e) {
        	System.out.println("Couldn't locate account file " + accountName + ".pkey");
        	return;
        } catch (ParseException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return;
        } catch (IOException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return;
        }
        String  privateKeyHex = (String) jobj.get("privkey");
        try{
            keyPair=ECKey.fromPrivate(Hex.decodeHex(privateKeyHex.toCharArray()));
        }catch (DecoderException e) {
        	System.out.println("Couldn't create ECKey with privateKeyHex = " + privateKeyHex);
        	return;
        } 
        
        String address = "0x"+new String(Hex.encodeHex(keyPair.getAddress()));
    	try{
    		Double.parseDouble(amountOfEther);
    	}catch (NumberFormatException e) {
    		System.out.println("The numer format is wrong.");
    		return;
    	}
    	
    	if(amountOfEther.length()-amountOfEther.indexOf(".")>19) {
    		amountOfEther=amountOfEther.substring(0,amountOfEther.indexOf(".")+19);
    	}
    	String localAppendingZerosForETH=appendingZerosForETH.substring(0, appendingZerosForETH.length()-amountOfEther.length()+1+amountOfEther.indexOf("."));
    	BigInteger value=new BigInteger(amountOfEther.replace(".", "")).multiply(new BigInteger(localAppendingZerosForETH));
    	System.out.println("User "+accountName+"("+address+") will trade "+value.toString()+" Wei to Token.");
       	
       	CallTransaction.Contract contract = new CallTransaction.Contract(tokenAbi);
        CallTransaction.Function mint = contract.getByName("mint");
        byte [] functionBytes=mint.encode();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(rpcAddress);     
        request.addHeader("content-type", "application/json");
        
        
        String queryGasString = "{\"method\":\"eth_estimateGas\"," +
                "\"params\":[" +
                "{" +
                "\"from\":\""+address+"\"," +
                "\"to\":\""+customTokenAddr+"\"," +
                "\"value\":\""+"0x"+value.toString(16)+"\","+
                "\"data\":\""+"0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(functionBytes))+"\"" +
                "}" +
                "]," +
                "\"id\":42,\"jsonrpc\":\"2.0\"}";
        String gasEstimateResult="";
    	try {
    		request.setEntity(new StringEntity(queryGasString));            
            CloseableHttpResponse response = httpClient.execute(request);            
            String temp=new BasicResponseHandler().handleResponse(response);
            response.close();
            jobj=(JSONObject)parser.parse(temp);
            for (Object key : jobj.keySet()) {
                if (((String)key).equalsIgnoreCase("result")) {
                	gasEstimateResult=(String) jobj.get(key);
                }
            }
        }catch (UnsupportedEncodingException e) {
        	System.out.println("UnsupportedEncodingException: " + e);
        }catch (ClientProtocolException e) {
        	System.out.println("ClientProtocolException: "+ e);
        }catch (IOException e) {
        	System.out.println("IOException: " + e);
        }catch(ParseException e) {
        	System.out.println("ParseException: " + e);
        }catch (NumberFormatException e){
        	System.out.println("NumberFormatException=" + e);
        }
    	if("".equals(gasEstimateResult)) {
    		System.out.println("Invoking function with given arguments is not allowed.");
    		return;
    	}
    	
    	String queryTokenBalanceString="{\"method\":\"eth_getBalance\",\"params\":[\""+address+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
    	String myEtherBalance="";
    	try {
            StringEntity params = new StringEntity(queryTokenBalanceString);
            request.setEntity(params);            
            CloseableHttpResponse response = httpClient.execute(request);            
            String temp=new BasicResponseHandler().handleResponse(response);
            response.close();
            jobj=(JSONObject)parser.parse(temp);
            for (Object key : jobj.keySet()) {
                if (((String)key).equalsIgnoreCase("result")) {
                	myEtherBalance=(String) jobj.get(key);
                }
            }
        }catch (UnsupportedEncodingException e) {
        	System.out.println("UnsupportedEncodingException: " + e);
        }catch (ClientProtocolException e) {
        	System.out.println("ClientProtocolException: " + e);
        }catch (IOException e) {
        	System.out.println("IOException: "+ e);
        }catch(ParseException e) {
        	System.out.println("ParseException: "+ e);
        }catch (NumberFormatException e){
        	System.out.println("NumberFormatException=" + e);
        }
    	if(new BigInteger(gasEstimateResult.substring(2),16).multiply(gasPrice).add(value).compareTo(new BigInteger(myEtherBalance.substring(2),16))>0) {
    		System.out.println("Insufficient Ether to finish the transaction.");
    		return;
    	}
    	String queryNonceString="{\"method\":\"parity_nextNonce\",\"params\":[\""+address+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
    	String myNonceResult="";
        try{
            StringEntity params = new StringEntity(queryNonceString);
            request.setEntity(params);            
            CloseableHttpResponse response = httpClient.execute(request);            
            String temp=new BasicResponseHandler().handleResponse(response);
            response.close();           
            jobj=(JSONObject)parser.parse(temp);
            for (Object key : jobj.keySet()) {
                if (((String)key).equalsIgnoreCase("result")) {
                	myNonceResult=(String) jobj.get(key);
                }
            }
        }catch (UnsupportedEncodingException e) {
        	System.out.println("UnsupportedEncodingException: "+e);
        }catch (ClientProtocolException e) {
        	System.out.println("ClientProtocolException: "+e);
        }catch (IOException e) {
        	System.out.println("IOException: "+e);
        }catch(ParseException e) {
        	System.out.println("ParseException: "+e);
        }catch (NumberFormatException e){
        	System.out.println("NumberFormatException="+e);
        }
        Transaction t = new Transaction(bigIntegerToBytes(new BigInteger(myNonceResult.substring(2),16)), // nonce
                bigIntegerToBytes(gasPrice), // gas price
                bigIntegerToBytes(new BigInteger(gasEstimateResult.substring(2),16)), // gas limit
                ByteUtil.hexStringToBytes(customTokenAddr), // to id
                bigIntegerToBytes(value), // value
                functionBytes, 42);// chainid
        t.sign(keyPair);
        String signedTrans = "0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(t.getEncoded()));
        String mintSendRawTransactionString = "{\"method\":\"eth_sendRawTransaction\",\"params\":[\""
                + signedTrans + "\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        String myTransactionID="";
        try {
            StringEntity params = new StringEntity(mintSendRawTransactionString);
            request.setEntity(params);
            CloseableHttpResponse response = httpClient.execute(request);
            String temp = new BasicResponseHandler().handleResponse(response);
            response.close();
            httpClient.close();
            jobj=(JSONObject)parser.parse(temp);
            for (Object key : jobj.keySet()) {
                if (((String)key).equalsIgnoreCase("result")) {
                	myTransactionID=(String) jobj.get(key);
                }
            }
        } catch (UnsupportedEncodingException e) {
        	System.out.println("UnsupportedEncodingException: "+e);
        }catch (ClientProtocolException e) {
        	System.out.println("ClientProtocolException: "+e);
        }catch (IOException e) {
        	System.out.println("IOException: "+e);
        }catch(ParseException e) {
        	System.out.println("ParseException: "+e);
        }catch (NumberFormatException e){
        	System.out.println("NumberFormatException="+e);
        }
        if(!"".equals(myTransactionID)) {
        	System.out.println("Transaction broadcast to Kovan with ID = "+myTransactionID);
        	newTransactionID(myTransactionID);
        	new Thread() {
        		private String pendingTransactionID=getTransactionID();
        		private boolean loop=true;
        		public void run(){
                    while(loop){
                    	Object tempObj=null;
                    	try {
	                        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	                        HttpPost request = new HttpPost(rpcAddress);     
	                        request.addHeader("content-type", "application/json");
	                        String queryTransactionString = "{\"method\":\"eth_getTransactionReceipt\"," +
	                                "\"params\":[\"" +
	                                pendingTransactionID +
	                                "\"]," +
	                                "\"id\":42,\"jsonrpc\":\"2.0\"}";
	                        StringEntity params = new StringEntity(queryTransactionString);
	                        request.setEntity(params);
	                        CloseableHttpResponse response = httpClient.execute(request);
	                        String temp =new BasicResponseHandler().handleResponse(response);
	                        response.close();
	                        httpClient.close();
	                      
	                        JSONParser parser = new JSONParser();
	                        JSONObject jobj=(JSONObject)parser.parse(temp);
                            for (Object key : jobj.keySet()) {
                                if (((String)key).equalsIgnoreCase("result")) {
                                	tempObj=jobj.get(key);
                                }
                            }
                        }catch (UnsupportedEncodingException e) {
                        	System.out.println("UnsupportedEncodingException: "+e);
                        }catch (ClientProtocolException e) {
                        	System.out.println("ClientProtocolException: "+e);
                        }catch (IOException e) {
                        	System.out.println("IOException: "+e);
                        }catch(ParseException e) {
                        	System.out.println("ParseException: "+e);
                        }catch (NumberFormatException e){
                        	System.out.println("NumberFormatException="+e);
                        }
                        if(tempObj==null){
                            //do nothing
                        }else{
                        	loop=false;
                            JSONObject jsonObject=(JSONObject) tempObj;
                            //jsonObject can be further parsed to get more information.
                            System.out.println("Transaction "+pendingTransactionID+" has been successfully mint.");
                        }
                        try {
                        	Thread.sleep(INTERVAL_CHECK_TRANS_DONE);
                        }catch (InterruptedException e) {
                        	
                        }
                    }
                }
        	}.start();
        }

    	
    }
    
    private static byte[] bigIntegerToBytes(BigInteger value) {
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
    
    public void getEtherBalance(String accountName){
    	JSONParser parser = new JSONParser();
    	JSONObject jobj=new JSONObject();
    	ECKey keyPair = new ECKey();
        try {     
        	jobj = (JSONObject)parser.parse(new FileReader(accountName+".pkey"));
            
        } catch (FileNotFoundException e) {
        	System.out.println("Couldn't locate account file " + accountName + ".pkey");
        	return;
        } catch (ParseException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return;
        } catch (IOException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return;
        }
        String  privateKeyHex = (String) jobj.get("privkey");
        try{
            keyPair=ECKey.fromPrivate(Hex.decodeHex(privateKeyHex.toCharArray()));
        }catch (DecoderException e) {
        	System.out.println("Couldn't create ECKey with privateKeyHex = " + privateKeyHex);
        	return;
        } 
        
        String address = "0x"+new String(Hex.encodeHex(keyPair.getAddress()));
    	CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(rpcAddress);
        String requestString="{\"method\":\"eth_getBalance\",\"params\":[\""+address+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        //System.out.println("The request string in getEtherBalance is "+requestString);
    	String myEtherBalance="";
    	try {
            StringEntity params = new StringEntity(requestString);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            System.out.println("About to execute transaction via RPC: "+request.getURI().toString());
            CloseableHttpResponse response = httpClient.execute(request);
            System.out.println("Finished to execute transaction.");
            myEtherBalance=new BasicResponseHandler().handleResponse(response);
            response.close();
            httpClient.close();
            //System.out.println("The response from RPC is "+response+".");
            jobj=(JSONObject)parser.parse(myEtherBalance);

            for (Object key1 : jobj.keySet()) {
                if (((String)key1).equalsIgnoreCase("result")) {
                	myEtherBalance=(String) jobj.get(key1);
                }
            }
        }catch (UnsupportedEncodingException e) {
        	System.out.println("UnsupportedEncodingException: " + e);
        }catch (ClientProtocolException e) {
        	System.out.println("ClientProtocolException: " + e);
        }catch (IOException e) {
        	System.out.println("IOException: "+ e);
        }catch(ParseException e) {
        	System.out.println("ParseException: "+ e);
        }catch (NumberFormatException e){
        	System.out.println("NumberFormatException=" + e);
        }
    	System.out.println("Ether Balance of "+accountName+"("+address+")"+" is "+new Float(new BigInteger(myEtherBalance.substring(2),16).floatValue()/(new BigInteger(appendingZerosForETH,10).floatValue())).toString()+" Ether");
    	
    }
    
    public void updateBalance() {
        System.out.println("UPDATE BALANCE");
    }
    
    public void closeChannel() {
        System.out.println("CLOSE CHANNEL");
    }
    
    /**
     * Displays a list of all available functions
     */
    private static void displayFunctions() {
        System.out.println("Possible Functions: ");
        Class cls = MicroRaiden.class;
        Method methlist[] = cls.getDeclaredMethods();
        for (int i = 0; i < methlist.length; i++) {
            Method m = methlist[i];
            if(m.getName().equals("main") || m.getName().equals("displayFunctions")) {
                continue;
            }
            
            String params = "";
            Class pvec[] = m.getParameterTypes();
            for (int j = 0; j < pvec.length; j++) {
                params = params + pvec[j];
                if(j < (pvec.length - 1)) {
                    params = params + ", ";
                }
            }
            
            System.out.println("  " + m.getReturnType() + " " + m.getName() + "(" + params + ")");
        }
    }
    
    public void getNonce(String accountName) {
    	JSONParser parser = new JSONParser();
    	JSONObject jobj=new JSONObject();
    	ECKey keyPair = new ECKey();
        try {     
        	jobj = (JSONObject)parser.parse(new FileReader(accountName+".pkey"));
            
        } catch (FileNotFoundException e) {
        	System.out.println("Couldn't locate account file " + accountName + ".pkey");
        	return;
        } catch (ParseException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return;
        } catch (IOException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return;
        }
        String  privateKeyHex = (String) jobj.get("privkey");
        try{
            keyPair=ECKey.fromPrivate(Hex.decodeHex(privateKeyHex.toCharArray()));
        }catch (DecoderException e) {
        	System.out.println("Couldn't create ECKey with privateKeyHex = " + privateKeyHex);
        	return;
        } 
        
        String address = "0x"+new String(Hex.encodeHex(keyPair.getAddress()));
    	CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(rpcAddress);
        String requestString="{\"method\":\"parity_nextNonce\",\"params\":[\""+address+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        //System.out.println("The request string in getNonce is "+requestString);
        request.addHeader("content-type", "application/json");
    	String myNonceResult="";
        try{
            StringEntity params = new StringEntity(requestString);
            request.setEntity(params);
            System.out.println("About to execute transaction via RPC: "+request.getURI().toString());
            CloseableHttpResponse response = httpClient.execute(request);
            System.out.println("Finished to execute transaction.");
            myNonceResult=new BasicResponseHandler().handleResponse(response);
            response.close();
            httpClient.close();
            //System.out.println("The response from RPC is "+response+".");
            jobj=(JSONObject)parser.parse(myNonceResult);

            for (Object key : jobj.keySet()) {
                if (((String)key).equalsIgnoreCase("result")) {
                	myNonceResult=(String) jobj.get(key);
                }
            }
        }catch (UnsupportedEncodingException e) {
        	System.out.println("UnsupportedEncodingException: "+e);
        }catch (ClientProtocolException e) {
        	System.out.println("ClientProtocolException: "+e);
        }catch (IOException e) {
        	System.out.println("IOException: "+e);
        }catch(ParseException e) {
        	System.out.println("ParseException: "+e);
        }catch (NumberFormatException e){
        	System.out.println("NumberFormatException="+e);
        }
        System.out.println("Nonce="+new BigInteger(myNonceResult.substring(2),16).toString(10));
    }
    
    public static void main(String[] args) throws Exception {
    	
        MicroRaiden mr = new MicroRaiden();
        JSONParser parser = new JSONParser();
        
        try {     
            Object obj = parser.parse(new FileReader("rm-ethereum.conf"));

            JSONObject jsonObject =  (JSONObject) obj;
            for (Object key : jsonObject.keySet()) {
                if (((String)key).equalsIgnoreCase("gasPrice")) {
                    gasPrice=new BigInteger((String) jsonObject.get(key),10);
                    System.out.println("The global gas price is set to be "+gasPrice.toString(10));
                }
            }
            
        } catch (FileNotFoundException e) {
        	
        } catch (ParseException e) {
        	System.out.println("Couldn't parse contents in m-ethereum.conf as a JSON object.");
        } catch (IOException e) {
        	System.out.println("Couldn't parse contents in m-ethereum.conf as a JSON object.");
        }
        
        if(args.length < 1) {
            System.out.println("Usage: microraiden-java <function> <args>");
            displayFunctions();
            return;
        }
        
        //get the function name - use reflection to call
        String functionName = args[0];
        
        //some trickery to get the method with the params (if we just
        //try to search for the method without the params specified it
        //will only look for parameter-less version
        Class cls = Class.forName("MicroRaiden");
        Method method = null; 
        Method methlist[] = cls.getDeclaredMethods();
        for (int i = 0; i < methlist.length; i++) {
            Method m = methlist[i];
            if(m.getName().equals(functionName)) {
                method = m;
                break;
            }
        }
        
        //cast the args to the correct type for the function params
        //note if you use a weird type this will probably shit itself.
        Object arglist[] = new Object[args.length - 1];
        Class pvec[] = method.getParameterTypes();
        for(int i = 1; i < args.length; i++) {
            if(pvec.length < i) {
                break;
            }
            
            String argtype = args[i].getClass().getName();
            String actualType = pvec[i-1].getName();
            if(!argtype.equals(actualType)) {
                switch(actualType) {
                    case "int": {
                        arglist[i-1] = Integer.parseInt(args[i]);
                        break;
                    }
                    default: {
                        System.out.println("UNKNOWN PARAM TYPE: " + actualType);
                        return;
                    }
                }
            } else {
                arglist[i-1] = args[i];
            }
        }
        
        Object retobj = method.invoke(mr, arglist);
    }    
}
