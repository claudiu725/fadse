package ro.ulbsibiu.fadse;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ralf
 */
public class ExitInputLister implements Runnable {

	public static Logger logger = LogManager.getLogger();
	
    public static void addExitListener() {
        ExitInputLister exi = new ExitInputLister();
        Thread t = new Thread(exi);
        t.setDaemon(true);
        t.start();
        logger.info("The ExitInputLister is running now...");
    }

    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        try {
            InputStream in = System.in;

            while (true) {

                while (in.available() < 4) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ioe) {
                    	logger.info("Interrupted by key input");
                    }
                    ;
                }
                byte[] buffer = new byte[512];
                int bytesRead = in.read(buffer);
                line = new String(buffer);
                logger.info("I read: " + line.trim());

                if (line.contains("exit")) {
                    logger.info("###### EXIT ######");
                    System.exit(0);
                } else {
                    logger.info("# No idea what to do with it: " + line.trim());
                    logger.info("Write 'exit' to exit");
                }
            }

            /* while ((line = br.readLine()) != null) {
            System.out.println("A line has been read from input: " + line);

            if(line.equals("exit")) {
            System.out.println("###### EXIT ######");
            System.exit(0);
            } else {
            System.out.println("# No idea what to do with it...");
            }
            } */
        } catch (IOException ex) {
            logger.error("IOException ", ex);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                // Nothing to do.
            }
        }
        logger.info("Exit-Listener has completed its work.");
    }
}
