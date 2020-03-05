import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPCliente {
   public static void main (String args[]) { // args message and hostname
      Socket s = null;
      String[] arg = new String[2];
      Scanner input = new Scanner(System.in);
      arg[0] = "ping";
      System.out.println("Ingrese el nombre del host");
      arg[1] = input.nextLine();
      input.close();
      try{
         int serverPort = 7896;
         s = new Socket(arg[1], serverPort);    
         DataInputStream in = new DataInputStream(s.getInputStream());
         DataOutputStream out =new DataOutputStream(s.getOutputStream());
         out.writeUTF(arg[0]);
         String data = in.readUTF();
         System.out.println("Leí: "+ data) ; 
      }catch (UnknownHostException e){
           System.out.println("Socket:"+e.getMessage());
      }catch (EOFException e){
           System.out.println("EOF:"+e.getMessage());
      }catch (IOException e){
           System.out.println("readline:"+e.getMessage());
      }finally {if(s!=null) try { s.close(); }catch (IOException e){
           System.out.println("close:"+e.getMessage());}}
     }
}
