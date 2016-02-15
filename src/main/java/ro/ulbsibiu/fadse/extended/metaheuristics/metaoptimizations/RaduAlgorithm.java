/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metaoptimized.MetaOptimizedAlgorithm;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

/**
 *
 * @author Cristian
 */
public class RaduAlgorithm extends BaseMetaOptimizationAlgorithm {

	Logger logger = LogManager.getLogger();
	
    public RaduAlgorithm(Problem problem) {
        super(problem);        
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {    
       
        readInputParameters();
        
        SolutionSet masterPopulation = readOrCreateInitialSolutionSet();

        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();
            ((ServerSimulator) problem_).dumpCurrentPopulation(masterPopulation);
        }

        while (evaluations < maxEvaluations) {
            List<SolutionSet> offspringSets = new ArrayList<>();
            updatePopulationSizes();
            for (int i = 0; i < moas.size(); ++i) {
                SolutionSet offsprings = moas.get(i).generateOffsprings(masterPopulation,
                        currentPopulationSizes[i]);
                offspringSets.add(offsprings);
            }

            for (SolutionSet ss : offspringSets) {
                for (int i = 0; i < ss.size(); ++i) {
                    Solution sol = ss.get(i);
                    problem_.evaluate(sol);
                    problem_.evaluateConstraints(sol);
                    evaluations++;
                }
            }

            for (int i = 0; i < moas.size(); i++) {
            	((ServerSimulator) problem_).dumpCurrentPopulation("off" + moas.get(i).getName(), System.currentTimeMillis(), offspringSets.get(i));
            }

            masterPopulation = selectBestAccordingToPercentages(masterPopulation, offspringSets);
            
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();
                ((ServerSimulator) problem_).dumpCurrentPopulation(masterPopulation);
            }

            updatePercentages(offspringSets);
        }

        Ranking ranking = new Ranking(masterPopulation);
        return ranking.getSubfront(0);
    }

    private SolutionSet selectBestAccordingToPercentages(SolutionSet initialParents, List<SolutionSet> initialOffspringSets) {

        int[] stillToTakeForMOA = new int[moas.size()];
        
        //copy parents' and offsprings' sets so that the initials are not "damaged" 
        SolutionSet parents = (new SolutionSet()).union(initialParents);

        List<SolutionSet> offspringSets = new LinkedList<SolutionSet>();
        for (int i = 0; i < initialOffspringSets.size(); i++) {
            SolutionSet offspringSet = initialOffspringSets.get(i);
            SolutionSet os = new SolutionSet().union(offspringSet);
            offspringSets.add(os);
            stillToTakeForMOA[i] = currentPopulationSizes[i];
            //clear the offspringsets so that we have at the end the selected offsprings for each moa
            initialOffspringSets.get(i).clear();
        }

        //we need to take N individuals still, this is the stopping condition
        int stillToTake = populationSize;

        do {
            
            //create the archive with individuals and who selected them (index of moa)
            //also add every individual to the archive
            HashMap<Solution, LinkedList<Integer>> archive = new HashMap<Solution, LinkedList<Integer>>();
            for (int i = 0; i < parents.size(); i++) {
                Solution sol = parents.get(i);
                if (!archive.containsKey(sol)) {
                    archive.put(sol, new LinkedList<Integer>());
                }
            }

            for (SolutionSet ss : offspringSets) {
                for (int i = 0; i < ss.size(); i++) {
                    Solution sol = ss.get(i);
                    if (!archive.containsKey(sol)) {
                        archive.put(sol, new LinkedList<Integer>());
                    }
                }
            }

            //for each moa select best individuals according to the specific selection operator in a number stillToTakeForMOA[i]
            for (int i = 0; i < moas.size(); ++i) {
                MetaOptimizedAlgorithm moa = moas.get(i);
                SolutionSet bestIndividuals = new SolutionSet();
                try {
                    if (stillToTakeForMOA[i] > 0) {
                        bestIndividuals = moa.selectNextGeneration(parents.union(offspringSets.get(i)), stillToTakeForMOA[i]);
                        //add this to the offspringsets that we will send back
                        initialOffspringSets.set(i, initialOffspringSets.get(i).union(bestIndividuals));
                    }
                } catch (JMException ex) {
                    logger.error("", ex);
                }

                //mark the individuals in the archive as selected 
                for (int solIndex = 0; solIndex < bestIndividuals.size(); solIndex++) {
                    Solution sol = bestIndividuals.get(solIndex);
                    archive.get(sol).add(i);
                }
            }

            //initialize an array for the single selected individuals
            int[] okSelectedIndividualsForMOA = new int[moas.size()];
            //total number of single selected individuals
            int okIndividualsNumber = 0;
            //total number of multiple selected individuals
            int multipleSelectedIndividualsNumber = 0;
            
            //for each solution in the archive we need to see how many times it has been selected
            for (Map.Entry<Solution, LinkedList<Integer>> entry : archive.entrySet()) {
                if (entry.getValue().size() == 1) {
                    okIndividualsNumber++;
                    Integer moaIndex = entry.getValue().get(0);
                    okSelectedIndividualsForMOA[moaIndex]++;
                }
                if (entry.getValue().size() > 1) {
                    multipleSelectedIndividualsNumber++;
                }
            }
            
            //we still need stillTotaTake individuals 
            stillToTake = stillToTake - okIndividualsNumber - multipleSelectedIndividualsNumber;

            
            //compute the "weight" according to document, out of the still to take x need to be from moa1, y from moa2 etc
            int nrStillToTake = 0;
            for (int i = 0; i < moas.size(); i++) {
                stillToTakeForMOA[i] = offspringSets.get(i).size() - okSelectedIndividualsForMOA[i];
                nrStillToTake += stillToTakeForMOA[i];
            }

            double weightForSelection = stillToTake / (double) nrStillToTake;

            int excess = stillToTake;
            for (int i = 0; i < moas.size(); i++) {
                stillToTakeForMOA[i] = (int) (stillToTakeForMOA[i] * weightForSelection + 0.5); //round up
                excess -= stillToTakeForMOA[i];
            }

            
            //can we substract the excess from only one?
            do {
                int random = rand.nextInt(moas.size());
                int stillToTakeFromThis = stillToTakeForMOA[random];
                if (stillToTakeFromThis + excess >= 0) {
                    stillToTakeForMOA[random] += excess;
                    excess = 0;
                } 
//                else {
//                    stillToTakeForMOA[random] += excess;
//                    excess += stillToTakeFromThis;
//                }
            } while (excess > 0);

            //remove all selected individuals from parents and offsprings so that we do not select them again
            for (Map.Entry<Solution, LinkedList<Integer>> entry : archive.entrySet()) {
                if (entry.getValue().size() > 0) {                      
                    for (int i = parents.size() - 1; i >= 0; i--) {
                        if (parents.get(i) == entry.getKey()) {
                            parents.remove(i);
                        }
                    }

                    for (int os = 0; os < offspringSets.size(); os++) {
                        SolutionSet currentSet = offspringSets.get(os);
                        for (int i = currentSet.size() - 1; i >= 0; i--) {
                            if (currentSet.get(i) == entry.getKey()) {
                                currentSet.remove(i);
                            }
                        }
                    }

                }
            }

            //
        } while (stillToTake > 0);

        //create masterPopulation from selected individuals, and do not add duplicates
        SolutionSet newMasterPopulation = new SolutionSet(populationSize);
        LinkedList<Solution> allOffsprings = new LinkedList<Solution>();

        for (SolutionSet off : initialOffspringSets) {
            for (int i = 0; i < off.size(); i++) {
                Solution currentSolution = off.get(i);
                if (!allOffsprings.contains(currentSolution)) {
                    allOffsprings.add(currentSolution);
                    newMasterPopulation.add(currentSolution);
                }
            }
        }

        return newMasterPopulation;
    }   
}
