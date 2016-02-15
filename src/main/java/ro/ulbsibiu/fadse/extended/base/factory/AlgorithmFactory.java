package ro.ulbsibiu.fadse.extended.base.factory;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.experiments.Settings;
import jmetal.experiments.SettingsFactory;
import jmetal.util.JMException;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.document.InputDocument;

/**
 * @author Claudiu
 *
 */
public class AlgorithmFactory {
	static Logger logger = LogManager.getLogger(AlgorithmFactory.class);
	
	static public Algorithm createFromInputDocument(InputDocument document, Environment env) throws JMException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException
	{
		Problem problem = FadseProblemFactory.createFromInputDocument(document, env);
		Properties properties = PropertiesFactory.createFromFile(document.getMetaheuristicConfigPath());
        Settings settings = (new SettingsFactory()).getSettingsObject(document.getMetaheuristicName(), problem);
        Algorithm algorithm = settings.configure(properties);
        algorithm.getOperator("mutation").setParameter("environment", env);
        algorithm.getOperator("crossover").setParameter("environment", env);
        
        if (document.getSimulatorParameter("forceFeasibleFirstGeneration") != null) {
            algorithm.setInputParameter("forceFeasibleFirstGeneration",
                    document.getSimulatorParameter("forceFeasibleFirstGeneration"));
        }
        if (document.getSimulatorParameter("forceMinimumPercentageFeasibleIndividuals") != null) {
            algorithm.setInputParameter("forceMinimumPercentageFeasibleIndividuals",
                    document.getSimulatorParameter("forceMinimumPercentageFeasibleIndividuals"));
        } else {
            algorithm.setInputParameter(
                    "forceMinimumPercentageFeasibleIndividuals", "0");
        }

        logger.info("Created an algorithm of class " + algorithm.getClass().getName());
		return null;
	}
}
