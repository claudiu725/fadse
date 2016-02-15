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
		SettingsFactory settingsFactory = new SettingsFactory();
		settingsFactory.setInputDocument(document);
        Settings settings = settingsFactory.getSettingsObject(document.getMetaheuristicName(), problem);
        Algorithm algorithm = settings.configure(properties);
        if (algorithm.getOperator("mutation") != null)
        	algorithm.getOperator("mutation").setParameter("environment", env);
        if (algorithm.getOperator("crossover") != null)
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

        
        // Algorithm parameters work only for NSGA-II for other algorithms
        // we need to define others, we have to see how to do it more easily
        // probably with configuration files
        if (env.getCheckpointFileParameter() != null
                && !env.getCheckpointFileParameter().equals("")) {
            algorithm.setInputParameter("checkpointFile",
                    env.getCheckpointFileParameter());
        }

        String outputPath = env.getInputDocument().getOutputPath();
        if (outputPath == null)
        {
        	// use the default environment output path 
        	// if the output path is not present in the xml config
        	outputPath = env.getResultsFolder();
        	env.getInputDocument().setOutputPath(outputPath);
        	logger.info("Using output folder " + outputPath);
        }
        else
        {
        	env.setResultsFolder(outputPath);
        	logger.info("Overriding output folder " + outputPath);
        }

        algorithm.setInputParameter("outputPath", outputPath);
        
        logger.info("Created an algorithm of class " + algorithm.getClass().getName());
		return algorithm;
	}
}
