/**
 * SettingsFactory.java
 * @author Antonio J. Nebro
 * @version 1.0
 */

package jmetal.experiments;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Problem;
import ro.ulbsibiu.fadse.environment.document.InputDocument;

/**
 * This class represents a factory for problems
 */
public class SettingsFactory {
	
	Logger logger = LogManager.getLogger();
	
	InputDocument inputDocument;

	public InputDocument getInputDocument() {
		return inputDocument;
	}

	public void setInputDocument(InputDocument inputDocument) {
		this.inputDocument = inputDocument;
	}

	public Settings getMetaSettingsObject(String algorithmName, Problem problem,
			List<InputDocument.InputMetaOptimizedAlgorithm> inputMoas, List<InputDocument.InputMetric> inputMetrics) {
		try {
			String base = "jmetal.experiments.settings." + algorithmName + "_Settings";
			Class<?> problemClass = Class.forName(base);
			Constructor<?> constructor = problemClass.getConstructors()[0];
			Settings algorithmSettings = (Settings) constructor.newInstance(problem, inputMoas, inputMetrics);
			return algorithmSettings;
		} catch (ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
		}
		return null;
	}

	public Settings getSettingsObject(String algorithmName, Problem problem) {
		Settings settings = null;
		settings = getJmetalSettingsObject(algorithmName, problem);
		if (settings == null)
			settings = getMetaSettingsObject(algorithmName, problem, inputDocument.getMetaOptimizedAlgorithms(), inputDocument.getMetrics());
		if (settings == null)
		{
			logger.error("Could not create settings class");
		}
		return settings;
	}
	
	public Settings getJmetalSettingsObject(String algorithmName, Problem problem) {
		try {
			String base = "jmetal.experiments.settings." + algorithmName + "_Settings";
			Class<?> problemClass = Class.forName(base);
			Constructor<?> constructor = problemClass.getConstructor(Problem.class);
			Settings algorithmSettings = (Settings) constructor.newInstance(problem);
			return algorithmSettings;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		}
		return null;
	}

} // SettingsFactory
