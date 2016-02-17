package ro.ulbsibiu.fadse.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.environment.parameters.VirtualParameter;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metaoptimized.MetaOptimizedAlgorithm;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;

public class Utils {

	static Logger logger = LogManager.getLogger();
	private static Environment environment;
	private static String iniPath;
    public static void setIniPath(String iniPath) {
		Utils.iniPath = iniPath;
	}

	private Random r;

    public Random getRandom() {
        if (r == null) {
            r = new Random();
        }
        return r;
    }

    public static String generateCSV(SolutionSet s) {
        String csvOutput = "";
        for (int i = 0; i < s.size(); i++) {
            String csvLine = "";
            Solution solution = s.get(i);
            for (Variable v : solution.getDecisionVariables()) {
                try {
                    csvLine += v.getValue() + ",";
                } catch (JMException ex) {
                    logger.error("", ex);
                    csvLine += "unknown" + ",";
                }
            }
            for (int j = 0; j < solution.numberOfObjectives(); j++) {
                double objVal = solution.getObjective(j);
                csvLine += Double.toString(objVal) + ",";
            }
            csvLine = csvLine.substring(0, csvLine.length() - 1);
            csvLine += System.getProperty("line.separator");
            csvOutput += csvLine;
        }
        return csvOutput;
    }

    public static String generateCSVHeadder(Environment environment) {
        String headder = "";
        for (Parameter p : environment.getInputDocument().getParameters()) {
            headder += p.getName() + ",";
        }
        for (Objective o : environment.getInputDocument().getObjectives().values()) {
            headder += o.getName() + ",";
        }
        headder = headder.substring(0, headder.length() - 1);
        headder += System.getProperty("line.separator");
        return headder;
    }

    /**
     *
     * @param simulationStatus
     * @return a new SolutionSet containing solutions with filled objectives
     */
    public SolutionSet insertObjectivesValuesIntoSolutions(SimulationStatus simulationStatus) {
        //extract all the solutions from the simualtion status and build new objects so we will work on local data
        List<Message> filledMessages = simulationStatus.getReceiver().getResults();
        SolutionSet solSet = new SolutionSet();
        Map<String, Solution> solMap = new HashMap<String, Solution>();
        for (Message filledM : filledMessages) {
            for (Message sentM : simulationStatus.getSentMessages()) {
                if (filledM.getMessageId().equals(sentM.getMessageId())) {
                    //obtain the solution of this individual
                    Solution temp = simulationStatus.getSolution(sentM.getMessageId());
                    Solution s = new Solution(temp);
                    solSet.add(s);
                    solMap.put(sentM.getMessageId(), s);
                }
            }
        }
        for (Message filledM : filledMessages) {
            for (Message sentM : simulationStatus.getSentMessages()) {
                if (filledM.getMessageId().equals(sentM.getMessageId())) {
                    List<Objective> objs = filledM.getIndividual().getObjectives();
                    int i = 0;
                    for (Objective o : objs) {
                        //obtain the solution of this individual
                        Solution s = solMap.get(sentM.getMessageId());
                        double value = s.getObjective(i);
                        value = (o.getValue() + value);//Add all the values. later we will divide it by the number of benchmarks
                        s.setObjective(i, value);
                        i++;
                    }
                }
            }
        }

        //compute the average
        //since the same solution exists  nrOfBenchmarks times in the sent messages list we have to divide by nr of benchmarks only once
        //so we first build a set of all the solutions (no duplciates)
        Set<Solution> solutions = new HashSet<Solution>();
        for (Message sentM : simulationStatus.getSentMessages()) {
            Solution s = solMap.get(sentM.getMessageId());
            solutions.add(s);
        }
        for (Solution s : solutions) {
            for (int i = 0; i < s.numberOfObjectives(); i++) {
                double value = s.getObjective(i);
                value = value / simulationStatus.getEnvironment().getInputDocument().getBenchmarks().size();//compute the average
//                System.out.println("FINAL for solution["+s.getDecisionVariables()+"] for objective["+i+"] = "+value);
                s.setObjective(i, value);
            }
        }
        return solSet;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static double[] concat(double[] first, double[] second) {
        double[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static Parameter[] getParameters(Solution solution, Environment environment) {
        Variable[] vars = solution.getDecisionVariables();
        Parameter[] params = environment.getInputDocument().getParameters();
        /** for all variables... associate them with a parameter */
        for (int i = 0; i < vars.length; i++) {
            try {
                Parameter p = params[i];
                Parameter parameter = (Parameter) p.clone();
//                System.out.printf("param %s - variable %s\n", parameter.getName(), vars[i].getValue());
                parameter.setVariable(vars[i]);
                //System.out.printf("%d - %d", vars[i].getValue(), parameter.getValue());
                params[i] = parameter;
            } catch (CloneNotSupportedException ex) {
                logger.error("cloning of the parameter was not supported", ex);
            }
        }
        return params;
    }

    public static Parameter[] getParametersAndVitualParameters(Solution solution, Environment environment) {
        Variable[] vars = solution.getDecisionVariables();
        Parameter[] params = new Parameter[environment.getInputDocument().getParameters().length+environment.getInputDocument().getVirtualParameters().length];
        /** for all variables... associate them with a parameter */
        for (int i = 0; i < vars.length; i++) {
            try {
                Parameter p = environment.getInputDocument().getParameters()[i];
                Parameter parameter = (Parameter) p.clone();
//                System.out.printf("param %s - variable %s\n", parameter.getName(), vars[i].getValue());
                parameter.setVariable(vars[i]);
                //System.out.printf("%d - %d", vars[i].getValue(), parameter.getValue());
                params[i] = parameter;
            } catch (CloneNotSupportedException ex) {
            	logger.error("cloning of the parameter was not supported", ex);
            }
        }
        if (environment.getInputDocument().getVirtualParameters() != null) {
            for (Parameter p : environment.getInputDocument().getVirtualParameters()) {
                VirtualParameter e = (VirtualParameter) p;
                for (Parameter param : environment.getInputDocument().getParameters()) {
                    try {
                        e.addVariable(param.getName(), new Double((Integer) param.getValue()));
                    } catch (Exception ex) {}
                }
            }
            Parameter[] virtualParameters = environment.getInputDocument().getVirtualParameters();
            Parameter[] origParams = environment.getInputDocument().getParameters();
            System.arraycopy(virtualParameters, 0, params, origParams.length, virtualParameters.length);

        }
        return params;
    }
    
    public static void setEnv(Environment envrionment)
    {
    	Utils.environment = envrionment;
    }
    
    public static Environment getEnv()
    {
    	return environment;
    }
    
    public static String normalizePath(String path)
    {
    	String[] paths = path.split("/");
    	return Paths.get(System.getProperty("user.dir")).resolve(String.join(File.separator, paths)).toString();
    }

    public static void dumpCurrentPopulation(SolutionSet population) {        
        dumpCurrentPopulation("filled", String.valueOf(System.currentTimeMillis()), population);
    }
    
    public static void dumpCurrentPopulation(String folder, SolutionSet population) {
    	dumpCurrentPopulation(folder, System.currentTimeMillis(), population);
    }
    
    public static void dumpCurrentPopulation(String folder, long filename, SolutionSet population) {
    	dumpCurrentPopulation(folder, String.valueOf(filename), population);
    }

    public static void dumpCurrentPopulation(String folder, String filename, SolutionSet population) {
        String result = Utils.generateCSVHeadder(getEnv());
        result += Utils.generateCSV(population);
        
        System.out.println("Result of the population (" + folder + "/" + filename + "):\n" + result);
        
        try {
        	Path dir = Paths.get(getEnv().getResultsFolder()).resolve(folder);
            Files.createDirectories(dir);
            File file = dir.resolve(filename + ".csv").toFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(result);
            out.close();
        } catch (IOException e) {
            logger.error("",e);
        }
    }
    
    public static void dumpMoasPopulations(List<MetaOptimizedAlgorithm> moas, List<SolutionSet> offspringSets)
    {
        for (int i = 0; i < moas.size(); i++) {
        	Utils.dumpCurrentPopulation("off" + moas.get(i).getName(), System.currentTimeMillis(), offspringSets.get(i));
        }
    }
    
    public static void dumpCurrentPopulationAndFirstParetoFront(SolutionSet population)
    {
		Utils.dumpCurrentPopulation("corrected"
				, System.currentTimeMillis(), population);
		Ranking ranking_temp = new Ranking(population);
		Utils.dumpCurrentPopulation("pareto"
				, System.currentTimeMillis(), ranking_temp.getSubfront(0));
    }
    
    public static void join(Problem problem_)
    {
		if (problem_ instanceof ServerSimulator) {
			((ServerSimulator) problem_).join();// blocks until all the
												// offsprings are evaluated
		}
    }
    
    public static Path getOutputDirectory() 
    {
    	return Paths.get(getEnv().getInputDocument().getOutputPath());
    }
    
    public static String getIniPath()
    {
    	return iniPath;
    }
}
