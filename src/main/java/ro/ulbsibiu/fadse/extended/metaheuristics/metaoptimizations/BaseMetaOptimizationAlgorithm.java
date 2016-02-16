/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.util.JMException;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metaoptimized.MetaOptimizedAlgorithm;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metrics.Metric;
import ro.ulbsibiu.fadse.utils.Utils;

/**
 *
 * @author Radu
 */
public abstract class BaseMetaOptimizationAlgorithm extends Algorithm {

	Logger logger = LogManager.getLogger();
	
    protected Problem problem_;

    protected Random rand;

    protected int populationSize;
    protected int[] currentPopulationSizes;
    protected double[] currentPercentages;
    protected double[] minimumPercentages;
    protected double[] weights;

    protected List<MetaOptimizedAlgorithm> moas;
    protected List<Metric> metrics;

    protected int maxEvaluations;
    protected int evaluations = 0;
    
    protected Path resultsFolder = null;

    public BaseMetaOptimizationAlgorithm(Problem problem) {
        problem_ = problem;
        rand = new Random();
        
        resultsFolder = Utils.getOutputDirectory();
    }

    protected SolutionSet readOrCreateInitialSolutionSet() throws ClassNotFoundException, JMException {

        SolutionSet masterPopulation = new SolutionSet(populationSize);
        CheckpointFileParameter fileParam = (CheckpointFileParameter) getInputParameter("checkpointFile");
        String file = "";
        if (fileParam != null) {
            file = fileParam.GetCheckpointFile();
        }

        if (file != null && !file.equals("")) {
            logger.warn("Using a checkpoint file: " + file);
            int i = 0;
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));

                String line = null; //not declared within while loop
                line = input.readLine();//skip the headder
                while ((line = input.readLine()) != null && i < populationSize) {
                    Solution newSolution = new Solution(problem_);

                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    for (int j = 0; j < problem_.getNumberOfVariables(); j++) {
                        newSolution.getDecisionVariables()[j].setValue(Double.valueOf(tokenizer.nextToken()));
                    }
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    evaluations++;
                    masterPopulation.add(newSolution);
                    i++;
                } //while
                if (i < populationSize) {
                    throw new IOException("Checkpoint file does not have enough elements to fill the entire population");
                }
            } catch (IOException ex) {
                logger.error("Checkpoint file does not have enough elements to fill the entire population [" + i + "<" + populationSize + "]. Filling it with random individuals");
                while (i < populationSize) {
                    Solution newSolution = new Solution(problem_);
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    evaluations++;
                    masterPopulation.add(newSolution);
                    i++;
                }
            }
        } else {
            for (int i = 0; i < populationSize; ++i) {
                Solution newSolution = new Solution(problem_);
                problem_.evaluate(newSolution);
                problem_.evaluateConstraints(newSolution);
                evaluations++;
                masterPopulation.add(newSolution);
            }
        }

        return masterPopulation;
    }

    protected void readInputParameters() {
        populationSize = (Integer) getInputParameter("populationSize");
        maxEvaluations = (Integer) getInputParameter("maxEvaluations");

        moas = (List<MetaOptimizedAlgorithm>) getInputParameter("moas");
        currentPopulationSizes = new int[moas.size()];
        metrics = (List<Metric>) getInputParameter("metrics");

        currentPercentages = (double[]) getInputParameter("initialPercentages");
        minimumPercentages = (double[]) getInputParameter("minimumPercentages");
        weights = (double[]) getInputParameter("weights");
    }

    protected void updatePercentages(List<SolutionSet> offspringSets) {
        double[][] metricValues = new double[metrics.size()][moas.size()];

        PrintWriter writer;
        try {
            long time = System.currentTimeMillis();
            Path dir = resultsFolder.resolve("metrics");
            Files.createDirectories(dir);
            Path metricsOutputFile = (dir.resolve(time + ".csv"));
            writer = new PrintWriter(metricsOutputFile.toString(), "UTF-8");
            writer.println("Metrics");
            for (int i = 0; i < metrics.size(); ++i) {
                Metric metric = metrics.get(i);
                double sum = 0.0;
                for (int j = 0; j < moas.size(); ++j) {
                    MetaOptimizedAlgorithm moa = moas.get(j);

                    SolutionSet theRest = new SolutionSet(populationSize);
                    for (int jj = 0; jj < moas.size(); ++jj) {
                        if (jj != j) {
                            theRest = theRest.union(offspringSets.get(jj));
                        }
                    }

                    metricValues[i][j] = metric.compute(offspringSets.get(j), theRest);
                    sum += metricValues[i][j];
                    writer.print(metricValues[i][j] + ";");
                }

                writer.println();

                double maxMetric = 0;
                if (sum > 0) {
                    for (int j = 0; j < moas.size(); ++j) {
                        if (metricValues[i][j] > maxMetric) {
                            maxMetric = metricValues[i][j];
                        }
                    }
                    for (int j = 0; j < moas.size(); ++j) {
                        if (metricValues[i][j] == 0) {
                            metricValues[i][j] = maxMetric / 10;
                            sum += maxMetric / 10;
                        }
                    }
                } else {
                    for (int j = 0; j < moas.size(); ++j) {
                        metricValues[i][j] = 1;
                        sum++;
                    }
                }

                for (int j = 0; j < moas.size(); ++j) {
                    metricValues[i][j] /= sum;
                }
            }

            writer.println("Percentages;");
            // poate ar fi mai bine cva de genu
            //      nouaValoare = beta * nouValoare + (1 - beta) * vecheaValoare
            //      sa fie o trecere mai lina intre valori ????
            for (int j = 0; j < moas.size(); ++j) {
                currentPercentages[j] = 0.0;
                for (int i = 0; i < metrics.size(); ++i) {
                    currentPercentages[j] += weights[i] * metricValues[i][j];
                }
                writer.print(currentPercentages[j] + ";");
            }
            writer.close();
        } catch (FileNotFoundException ex) {
            logger.error("", ex);
        } catch (UnsupportedEncodingException ex) {
        	logger.error("", ex);
        } catch (IOException e) {
        	logger.error("", e);
		}

        adjustPercentages();
    }

    protected void adjustPercentages() {
        double excess = 0.0;

        // se aduc la valoarea minima acele procentaje care sunt sub prag
        // se contorizeaza cat surplus s-a adunat
        for (int i = 0; i < moas.size(); ++i) {
            if (currentPercentages[i] < minimumPercentages[i]) {
                excess += minimumPercentages[i] - currentPercentages[i];
                currentPercentages[i] = minimumPercentages[i];
            }
        }

        // surplus incearca sa se deduca in mod egal din procentajele din
        //      care se mai poate scadea ceva, care sunt mai mari ca minimul
        while (excess > 0) {
            // se calculeaza din cate procentaje se poate scadea
            int count = 0;
            for (int i = 0; i < moas.size(); ++i) {
                if (currentPercentages[i] > minimumPercentages[i]) {
                    count++;
                }
            }

            // se imparte in mod egal surplusul la toate cele contorizate
            double diff = excess / count;
            for (int i = 0; i < moas.size(); ++i) {
                // daca se poate se scade intreaga portie de surplus
                if (currentPercentages[i] - diff >= minimumPercentages[i]) {
                    excess -= diff;
                    currentPercentages[i] -= diff;
                } // daca nu se scade cat se poate, iar restul va ramane in 
                //      surplus, care va fi compensat in urmatoarea iteratie 
                //      while
                else {
                    excess -= currentPercentages[i] - minimumPercentages[i];
                    currentPercentages[i] = minimumPercentages[i];
                }
            }
        }

        // daca suma procentajelor nu da 1.0, diferenta va fi mica si va fi
        //      oricum mascata atunci cand se realizeaza convertirea la intreg,
        //      dimensiunea populatiei fiind relativ mica ~40
    }

    protected void updatePopulationSizes() throws JMException {
        int remaining = populationSize;
        for (int i = 0; i < currentPopulationSizes.length; ++i) {
            currentPopulationSizes[i] = (int) (currentPercentages[i] * populationSize);
            remaining -= currentPopulationSizes[i];
        }

        // poate nimerim exact pe unu care are prea putin si da mai mic decat threashold-ul
        // sa luam atunci unu la care avem de unde sa scadem
//        boolean removedExcess = false;
//        do {
//            int randomIndexOfPopulation = rand.nextInt(currentPopulationSizes.length);
//            double minimumPercetangeCurrentSelected = minimumPercentages[randomIndexOfPopulation];
//            int currentPopulationSize = currentPopulationSizes[randomIndexOfPopulation];
//            if (minimumPercetangeCurrentSelected > (currentPopulationSize + remaining) / (double)populationSize) {
//                currentPopulationSizes[randomIndexOfPopulation] += remaining;
//                removedExcess = true;
//            }
//        } while (!removedExcess);
        currentPopulationSizes[rand.nextInt(currentPopulationSizes.length)] += remaining;
        if (remaining < 0) {
            logger.error("Remaining is " + remaining);
            throw new JMException("remaining is negative");
        }
    }
}
