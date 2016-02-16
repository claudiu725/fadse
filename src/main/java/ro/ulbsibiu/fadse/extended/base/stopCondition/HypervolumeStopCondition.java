/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.base.stopCondition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.extended.qualityIndicator.HypervolumeNoTruePareto;
import ro.ulbsibiu.fadse.extended.qualityIndicator.MetricsUtil;

/**
 *
 * @author Horia
 */
public class HypervolumeStopCondition extends StopCondition {

	Logger logger = LogManager.getLogger();
	
    public HypervolumeStopCondition(Environment environment) {
        super(environment);
    }

    public boolean stopConditionFulfilled(List<Path> listOfPopulationFiles) {
        boolean result = false;
        InputDocument id = environment.getInputDocument();
        //***********INITIALIZATION*************
        //TODO 
        int populationSize = 100;
        int nrOfobejctives = id.getObjectives().size();

        int X = 20;
        double treshold = 0.0015;
        //***********END INITIALIZATION*************


        HypervolumeNoTruePareto hypervolume = new HypervolumeNoTruePareto();

        List<Double> hypValues = new ArrayList<Double>();
		List<double[][]> parsedFiles = MetricsUtil.parseFiles(nrOfobejctives, populationSize, listOfPopulationFiles);//TODO repalce 100 with real size of pop
		double[] maxObjectives = MetricsUtil.getmaxObjectives(nrOfobejctives, parsedFiles);
		for (double[][] parsedFile : parsedFiles) {
		    //repairing Pareto optimal set = removing objectives with the value 0 and replacing them with the first individual of the current pop
		    MetricsUtil.repairParetoOptimalSet(parsedFile, populationSize, nrOfobejctives);
		    double value = hypervolume.hypervolume(parsedFile, maxObjectives, nrOfobejctives);
		    hypValues.add(0, value);
//                System.out.println(hypValues.get(0));
		}
		double sum = 0;
		if (hypValues.size() > X) {
		    for (int i = 1; i <= X; i++) {
		        sum += hypValues.get(0) - hypValues.get(i);
		    }
		} else {
		    sum = Integer.MAX_VALUE;
		}
//            System.out.println("SUM: " + sum);
		result = sum < treshold;
        return result;
    }

    public static void main(String[] args) {
        Path currentdir = Paths.get(System.getProperty("user.dir"));
        File dir = currentdir.toFile();
        String xmlFileName = "falsesimin.xml";
        Environment env = new Environment(dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + xmlFileName);
        env.setResultsFolder(currentdir.resolve("test").toString());
        HypervolumeStopCondition condition = new HypervolumeStopCondition(env);
        String resultsFolder = env.getResultsFolder();
        List<Path> listOfPopulationFiles = MetricsUtil.getListOfFiles(Paths.get(resultsFolder), "filled");
        for (int i = 0; i < listOfPopulationFiles.size(); i++) {
            List<Path> subLsit = listOfPopulationFiles.subList(0, i);
            if (condition.stopConditionFulfilled(subLsit)) {
                System.out.println("["+i+"]"+"Fullfilled");
            } else {
                System.out.println("["+i+"]"+"NOT fulfiled");
            }
        }
    }
}
