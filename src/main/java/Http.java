import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Http {
	 
	private String rpcAddress;
	private boolean debugInfo;
	/**
	 * 
	 * @param _rpcAddress the rpc URL
	 * @param _debug debug info switch
	 */
	public Http(String _rpcAddress, boolean _debug) {
		rpcAddress=_rpcAddress;
		debugInfo=_debug;
				
	}
	
	/**
	 * This function is to send RPC request to the running peer.
	 * @param requestString the HTTP request string
	 * @return the result of the HTTP request
	 * @throws IOException
	 */
	public Object getHttpResponse(String requestString) throws IOException{
    	JSONParser parser = new JSONParser();
    	JSONObject jobj=new JSONObject();
        Object executionResult=null;
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
            if(debugInfo) {
            	System.out.println("result = "+jobj.toJSONString());
            }
            for (Object key : jobj.keySet()) {
                if (((String)key).equalsIgnoreCase("result")) {
                	executionResult=jobj.get(key);
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
}
