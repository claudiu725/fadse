/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metaoptimized;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Operator;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzy;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationRandomDefuzzifier;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import ro.ulbsibiu.fadse.utils.Utils;

/**
 *
 * @author Cristian
 */
public class MetaOptimizedNSGAII extends MetaOptimizedAlgorithm {
    
	Logger logger = LogManager.getLogger();
	
    private final Problem problem_;
    
    public MetaOptimizedNSGAII(Problem problem) {
        problem_ = problem;
    }
    
    @Override
    public String getName(){
        return "MONSGAII";
    }

    @Override
    public SolutionSet generateOffsprings(SolutionSet parents, int count) throws JMException {
        Operator mutationOperator;
        Operator crossoverOperator;
        Operator selectionOperator;
        
        mutationOperator = operators_.get("mutation");
        crossoverOperator = operators_.get("crossover");
        selectionOperator = operators_.get("selection");
        
        SolutionSet offspringPopulation = new SolutionSet(count);
        Solution[] pars = new Solution[2];
        while (offspringPopulation.size() < count) {
                pars[0] = (Solution) selectionOperator.execute(parents);
                pars[1] = (Solution) selectionOperator.execute(parents);
                Solution[] offSpring = (Solution[]) crossoverOperator.execute(pars);
                mutationOperator.execute(offSpring[0]);
                mutationOperator.execute(offSpring[1]);
                offspringPopulation.add(offSpring[0]);
                if (offspringPopulation.size() < count) {
                    offspringPopulation.add(offSpring[1]);
                }
        } // for
        
        return offspringPopulation;
    }

    @Override
    public SolutionSet selectNextGeneration(SolutionSet union, int count) throws JMException {
        SolutionSet population = new SolutionSet(count);
        Distance distance = new Distance();
        // Ranking the union
        Ranking ranking = new Ranking(union);
        int remain = count;
        int index = 0;
        SolutionSet front = null;
        // Obtain the next front
        front = ranking.getSubfront(index);
        while ((remain > 0) && (remain >= front.size())) {
            //Assign crowding distance to individuals
            distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
            //Add the individuals of this front
            for (int k = 0; k < front.size(); k++) {
                population.add(front.get(k));
            } // for
            //Decrement remain
            remain = remain - front.size();
            //Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            } // if
        } // while
        // Remain is less than front(index).size, insert only the best one
        if (remain > 0) {  // front contains individuals to insert
            distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
            front.sort(new jmetal.base.operator.comparator.CrowdingComparator());
            for (int k = 0; k < remain; k++) {
                population.add(front.get(k));
            } // for
            remain = 0;
        } // if
        return population;
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        SolutionSet population;
        
        int populationSize;
        int maxEvaluations;
        int evaluations;

        QualityIndicator indicators; // QualityIndicator object
        int requiredEvaluations; // Use in the example of use of the
        // indicators object (see below)


        SolutionSet offspringPopulation;
        SolutionSet union;

        Operator mutationOperator;
        populationSize = (Integer) getInputParameter("populationSize");
        maxEvaluations = (Integer) getInputParameter("maxEvaluations");
        indicators = (QualityIndicator) getInputParameter("indicators");

        boolean outputEveryPopulation = false;
        Object output = getInputParameter("outputEveryPopulation");
        if (output != null) {
            outputEveryPopulation = (Boolean) output;
        }
        String outputPath = (String) getInputParameter("outputPath");

        //Initialize the variables
        population = new SolutionSet(populationSize);
        evaluations = 0;

        requiredEvaluations = 0;

        //Read the operators
        mutationOperator = operators_.get("mutation");

        Solution newSolution;
        //Added by HORIA
        CheckpointFileParameter fileParam = (CheckpointFileParameter) getInputParameter("checkpointFile");
        String file = "";
        if (fileParam != null) {
            file = fileParam.GetCheckpointFile();
        }
        String feasible = (String) getInputParameter("forceFeasibleFirstGeneration");
        int feasiblePercentage = Integer
                .parseInt((String) getInputParameter("forceMinimumPercentageFeasibleIndividuals"));
        if (file != null && !file.equals("")) {
            logger.warn("Using a checkpoint file: " + file);
            int i = 0;
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));

                String line = null; //not declared within while loop
                line = input.readLine();//skip the headder
                while ((line = input.readLine()) != null && i < populationSize) {
                    newSolution = new Solution(problem_);

                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    for (int j = 0; j < problem_.getNumberOfVariables(); j++) {
                        newSolution.getDecisionVariables()[j].setValue(Double.valueOf(tokenizer.nextToken()));
                    }
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    evaluations++;
                    population.add(newSolution);
                    i++;
                } //while
                if (i < populationSize) {
                    throw new IOException("Checkpoint file does not have enough elements to fill the entire population");
                }
            } catch (IOException ex) {
                logger.error("Checkpoint file does not have enough elements to fill the entire population [" + i + "<" + populationSize + "]. Filling it with random individuals");
                while (i < populationSize) {
                    newSolution = new Solution(problem_);
                    mutationOperator.execute(newSolution);
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    if (feasible != null && feasible.equals("true")) {//this will skip ind only if they are infeasible because of constrains
                        if (newSolution.getNumberOfViolatedConstraint() > 0) {
                            continue;
                        }
                    }
                    evaluations++;
                    population.add(newSolution);
                    i++;
                }
            }
        } else {
            int i = 0;
            while (i < populationSize) {
                newSolution = new Solution(problem_);
                mutationOperator.execute(newSolution);
                problem_.evaluate(newSolution);
                problem_.evaluateConstraints(newSolution);
                if (feasible != null && feasible.equals("true")) {//this will skip ind only if they are infeasible because of constrains
                    if (newSolution.getNumberOfViolatedConstraint() > 0) {
                        continue;
                    }
                }
                evaluations++;
                population.add(newSolution);
                i++;
            } //for
            //Added by HORIA
        }
        Utils.join(problem_);
        Utils.dumpCurrentPopulation(population);
        //WORKAROUND
        for (int i = 0; i < populationSize; i++) {
            Solution s = population.get(i);
            problem_.evaluate(s);
        }
        Utils.join(problem_);
        Utils.dumpCurrentPopulationAndFirstParetoFront(population);
        // Generations ...
        while (evaluations < maxEvaluations) {
            offspringPopulation = generateOffsprings(population, populationSize);
            for (int i = 0; i < offspringPopulation.size(); ++i) {
                Solution offspring = offspringPopulation.get(i);
                problem_.evaluate(offspring);
                problem_.evaluateConstraints(offspring);
                evaluations++;
            }

            Utils.join(problem_);
            Utils.dumpCurrentPopulation("offspring", System.currentTimeMillis(), offspringPopulation);
            //WORKAROUND
            for (int i = 0; i < populationSize; i++) {
                Solution s = population.get(i);
                problem_.evaluate(s);
            }
            Utils.join(problem_);
            Utils.dumpCurrentPopulation("corrected", System.currentTimeMillis(), population);

            union = ((SolutionSet) population).union(offspringPopulation);
            population = selectNextGeneration(union, populationSize);

            Utils.dumpCurrentPopulation(population);
            Ranking ranking_temp = new Ranking(population);
            Utils.dumpCurrentPopulation("pareto", System.currentTimeMillis(), ranking_temp.getSubfront(0));
            if (outputEveryPopulation) {
                population.printObjectivesToFile(outputPath + System.currentTimeMillis() + ".csv");
            }
        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);

        // Return the first non-dominated front
        Ranking ranking = new Ranking(population);


        return ranking.getSubfront(0);
    }
}
