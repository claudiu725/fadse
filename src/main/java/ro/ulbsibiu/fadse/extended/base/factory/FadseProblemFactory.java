package ro.ulbsibiu.fadse.extended.base.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Problem;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.document.InputDocument;

/**
 * @author Claudiu
 *
 */
public class FadseProblemFactory {
	static Logger logger = LogManager.getLogger(FadseProblemFactory.class);
	
	static Problem createFromInputDocument(InputDocument document, Environment env) throws JMException
	{
		Problem problem = null;
        logger.info("Simulator type is " + document.getSimulatorType());
        String problemName = document.getSimulatorName();
        if (document.getSimulatorType().equalsIgnoreCase("synthetic")) {
            // it is a synthetic problem
            Object[] problemParams = {"Real"};// TODO configure the problem
            // param type, nr of variables,
            // nr of objectives
            if (problemName.startsWith("DTLZ")) {
                problemParams = new Object[3];
                problemParams[0] = "Real";
                problemParams[1] = document.getParameters().length;
                problemParams[2] = document.getObjectives().size();
            }
            problem = (new ProblemFactory()).getProblem(problemName,
                    problemParams);
        } else {
            // is a simulator
            Object[] problemParams = { env };
            problem = (new ProblemFactory()).getProblem(problemName,
                    problemParams);
        }
        logger.info("Created a problem of class " + problem.getClass().getName());
		return problem;
	}
}
