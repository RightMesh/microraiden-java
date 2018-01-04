import java.lang.reflect.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
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
import org.kocakosm.pitaya.security.Digest;
import org.kocakosm.pitaya.security.Digests;

/*
 * CLI for the MicroRaiden client
 */
public class MicroRaiden {
	private static final String rpcAddress="http://localhost:8545";
	private static final String channelAddr="0x4913f12d38c04094cF1E382d0ffEEf3036eCCa32";
	private static final String tokenAddr="0x0fC373426c87F555715E6fE673B07Fe9E7f0E6e7";
	private static final CallTransaction.Contract channelContract = new CallTransaction.Contract("[{\"constant\":true,\"inputs\":[],\"name\":\"challenge_period\",\"outputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_sender_address\",\"type\":\"address\"},{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"}],\"name\":\"getChannelInfo\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"},{\"name\":\"\",\"type\":\"uint192\"},{\"name\":\"\",\"type\":\"uint32\"},{\"name\":\"\",\"type\":\"uint192\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_balance\",\"type\":\"uint192\"},{\"name\":\"_balance_msg_sig\",\"type\":\"bytes\"}],\"name\":\"extractBalanceProofSignature\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_balance\",\"type\":\"uint192\"},{\"name\":\"_balance_msg_sig\",\"type\":\"bytes\"},{\"name\":\"_closing_sig\",\"type\":\"bytes\"}],\"name\":\"cooperativeClose\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_sender_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_balance\",\"type\":\"uint192\"},{\"name\":\"_closing_sig\",\"type\":\"bytes\"}],\"name\":\"extractClosingSignature\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_balance\",\"type\":\"uint192\"}],\"name\":\"uncooperativeClose\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"version\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_deposit\",\"type\":\"uint192\"}],\"name\":\"createChannelERC20\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"}],\"name\":\"settle\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"channel_deposit_bugbounty_limit\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"closing_requests\",\"outputs\":[{\"name\":\"closing_balance\",\"type\":\"uint192\"},{\"name\":\"settle_block_number\",\"type\":\"uint32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"channels\",\"outputs\":[{\"name\":\"deposit\",\"type\":\"uint192\"},{\"name\":\"open_block_number\",\"type\":\"uint32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_sender_address\",\"type\":\"address\"},{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"}],\"name\":\"getKey\",\"outputs\":[{\"name\":\"data\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_sender_address\",\"type\":\"address\"},{\"name\":\"_deposit\",\"type\":\"uint256\"},{\"name\":\"_data\",\"type\":\"bytes\"}],\"name\":\"tokenFallback\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_added_deposit\",\"type\":\"uint192\"}],\"name\":\"topUpERC20\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"token\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[{\"name\":\"_token_address\",\"type\":\"address\"},{\"name\":\"_challenge_period\",\"type\":\"uint32\"}],\"payable\":false,\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_sender\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_receiver\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_deposit\",\"type\":\"uint192\"}],\"name\":\"ChannelCreated\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_sender\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_receiver\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"indexed\":false,\"name\":\"_added_deposit\",\"type\":\"uint192\"}],\"name\":\"ChannelToppedUp\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_sender\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_receiver\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"indexed\":false,\"name\":\"_balance\",\"type\":\"uint192\"}],\"name\":\"ChannelCloseRequested\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_sender\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_receiver\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"indexed\":false,\"name\":\"_balance\",\"type\":\"uint192\"}],\"name\":\"ChannelSettled\",\"type\":\"event\"}]");
	private static final CallTransaction.Contract tokenContract = new CallTransaction.Contract("[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"mint\",\"outputs\":[],\"payable\":true,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"multiplier\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint8\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"transferFunds\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"version\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner_address\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"},{\"name\":\"_data\",\"type\":\"bytes\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"},{\"name\":\"_spender\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[{\"name\":\"initial_supply\",\"type\":\"uint256\"},{\"name\":\"token_name\",\"type\":\"string\"},{\"name\":\"token_symbol\",\"type\":\"string\"},{\"name\":\"decimal_units\",\"type\":\"uint8\"}],\"payable\":false,\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_num\",\"type\":\"uint256\"}],\"name\":\"Minted\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]");
	private static final String appendingZerosForETH="1000000000000000000";
    private static final String appendingZerosForTKN="1000000000000000000";
    private static final BigInteger MAX_196_BIT=new BigInteger("2",10).pow(196).subtract(new BigInteger("1",10));
    private static BigInteger gasPrice=new BigInteger("5000000000");
    private static boolean debugInfo=false;
    private static final int INTERVAL_CHECK_TRANS_DONE=100;

    /*
    private boolean running=false;
    private String mytransactionID = null;
    private final Object lockID = new Object();
    private final Object lockRunning = new Object();
    public void newTransactionID(String id) {
        synchronized (lockID) {
        	mytransactionID = id;
        }
    }

    public String getTransactionID() {
        synchronized (lockID) {
            String temp = mytransactionID;
            mytransactionID = null;
            return temp;
        }
    }
    
    public void enableRunning() {
        synchronized (lockRunning) {
        	running = true;
        }
    }

    public void disableRunning() {
        synchronized (lockRunning) {
        	running = false;
        }
    }
    */
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
    
    public void getTokenBalance(String accountName){
    	ECKey keyPair=getECKeyByName(accountName);
    	if(keyPair==null) {
    		System.out.println("Cannot load account with name "+accountName+".");
    		return;
    	}
        
        String address = "0x"+new String(Hex.encodeHex(keyPair.getAddress()));
        
        CallTransaction.Function balanceOf = tokenContract.getByName("balanceOf");
        byte [] functionBytes=balanceOf.encode(address);
        String requestString = "{\"method\":\"eth_call\"," +
                "\"params\":[" +
                "{" +
                "\"to\":\""+tokenAddr+"\"," +
                "\"data\":\""+"0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(functionBytes))+"\"" +
                "}," +
                "\"latest\"" +
                "]," +
                "\"id\":42,\"jsonrpc\":\"2.0\"}";
        if(debugInfo) {
        	System.out.println("Request in getTokenBalance = "+requestString);
        }
        String myTokenBalance="";
        try {
        	myTokenBalance=getHttpResponse(requestString);
        }catch (IOException e) {
        	System.out.println("Cannot get token balance for "+accountName);
    		return;
        }
    	System.out.println("Balance = "+new Float(new BigInteger(myTokenBalance.substring(2),16).floatValue()/(new BigInteger(appendingZerosForETH,10).floatValue())).toString()+" TKN");
    	
    }
    
    private static ECKey getECKeyByName(String accountName) {
    	JSONParser parser = new JSONParser();
    	JSONObject jobj=new JSONObject();
        try {     
        	jobj = (JSONObject)parser.parse(new FileReader(accountName+".pkey"));
            
        } catch (FileNotFoundException e) {
        	System.out.println("Couldn't locate account file " + accountName + ".pkey");
        	return null;
        } catch (ParseException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return null;
        } catch (IOException e) {
        	System.out.println("Couldn't parse contents in " + accountName + ".pkey as a JSON object.");
        	return null;
        }
        String  privateKeyHex = (String) jobj.get("privkey");
        try{
            return ECKey.fromPrivate(Hex.decodeHex(privateKeyHex.toCharArray()));
        }catch (DecoderException e) {
        	System.out.println("PrivateKeyHex = " + privateKeyHex + " is invalid.");
        	return null;
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
    
    public void createChannel(String senderAccountName, String receiverAccountID, String deposit) {
    	try{
    		Double.parseDouble(deposit);
    	}catch (NumberFormatException e) {
    		System.out.println("The numer format is wrong.");
    		return;
    	}
    	BigInteger initDeposit=new BigInteger("0");
    	if(deposit.indexOf(".")!=-1) {
	    	if(deposit.length()-deposit.indexOf(".")>19) {
	    		deposit=deposit.substring(0,deposit.indexOf(".")+19);
	    	}
	    	String localAppendingZerosForETH=appendingZerosForTKN.substring(0, appendingZerosForTKN.length()-deposit.length()+1+deposit.indexOf("."));
	    	initDeposit=new BigInteger(deposit.replace(".", "")).multiply(new BigInteger(localAppendingZerosForETH));
	    	if(MAX_196_BIT.compareTo(initDeposit)<0) {
	    		System.out.println("Please choose a deposit <= "+MAX_196_BIT.toString(10));
	    		return;
	    	}
    	}else {
    		initDeposit=new BigInteger(deposit,10).multiply(new BigInteger(appendingZerosForTKN));
    	}
    	ECKey keyPair=getECKeyByName(senderAccountName);
    	if(keyPair==null) {
    		System.out.println("Cannot load account with name "+senderAccountName+".");
    		return;
    	}
        
        String address = "0x"+new String(Hex.encodeHex(keyPair.getAddress()));
    	
    	if(debugInfo) {
    		System.out.println("User "+senderAccountName+"("+address+") will open a channel to "+receiverAccountID+" with " + deposit +" Token.");
    	}
        
        CallTransaction.Function approve=tokenContract.getByName("approve");
        byte [] approveFunctionBytes=approve.encode(channelAddr,initDeposit); 
        String queryApproveGasString = "{\"method\":\"eth_estimateGas\"," +
                "\"params\":[" +
                "{" +
                "\"from\":\""+address+"\"," +
                "\"to\":\""+tokenAddr+"\"," +
                "\"value\":\""+"0x"+new BigInteger("0",10).toString(16)+"\","+
                "\"data\":\""+"0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(approveFunctionBytes))+"\"" +
                "}" +
                "]," +
                "\"id\":42,\"jsonrpc\":\"2.0\"}";
        String approveGasEstimate="";
    	try {
    		approveGasEstimate=getHttpResponse(queryApproveGasString);
        }catch (IOException e) {
        	System.out.println("Invoking function with given arguments is not allowed.");
    		return;
        }
    	if(debugInfo) {
    		System.out.println("The estimatedGas of approve is "+approveGasEstimate+".");
    	}
    	
    	String queryNonceString="{\"method\":\"parity_nextNonce\",\"params\":[\""+address+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
    	String myNonceResult1="";
    	try {
    		myNonceResult1=getHttpResponse(queryNonceString);
        }catch (IOException e) {
        	System.out.println("Nonce with account "+senderAccountName+" cannot be found.");
    		return;		
        }
    	if(debugInfo) {
    		System.out.println("The nonce of "+senderAccountName+" is "+myNonceResult1);
    	}
    	
        Transaction approveTrans = new Transaction(bigIntegerToBytes(new BigInteger(myNonceResult1.substring(2),16)), // nonce
                bigIntegerToBytes(gasPrice), // gas price
                bigIntegerToBytes(new BigInteger(approveGasEstimate.substring(2),16)), // gas limit
                ByteUtil.hexStringToBytes(tokenAddr), // to id
                bigIntegerToBytes(new BigInteger("0",10)), // value
                approveFunctionBytes, 42);// chainid
        approveTrans.sign(keyPair);
        String signedApproveTrans = "0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(approveTrans.getEncoded()));
        String approveSendRawTransactionString = "{\"method\":\"eth_sendRawTransaction\",\"params\":[\""
                + signedApproveTrans + "\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        
    	String myTransactionID1="";
    	try {
    		myTransactionID1=getHttpResponse(approveSendRawTransactionString);
        }catch (IOException e) {
        	System.out.println("Fail to execute HTTP request.");
    		return;
        }
    	
        if(!"".equals(myTransactionID1)) {
        	System.out.println("Running ... ");
        	waitingForTransaction(myTransactionID1);
        }
        if(debugInfo) {
        	System.out.println("\bApproving funding transfer is done.");  	
        }
        String myNonceResult2="";
        try {
    		myNonceResult2=getHttpResponse(queryNonceString);
        }catch (IOException e) {
        	System.out.println("Nonce with account "+senderAccountName+" cannot be found.");
    		return;
        }
        if(debugInfo) {
        	System.out.println("The nonce of "+senderAccountName+" is "+myNonceResult2);
        }

    	CallTransaction.Function createChannelERC20 = channelContract.getByName("createChannelERC20");
        byte [] createChannelERC20FunctionBytes=createChannelERC20.encode(receiverAccountID,initDeposit);        
        String queryCreatChannelGasString = "{\"method\":\"eth_estimateGas\"," +
                "\"params\":[" +
                "{" +
                "\"from\":\""+address+"\"," +
                "\"to\":\""+channelAddr+"\"," +
                "\"value\":\""+"0x"+new BigInteger("0",10).toString(16)+"\","+
                "\"data\":\""+"0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(createChannelERC20FunctionBytes))+"\"" +
                "}" +
                "]," +
                "\"id\":42,\"jsonrpc\":\"2.0\"}";
        String creatChannelGasEstimate="";
    	try {
    		creatChannelGasEstimate=getHttpResponse(queryCreatChannelGasString);
        }catch (IOException e) {
        	System.out.println("Invoking function with given arguments is not allowed.");
    		return;
        }

    	if(debugInfo) {
    		System.out.println("The estimatedGas of createChannelERC20 is "+creatChannelGasEstimate);
    	}
        Transaction createTrans = new Transaction(bigIntegerToBytes(new BigInteger(myNonceResult2.substring(2),16)), // nonce
                bigIntegerToBytes(gasPrice), // gas price
                bigIntegerToBytes(new BigInteger(creatChannelGasEstimate.substring(2),16)), // gas limit
                ByteUtil.hexStringToBytes(channelAddr), // to id
                bigIntegerToBytes(new BigInteger("0",10)), // value
                createChannelERC20FunctionBytes, 42);// chainid
        createTrans.sign(keyPair);
        String signedChannelCreationTrans = "0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(createTrans.getEncoded()));
        String createChannelSendRawTransactionString = "{\"method\":\"eth_sendRawTransaction\",\"params\":[\""
                + signedChannelCreationTrans + "\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        
    	String myTransactionID2="";
    	try {
    		myTransactionID2=getHttpResponse(createChannelSendRawTransactionString);
        }catch (IOException e) {
        	System.out.println("Fail to execute HTTP request.");
    		return;
        }
    	
        if(!"".equals(myTransactionID2)) {
	        String blockNumberHex=waitingForTransaction(myTransactionID2);
	        if(debugInfo) {
	        	System.out.println("\bChannel has been opened in block "+blockNumberHex);
	        }
	        Digest keccak256 = Digests.keccak256();

	        
	        String firstArgVal=address.substring(2).toLowerCase();
	        String secondArgVal=receiverAccountID.substring(2).toLowerCase();
	        String thirdArgVal=String.format("%08x", Integer.parseInt(blockNumberHex.substring(2), 16));
	        try{
	        	byte[] data = concatenateByteArrays(Hex.decodeHex(firstArgVal.toCharArray()),Hex.decodeHex(secondArgVal.toCharArray()),Hex.decodeHex(thirdArgVal.toCharArray()));
	        	if(debugInfo) {
	        		System.out.println("The keccak256 argument of bytes in string "+Hex.encodeHexString(data));	
	        	}
	        	byte[] keyInBytes=keccak256.reset().update(data).digest();
	        	String channelKeyHex = "0x"+new String(Hex.encodeHexString(keyInBytes));
	        	System.out.println("\bChannel key = "+channelKeyHex);
	        	System.out.println("Channel on Koven can be found on page:\nhttps://kovan.etherscan.io/address/0x4913f12d38c04094cf1e382d0ffeef3036ecca32#readContract");
	        }catch (DecoderException e) {
	        	System.out.println("Hex string cannot be converted to byte array!");
	        }        
	        
        }
    	return;

    }
    
    /**
     * Helper function to join two byte arrays together.
     * @param a the first byte array
     * @param b the second byte array
     * @return the merged array
     */
    private static byte[] concatenateByteArrays(byte[]... arrays)
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
    
    private String getHttpResponse(String requestString) throws IOException{
    	JSONParser parser = new JSONParser();
    	JSONObject jobj=new JSONObject();
        String executionResult="";
        String temp="";
    	try {
    		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    		
    		HttpPost request = new HttpPost(rpcAddress);     
            request.addHeader("content-type", "application/json");
    		request.setEntity(new StringEntity(requestString));  
    		
            CloseableHttpResponse response = httpClient.execute(request);            
            response.close();
            httpClient.close();
            
            temp=new BasicResponseHandler().handleResponse(response);
            jobj=(JSONObject)parser.parse(temp);
            for (Object key : jobj.keySet()) {
                if (((String)key).equalsIgnoreCase("result")) {
                	executionResult=(String) jobj.get(key);
                    if(debugInfo) {
                    	System.out.println("result = "+executionResult);
                    }
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
    	if("".equals(executionResult)) {
    		throw new IOException(temp);
    	}
    	return executionResult;
    } 
    
    public void buyToken(String accountName,String amountOfEther){
    	ECKey keyPair=getECKeyByName(accountName);
    	if(keyPair==null) {
    		System.out.println("Cannot load account with name "+accountName+".");
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
    	if(debugInfo) {
    		System.out.println("User "+accountName+"("+address+") will trade "+value.toString()+" Wei to Token.");
    	}
        
        CallTransaction.Function mint = tokenContract.getByName("mint");
        byte [] functionBytes=mint.encode();
        String queryGasString = "{\"method\":\"eth_estimateGas\"," +
                "\"params\":[" +
                "{" +
                "\"from\":\""+address+"\"," +
                "\"to\":\""+tokenAddr+"\"," +
                "\"value\":\""+"0x"+value.toString(16)+"\","+
                "\"data\":\""+"0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(functionBytes))+"\"" +
                "}" +
                "]," +
                "\"id\":42,\"jsonrpc\":\"2.0\"}";
        String gasEstimateResult="";
    	try {
    		gasEstimateResult=getHttpResponse(queryGasString);
        }catch (IOException e) {
        	System.out.println("Invoking function with given arguments is not allowed.");
    		return;
        }
    	if(debugInfo) {
    		System.out.println("The estimatedGas of mint is "+gasEstimateResult);
    	}
    	
    	String queryTokenBalanceString="{\"method\":\"eth_getBalance\",\"params\":[\""+address+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        String myEtherBalance="";
    	try {
    		myEtherBalance=getHttpResponse(queryTokenBalanceString);
        }catch (IOException e) {
        	System.out.println("Invoking function with given arguments is not allowed.");
    		return;
        }
    	if(debugInfo) {
    		System.out.println("Total ether balance of "+accountName+" is "+myEtherBalance);
    	}
    	if(new BigInteger(gasEstimateResult.substring(2),16).multiply(gasPrice).add(value).compareTo(new BigInteger(myEtherBalance.substring(2),16))>0) {
    		System.out.println("Insufficient Ether to finish the transaction.");
    		return;
    	}
    	
    	String queryNonceString="{\"method\":\"parity_nextNonce\",\"params\":[\""+address+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
    	String myNonceResult="";
    	try {
    		myNonceResult=getHttpResponse(queryNonceString);
        }catch (IOException e) {
        	System.out.println("Nonce with account "+accountName+" cannot be found.");
    		return;
        }
    	if(debugInfo) {
    		System.out.println("The nonce of "+accountName+" is "+myNonceResult);
    	}
    	
        Transaction t = new Transaction(bigIntegerToBytes(new BigInteger(myNonceResult.substring(2),16)), // nonce
                bigIntegerToBytes(gasPrice), // gas price
                bigIntegerToBytes(new BigInteger(gasEstimateResult.substring(2),16)), // gas limit
                ByteUtil.hexStringToBytes(tokenAddr), // to id
                bigIntegerToBytes(value), // value
                functionBytes, 42);// chainid
        t.sign(keyPair);
        String signedTrans = "0x" + new String(org.apache.commons.codec.binary.Hex.encodeHex(t.getEncoded()));
        String mintSendRawTransactionString = "{\"method\":\"eth_sendRawTransaction\",\"params\":[\""
                + signedTrans + "\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        
    	String myTransactionID="";
    	try {
    		myTransactionID=getHttpResponse(mintSendRawTransactionString);
        }catch (IOException e) {
        	System.out.println("Fail to execute HTTP request.");
    		return;
        }
    	
        if(!"".equals(myTransactionID)) {
        	waitingForTransaction(myTransactionID);
        }
        System.out.println("\bYou have been given 50 tokens.");

    	
    }
    
    private static String waitingForTransaction(String myTransactionID) {
    	if(debugInfo) {
    		System.out.println("Transaction ID = "+myTransactionID);
    	}
		boolean loop=true;
		String blockNumber=new String();
        while(loop){
        	Object tempObj=null;
        	try {
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost request = new HttpPost(rpcAddress);     
                request.addHeader("content-type", "application/json");
                String queryTransactionString = "{\"method\":\"eth_getTransactionReceipt\"," +
                        "\"params\":[\"" +
                        myTransactionID +
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
                //The jsonObject can be further parsed to get more information.
                blockNumber = (String)jsonObject.get("blockNumber");
            }
            try {
                Thread.sleep(INTERVAL_CHECK_TRANS_DONE);
                System.out.print("\b\\");
                Thread.sleep(INTERVAL_CHECK_TRANS_DONE);
                System.out.print("\b|");
                Thread.sleep(INTERVAL_CHECK_TRANS_DONE);
                System.out.print("\b/");
                Thread.sleep(INTERVAL_CHECK_TRANS_DONE);
                System.out.print("\b-");

            }catch (InterruptedException e) {
            	
            }
        }
        return blockNumber;
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

        String requestString="{\"method\":\"eth_getBalance\",\"params\":[\""+address+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";
        //System.out.println("The request string in getEtherBalance is "+requestString);
        String myEtherBalance="";
    	try {
    		myEtherBalance=getHttpResponse(requestString);
        }catch (IOException e) {
        	System.out.println("Invoking function with given arguments is not allowed.");
    		return;
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
            if(m.getName().equals("main") 
            		|| m.getName().equals("displayFunctions")
            		|| m.getName().equals("getECKeyByName")
            		|| m.getName().equals("concatenateByteArrays")
            		|| m.getName().equals("getHttpResponse")
            		|| m.getName().equals("waitingForTransaction")) {
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
                if (((String)key).equalsIgnoreCase("debugInfo")) {
                	debugInfo=((String) jsonObject.get(key)).equals("true")?true:false;
                }
            	if (((String)key).equalsIgnoreCase("gasPrice")) {
                    gasPrice=new BigInteger((String) jsonObject.get(key),10);
                    if(debugInfo) {
                    	System.out.println("The global gas price is set to be "+gasPrice.toString(10));
                    }
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
