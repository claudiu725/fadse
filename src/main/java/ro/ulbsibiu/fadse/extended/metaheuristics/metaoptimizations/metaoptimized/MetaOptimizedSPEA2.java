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
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.base.Operator;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.spea2.SPEA2;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.Spea2Fitness;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

/**
 *
 * @author Cristian
 */
public class MetaOptimizedSPEA2 extends MetaOptimizedAlgorithm {
    
    public static final int TOURNAMENTS_ROUNDS = 1;

    private final Problem problem_;
    
    public MetaOptimizedSPEA2(Problem problem) {
        problem_ = problem;
    }
    
      @Override
    public String getName(){
        return "MOSPEA2";
    }
    
    @Override
    public SolutionSet generateOffsprings(SolutionSet parents, int count) throws JMException {
        Operator crossoverOperator = operators_.get("crossover");
        Operator mutationOperator = operators_.get("mutation");
        Operator selectionOperator = operators_.get("selection");
        
        SolutionSet offSpringSolutionSet = new SolutionSet(count);
        Solution[] pars = new Solution[2];
        while (offSpringSolutionSet.size() < count) {
            int j = 0;
            do {
                j++;
                pars[0] = (Solution) selectionOperator.execute(parents);
            } while (j < MetaOptimizedSPEA2.TOURNAMENTS_ROUNDS); // do-while
            int k = 0;
            do {
                k++;
                pars[1] = (Solution) selectionOperator.execute(parents);
            } while (k < MetaOptimizedSPEA2.TOURNAMENTS_ROUNDS); // do-while

            //make the crossover
            Solution[] offSpring = (Solution[]) crossoverOperator.execute(pars);
            mutationOperator.execute(offSpring[0]);
            //problem_.evaluate(offSpring[0]);
            //problem_.evaluateConstraints(offSpring[0]);
            offSpringSolutionSet.add(offSpring[0]);
            //evaluations++;
        } // while
        
        return offSpringSolutionSet;
    }

    @Override
    public SolutionSet selectNextGeneration(SolutionSet union, int count) throws JMException {
        Spea2Fitness spea = new Spea2Fitness(union);
        spea.fitnessAssign();
        return spea.environmentalSelection(count);
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize, archiveSize, maxEvaluations, evaluations;
        Operator crossoverOperator, mutationOperator, selectionOperator;
        SolutionSet solutionSet, archive, offSpringSolutionSet;

        //Read the params
        populationSize = (Integer) getInputParameter("populationSize");
        archiveSize = (Integer) getInputParameter("archiveSize");
        maxEvaluations = (Integer) getInputParameter("maxEvaluations");

        //Read the operators
        crossoverOperator = operators_.get("crossover");
        mutationOperator = operators_.get("mutation");
        selectionOperator = operators_.get("selection");

        boolean outputEveryPopulation = false;
        Object output = getInputParameter("outputEveryPopulation");
        if (output != null) {
                outputEveryPopulation = (Boolean) output;
        }
        String outputPath = (String) getInputParameter("outputPath");
        
        //Initialize the variables
        solutionSet = new SolutionSet(populationSize);
        archive = new SolutionSet(archiveSize);
        evaluations = 0;

        //-> Create the initial solutionSet
        Solution newSolution;
        //Added by HORIA
        CheckpointFileParameter fileParam = (CheckpointFileParameter) getInputParameter("checkpointFile");
        String file ="";
        if(fileParam !=null){
            file = fileParam.GetCheckpointFile();
        }
        if (file != null && !file.equals("")) {
            Logger.getLogger(SPEA2.class.getName()).log(Level.WARNING, "Using a checkpoint file: " + file);
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
                    solutionSet.add(newSolution);
                    i++;
                } //while
            } catch (IOException ex) {
                Logger.getLogger(SPEA2.class.getName()).log(Level.SEVERE, "Checkpoint file does not have enough elements to fill the entire population [" + i + "<" + populationSize + "]. Filling it with random individuals");
                while (i < populationSize) {
                    newSolution = new Solution(problem_);
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    evaluations++;
                    solutionSet.add(newSolution);
                    i++;
                }
            }
        } else {
            //END added by Horia
            for (int i = 0; i < populationSize; i++) {
                newSolution = new Solution(problem_);
                problem_.evaluate(newSolution);
                problem_.evaluateConstraints(newSolution);
                evaluations++;
                solutionSet.add(newSolution);
            } //for
            //Added by HORIA
        }
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
        }
        //END added by Horia

        while (evaluations < maxEvaluations) {
            SolutionSet union = ((SolutionSet) solutionSet).union(archive);            
            //Spea2Fitness spea = new Spea2Fitness(union);
            //spea.fitnessAssign();
            //archive = spea.environmentalSelection(archiveSize);
            archive = selectNextGeneration(union, archiveSize);
            
            ((ServerSimulator) problem_).dumpCurrentPopulation(archive);
            // Create a new offspringPopulation
//            offSpringSolutionSet = new SolutionSet(populationSize);
//            Solution[] parents = new Solution[2];
//            while (offSpringSolutionSet.size() < populationSize) {
//                int j = 0;
//                do {
//                    j++;
//                    parents[0] = (Solution) selectionOperator.execute(archive);
//                } while (j < SPEA2.TOURNAMENTS_ROUNDS); // do-while
//                int k = 0;
//                do {
//                    k++;
//                    parents[1] = (Solution) selectionOperator.execute(archive);
//                } while (k < SPEA2.TOURNAMENTS_ROUNDS); // do-while
//
//                //make the crossover
//                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
//                mutationOperator.execute(offSpring[0]);
//                problem_.evaluate(offSpring[0]);
//                problem_.evaluateConstraints(offSpring[0]);
//                offSpringSolutionSet.add(offSpring[0]);
//                evaluations++;
//            } // while
            offSpringSolutionSet = generateOffsprings(archive, populationSize);
            for (int i = 0; i < offSpringSolutionSet.size(); ++i) {
                Solution offSpring = offSpringSolutionSet.get(i);
                problem_.evaluate(offSpring);
                problem_.evaluateConstraints(offSpring);
                evaluations++;
            }
            
            //Added by HORIA
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            }
            ((ServerSimulator) problem_).dumpCurrentPopulation("offspring", System.currentTimeMillis(),offSpringSolutionSet);
            if (outputEveryPopulation) {
					offSpringSolutionSet.printObjectivesToFile(outputPath
							+ System.currentTimeMillis()+".csv");
				}
            //TODO save archive and solution set to a file
            //END added by Horia
            // End Create a offSpring solutionSet
            solutionSet = offSpringSolutionSet;
        } // while

        Ranking ranking = new Ranking(archive);
        return ranking.getSubfront(0);
    }
    
}
