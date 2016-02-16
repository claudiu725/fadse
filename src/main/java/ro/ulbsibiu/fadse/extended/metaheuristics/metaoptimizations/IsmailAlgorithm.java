/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import ro.ulbsibiu.fadse.utils.Utils;

/**
 *
 * @author Cristian
 */
public class IsmailAlgorithm extends BaseMetaOptimizationAlgorithm {
 
	Logger logger = LogManager.getLogger();
	
    public IsmailAlgorithm(Problem problem) {
        super(problem);        
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {      
        
        readInputParameters();
        
        SolutionSet masterPopulation = readOrCreateInitialSolutionSet();
             
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();
        }
        Utils.dumpCurrentPopulation(masterPopulation);

        while (evaluations < maxEvaluations) {
            List<SolutionSet> offspringSets = new ArrayList<>();
            updatePopulationSizes();
            for (int i = 0; i < getMoas().size(); ++i) {
                SolutionSet offsprings = getMoas().get(i).generateOffsprings(masterPopulation,
                        populationSize);
                offspringSets.add(selectRandom(masterPopulation.union(offsprings), currentPopulationSizes[i]));
            }

            masterPopulation.clear();
            for (SolutionSet ss : offspringSets) {
                for (int i = 0; i < ss.size(); ++i) {
                    masterPopulation.add(ss.get(i));
                }
            }

            for (int i = 0; i < masterPopulation.size(); ++i) {
                Solution sol = masterPopulation.get(i);
                problem_.evaluate(sol);
                problem_.evaluateConstraints(sol);
                evaluations++;
            }                        
            
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();
            }
            Utils.dumpCurrentPopulation(masterPopulation);
            Utils.dumpMoasPopulations(getMoas(), offspringSets);
            
            logger.warn("Evaluations until now " + evaluations);
            updatePercentages(offspringSets);
        }

        Ranking ranking = new Ranking(masterPopulation);
        return ranking.getSubfront(0);
    }

    private SolutionSet selectRandom(SolutionSet offsprings, int count) {
        SolutionSet selected = new SolutionSet(count);
        for (; count > 0; count--) {
            int i = rand.nextInt(offsprings.size());
            selected.add(offsprings.get(i));
            offsprings.remove(i);
        }
        return selected;
    }  
}
