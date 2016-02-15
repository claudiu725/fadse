/**
 * ProblemFactory.java
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.problems;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Problem;
import jmetal.util.Configuration;
import jmetal.util.JMException;

/**
 * This class represents a factory for problems
 */
public class ProblemFactory {

	Logger logger = LogManager.getLogger();
    /**
     * Creates an object representing a problem
     * @param name Name of the problem
     * @param params Parameters characterizing the problem
     * @return The object representing the problem
     * @throws JMException
     */
    public Problem getProblem(String name, Object[] params) throws JMException {
        // Params are the arguments
        // The number of argument must correspond with the problem constructor params
        String base = "jmetal.problems.";
        if (name.substring(0, name.length() - 1).equalsIgnoreCase("DTLZ")) {
            base += "DTLZ.";
        } else if (name.substring(0, name.length() - 1).equalsIgnoreCase("WFG")) {
            base += "WFG.";
        } else if (name.substring(0, name.length() - 1).equalsIgnoreCase("ZDT")) {
            base += "ZDT.";
        } else if (name.substring(0, name.length() - 3).equalsIgnoreCase("ZZJ07")) {
            base += "ZZJ07.";
        } else if (name.substring(0, name.length() - 3).equalsIgnoreCase("LZ09")) {
            base += "LZ09.";
        } else if (name.substring(0, name.length() - 4).equalsIgnoreCase("ZZJ07")) {
            base += "ZZJ07.";
        } else if (name.substring(0, name.length() - 3).equalsIgnoreCase("LZ06")) {
            base += "LZ06.";
        } else if (name.substring(0, name.length() - 4).equalsIgnoreCase("CEC2009")) {
            base += "cec2009Competition.";
        } else if (name.substring(0, name.length() - 5).equalsIgnoreCase("CEC2009")) {
            base += "cec2009Competition.";
        } else {
            base = "ro.ulbsibiu.fadse.extended.problems.simulators.";
//            try {
//                //TODO REMOVE. Add it if you want to see bugs in the connector
//                return new Msim3Simulator((Environment) params[0]);
//            } catch (ClassNotFoundException ex) {
//                Logger.getLogger(ProblemFactory.class.getName()).log(Level.SEVERE, null, ex);
//            }

        }
        try {
            Class<?> problemClass = Class.forName(base + name);
            logger.info("Base+name: "+base+name);
            logger.info("Params:" + Arrays.toString(params));
            Constructor[] constructors = problemClass.getConstructors();
            int i = 0;
            //find the constructor
            while ((i < constructors.length)
                    && (constructors[i].getParameterTypes().length != params.length)) {
                i++;
            }
            // constructors[i] is the selected one constructor
            Problem problem = (Problem) constructors[i].newInstance(params);
            return problem;
        }// try
        catch (Exception e) {
            logger.fatal("ProblemFactory.getProblem: "
                    + "Problem '" + name + "' does not exist. "
                    + "Please, check the problem names in jmetal/problems: ", e);
            throw new IllegalArgumentException("Class not found.");
        } // catch
    }

    public Problem getProblem(String name, Properties params) throws JMException {
        // Params are the arguments
        // The number of argument must correspond with the problem constructor params

        String base = "jmetal.gui.problems.";
        if (name.substring(0, name.length() - 1).startsWith("DTLZ")) {
            base += "DTLZ.";
        } else if (name.substring(0, name.length() - 1).startsWith("WFG")) {
            base += "WFG.";
        } else if (name.substring(0, name.length() - 1).startsWith("ZDT")) {
            base += "ZDT.";
        } else if (name.substring(0, name.length() - 3).startsWith("ZZJ07")) {
            base += "ZZJ07.";
        } else if (name.substring(0, name.length() - 3).startsWith("LZ09")) {
            base += "LZ09.";
        } else if (name.substring(0, name.length() - 4).startsWith("ZZJ07")) {
            base += "ZZJ07.";
        } else if (name.substring(0, name.length() - 3).startsWith("LZ06")) {
            base += "LZ06.";
        } else if (name.substring(0, name.length() - 4).startsWith("CEC2009")) {
            base += "cec2009Competition.";
        } else if (name.substring(0, name.length() - 5).startsWith("CEC2009")) {
            base += "cec2009Competition.";
        } else {
            base = "ro.ulbsibiu.fadse.extended.problems.simulators.";
        }
        try {
            Class<?> problemClass = Class.forName(base + name);
            //Constructor constructors = problemClass.getConstructor(Properties.class);
            Constructor<?> constructors = problemClass.getConstructor(Properties.class);

            Problem problem = (Problem) constructors.newInstance(params);

            return problem;
        }// try
        catch (Exception e) {
            Configuration.logger_.severe("ProblemFactory.getProblem: "
                    + "Problem '" + name + "' does not exist. "
                    + "Please, check the problem names in jmetal/problems");
            throw new IllegalArgumentException("Class not found.");
        } // catch
    }
}
