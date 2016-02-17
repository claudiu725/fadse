/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.network.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;

/**
 *
 * @author Horia Calborean
 */
public class ResultsSender {

	Logger logger = LogManager.getLogger();
	
    public void send(Individual ind, Message m) throws IOException {
        ObjectOutputStream out = null;
        Socket socket = null;
        ObjectInputStream in = null;
        try {
            m.setIndividual(ind);
            m.setType(Message.TYPE_RESPONSE);
            InetAddress address = m.getServerIP();
            int port = m.getServerListenPort();
            logger.info("Sending to -"+address+":"+port);
            socket = new Socket(address, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(m);
            out.flush();
//            System.out.println("ResultsSender: Waiting for ACK");
            in = new ObjectInputStream(socket.getInputStream());
            Message response;
            socket.setSoTimeout(60000);//wait for 10 seconds for a response
            response = (Message) in.readObject();
            if(response.getType()==Message.TYPE_ACK && response.getMessageId().equals(m.getMessageId())){
            logger.info("ACK received for message - " + response.getMessageId());
            } else {
                send(ind, m);
            }
        } catch (SocketTimeoutException ex){
        	logger.warn("Server did not send back the ACK response. Retring", ex);
            send(ind, m);
        }catch(EOFException ex){
        	logger.warn("Server did not send back the ACK response corectly. Retring", ex);
            send(ind, m);
        } catch (ClassNotFoundException ex) {
        	logger.error("ClassNotFoundException", ex);
        } catch (IOException ex) {
            logger.error("IOException", ex);
        } finally {
        	if (out != null)
        		out.close();
        	if (in != null)
        		in.close();
        	if (socket != null)
        		socket.close();
        }
    }
}
