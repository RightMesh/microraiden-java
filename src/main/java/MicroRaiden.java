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
import org.ethereum.crypto.ECKey;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * CLI for the MicroRaiden client
 */
public class MicroRaiden {
	private static final String rpcAddress="http://localhost:8545";
	private static final String channelManagerAddr="0x488184931e6C37FB0dd1c375570c7470Dcf211C5";
	private static final String customTokenAddr="0x5e0f57e2c2e05434d57d35d90f0f9F676B779F5e";
	private static final String toAddr="0xF4ABFf26965D10E2162d26FEaf7E16349fA201fF";
	private static final String abi="[{\"constant\":true,\"inputs\":[],\"name\":\"challenge_period\",\"outputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_sender_address\",\"type\":\"address\"},{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"}],\"name\":\"getChannelInfo\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"},{\"name\":\"\",\"type\":\"uint192\"},{\"name\":\"\",\"type\":\"uint32\"},{\"name\":\"\",\"type\":\"uint192\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_balance\",\"type\":\"uint192\"},{\"name\":\"_balance_msg_sig\",\"type\":\"bytes\"}],\"name\":\"extractBalanceProofSignature\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_balance\",\"type\":\"uint192\"},{\"name\":\"_balance_msg_sig\",\"type\":\"bytes\"},{\"name\":\"_closing_sig\",\"type\":\"bytes\"}],\"name\":\"cooperativeClose\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_sender_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_balance\",\"type\":\"uint192\"},{\"name\":\"_closing_sig\",\"type\":\"bytes\"}],\"name\":\"extractClosingSignature\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_balance\",\"type\":\"uint192\"}],\"name\":\"uncooperativeClose\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"version\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_deposit\",\"type\":\"uint192\"}],\"name\":\"createChannelERC20\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"}],\"name\":\"settle\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"channel_deposit_bugbounty_limit\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"closing_requests\",\"outputs\":[{\"name\":\"closing_balance\",\"type\":\"uint192\"},{\"name\":\"settle_block_number\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"channels\",\"outputs\":[{\"name\":\"deposit\",\"type\":\"uint192\"},{\"name\":\"open_block_number\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_sender_address\",\"type\":\"address\"},{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"}],\"name\":\"getKey\",\"outputs\":[{\"name\":\"data\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_sender_address\",\"type\":\"address\"},{\"name\":\"_deposit\",\"type\":\"uint256\"},{\"name\":\"_data\",\"type\":\"bytes\"}],\"name\":\"tokenFallback\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_receiver_address\",\"type\":\"address\"},{\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"name\":\"_added_deposit\",\"type\":\"uint192\"}],\"name\":\"topUpERC20\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"token\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"_token_address\",\"type\":\"address\"},{\"name\":\"_challenge_period\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_sender\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_receiver\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_deposit\",\"type\":\"uint192\"}],\"name\":\"ChannelCreated\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_sender\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_receiver\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"indexed\":false,\"name\":\"_added_deposit\",\"type\":\"uint192\"}],\"name\":\"ChannelToppedUp\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_sender\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_receiver\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"indexed\":false,\"name\":\"_balance\",\"type\":\"uint192\"}],\"name\":\"ChannelCloseRequested\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_sender\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_receiver\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_open_block_number\",\"type\":\"uint32\"},{\"indexed\":false,\"name\":\"_balance\",\"type\":\"uint192\"}],\"name\":\"ChannelSettled\",\"type\":\"event\"}]";
    private static final String appendingZerosForETH="1000000000000000000";
    private static final String appendingZerosForRMT="1000000000000000000";
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
    
    public void loadAccountAndSignRecoverable(String accoutName, String msgHashHex){
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
            Object obj = parser.parse(new FileReader(accoutName+".pkey"));

            JSONObject jsonObject =  (JSONObject) obj;

            privateKeyHex = (String) jsonObject.get("privkey");
            keyPair=ECKey.fromPrivate(Hex.decodeHex(privateKeyHex.toCharArray()));
            
        } catch (FileNotFoundException e) {
        	System.out.println("Couldn't locate account file " + accoutName + ".pkey");
        } catch (ParseException e) {
        	System.out.println("Couldn't parse contents in " + accoutName + ".pkey as a JSON object.");
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
    
    public void createChannel() {
        System.out.println("CREATE CHANNEL");
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
    
    public static void getNonce(String account) {
    	String myresult="";
        try{
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(rpcAddress);
            String requestString="{\"method\":\"parity_nextNonce\",\"params\":[\""+account+"\"],\"id\":42,\"jsonrpc\":\"2.0\"}";

            System.out.println("callSysFunction()"+requestString);
            StringEntity params = new StringEntity(requestString);
            System.out.println("REQUEST"+ requestString);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            System.out.println("About to execute transaction:"+request.getURI().toString());
            CloseableHttpResponse response = httpClient.execute(request);
            System.out.println("Finished to execute transaction.");
            String results_=new BasicResponseHandler().handleResponse(response);
            response.close();
            System.out.println("response from RPC is "+response+".");
            JSONParser parser = new JSONParser();
            JSONObject jobj=(JSONObject)parser.parse(results_);

            for (Object key1 : jobj.keySet()) {
            	System.out.println("key1="+key1);
                if (((String)key1).equalsIgnoreCase("result")) {
                    myresult=(String) jobj.get(key1);
                }
            }
            BigInteger nonceValue=new BigInteger(myresult.substring(2),16);
            System.out.println("Nonce="+nonceValue.toString(10));
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
    	
    }
    
    public static void main(String[] args) throws Exception {
        MicroRaiden mr = new MicroRaiden();
        
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
