import java.lang.reflect.*;
import java.util.Arrays;
import org.ethereum.crypto.ECKey;

/*
 * CLI for the MicroRaiden client
 */
public class MicroRaiden {
    
    public MicroRaiden() {
        //should probably create an eth account with priv / pub keys
        //for doing the signing in the constructor
        
        //another option is we load the account based on what is
        //already saved in the folder
        
        //we need to think about what we should do if the sender / recv
        //are located in the same folder, or different folders and how
        //to store the files
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
    
    
    
    public static void main(String[] args) throws Exception {
        MicroRaiden m = new MicroRaiden();
        
        if(args.length < 1) {
            System.out.println("Usage: microraiden-java <function> <args>");
            return;
        }
        
        //get the function name - use reflection to call
        String functionName = args[0];
        
        Class cls = Class.forName("MicroRaiden");
        Method meth = cls.getMethod(functionName);
        
        Object arglist[] = Arrays.copyOfRange(args, 1, args.length);
        Object retobj = meth.invoke(m, arglist);
    }
}
