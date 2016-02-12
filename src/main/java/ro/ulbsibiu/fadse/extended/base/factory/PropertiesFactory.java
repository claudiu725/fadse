package ro.ulbsibiu.fadse.extended.base.factory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Claudiu
 *
 */
public class PropertiesFactory {
	static Logger logger = LogManager.getLogger(PropertiesFactory.class);
	
	static Properties createFromFile(String path)
	{
		Properties properties = new Properties();
        String currentDir = System.getProperty("user.dir");
        logger.info("Current folder is: " + currentDir);
        try {
            path = currentDir+ File.separator + path;
            logger.info("Loading properties file " + path);
            properties.load(new FileInputStream(path));
        } catch (Exception e) {
            logger.error("BAD properties file [" + path + "]. going with default values", e);
        }
		return properties;
	}
}
