/**
 * SPEA2_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * NSGAII_Settings class of algorithm NSGAII
 */
package jmetal.experiments.settings;

import jmetal.base.*;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.experiments.Settings;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metaoptimized.MetaOptimizedSPEA2;
import jmetal.metaheuristics.spea2.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

public class MetaOptimizedSPEA2_Settings extends Settings {

    public int populationSize_ = 100;
    public int archiveSize_ = 100;
    public int maxEvaluations_ = 25000;
    public double mutationProbability_ = 1.0 / problem_.getNumberOfVariables();
    public double crossoverProbability_ = 0.9;
    public double distributionIndexForCrossover_ = 20.0;
    public double distributionIndexForMutation_ = 20.0;
    public String crossoverOperator_ = "SBXCrossover";
    public String mutationOperator_ = "PolynomialMutation";
    public String selectionOperator_ = "BinaryTournament2";

    /**
     * Constructor
     */
    public MetaOptimizedSPEA2_Settings(Problem problem) {
        super(problem);
    } // SPEA2_Settings

    /**
     * Configure SPEA2 with default parameter settings
     * @return an algorithm object
     * @throws jmetal.util.JMException
     */
    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Operator crossover;         // Crossover operator
        Operator mutation;         // Mutation operator
        Operator selection;         // Selection operator

        QualityIndicator indicators;

        // Creating the problem
        algorithm = new MetaOptimizedSPEA2(problem_);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", populationSize_);
        algorithm.setInputParameter("archiveSize", archiveSize_);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);

        // Mutation and crossover for real codification
         crossover = CrossoverFactory.getCrossoverOperator(crossoverOperator_);
        crossover.setParameter("probability", crossoverProbability_);
        crossover.setParameter("distributionIndex", distributionIndexForCrossover_);

        mutation = MutationFactory.getMutationOperator(mutationOperator_);
        mutation.setParameter("probability", mutationProbability_);
        mutation.setParameter("distributionIndex", distributionIndexForMutation_);

        // Selection operator
        selection = SelectionFactory.getSelectionOperator(selectionOperator_) ;

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        // Creating the indicator object
        if (!paretoFrontFile_.equals("")) {
            indicators = new QualityIndicator(problem_, paretoFrontFile_);
            algorithm.setInputParameter("indicators", indicators);
        } // if

        return algorithm;
    } // configure
} // SPEA2_Settings