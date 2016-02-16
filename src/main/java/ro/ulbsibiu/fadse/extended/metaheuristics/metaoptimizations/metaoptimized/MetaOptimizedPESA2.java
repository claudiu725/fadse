package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metaoptimized;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.CrowdingDistanceComparator;
import jmetal.base.operator.crossover.Crossover;
import jmetal.base.operator.mutation.Mutation;
import jmetal.base.operator.selection.PESA2Selection;
import jmetal.base.operator.selection.Selection;
import jmetal.util.JMException;
import jmetal.util.archive.AdaptiveGridArchive;
import ro.ulbsibiu.fadse.utils.Utils;

/**
 * This class implements the PESA2 algorithm. 
 */
public class MetaOptimizedPESA2 extends MetaOptimizedAlgorithm {

    /**
     * Stores the problem to solve
     */
    private Problem problem_;
    
    AdaptiveGridArchive archive;
    Crossover crossover;
    Mutation mutation;
    Selection selection;
    boolean init = false;

    /**
     * Constructor
     * Creates a new instance of PESA2
     */
    public MetaOptimizedPESA2(Problem problem) {
        problem_ = problem;
    } // PESA2

    void init()
    {
    	if (!init)
    	{    		
	        int archiveSize, bisections;
	
	        // Read parameters
	        archiveSize = ((Integer) (inputParameters_.get("archiveSize"))).intValue();
	        bisections = ((Integer) (inputParameters_.get("bisections"))).intValue();
	
	        archive = new AdaptiveGridArchive(archiveSize, bisections,
	                problem_.getNumberOfObjectives());
    		selection = new PESA2Selection();
	        crossover = (Crossover) operators_.get("crossover");
	        mutation = (Mutation) operators_.get("mutation");
	        init = true;
    	}
    }
    
    /**
     * Runs of the PESA2 algorithm.
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {

    	int archiveSize, bisections, maxEvaluations, evaluations, populationSize;
    	
        SolutionSet solutionSet;
        populationSize = ((Integer) (inputParameters_.get("populationSize"))).intValue();
        archiveSize = ((Integer) (inputParameters_.get("archiveSize"))).intValue();
        bisections = ((Integer) (inputParameters_.get("bisections"))).intValue();
        maxEvaluations = ((Integer) (inputParameters_.get("maxEvaluations"))).intValue();
        // Get the operators
        crossover = (Crossover) operators_.get("crossover");
        mutation = (Mutation) operators_.get("mutation");
        
        archive = new AdaptiveGridArchive(archiveSize, bisections,
                problem_.getNumberOfObjectives());

        // Initialize the variables
        evaluations = 0;

        solutionSet = new SolutionSet(populationSize);

        //-> Create the initial individual and evaluate it and his constraints
        for (int i = 0; i < populationSize; i++) {
            Solution solution = new Solution(problem_);
            problem_.evaluate(solution);
            problem_.evaluateConstraints(solution);
            evaluations++;
            solutionSet.add(solution);
        }
        //<-
        Utils.join(problem_);
        Utils.dumpCurrentPopulation("offspring", System.currentTimeMillis(), solutionSet);
        
        // Incorporate non-dominated solution to the archive
        for (int i = 0; i < solutionSet.size(); i++) {
            archive.add(solutionSet.get(i)); // Only non dominated are accepted by
            // the archive
        }
        Utils.dumpCurrentPopulation(archive);

        // Clear the init solutionSet
        solutionSet.clear();

        //Iterations....
        do {
            //-> Create the offSpring solutionSet
        	
            while (evaluations < maxEvaluations) {
            	SolutionSet offsprings = selectNextGeneration(archive, populationSize);
            	for (int i = 0; i < offsprings.size(); i++)
            	{
	                problem_.evaluate(offsprings.get(i));
	                problem_.evaluateConstraints(offsprings.get(i));
	                evaluations++;
	                solutionSet.add(offsprings.get(i));
            	}
            }
            Utils.join(problem_);
            Utils.dumpCurrentPopulation("offspring", System.currentTimeMillis(), solutionSet);

            for (int i = 0; i < solutionSet.size(); i++) {
                archive.add(solutionSet.get(i));
            }

            // Clear the solutionSet
            solutionSet.clear();
            Utils.dumpCurrentPopulation(archive);
        } while (evaluations < maxEvaluations);
        //Return the  solutionSet of non-dominated individual
        return archive;
    } // execute

	@Override
	public String getName() {
		return "PESA2";
	}

	@Override
	public SolutionSet generateOffsprings(SolutionSet parents, int count) throws JMException {
		init();
		for (int i = 0; i < parents.size(); i++)
		{
			archive.add(parents.get(i));
		}
		SolutionSet offsprings = new SolutionSet(count);
		for (int i = 0; i < count; i++)
		{
			Solution[] par = new Solution[2];
	        par[0] = (Solution) selection.execute(archive);
	        par[1] = (Solution) selection.execute(archive);
	
	        Solution[] offSpring = (Solution[]) crossover.execute(par);
	        mutation.execute(offSpring[0]);
	        offsprings.add(offSpring[0]);
		}
        return offsprings;
	}

	@Override
	public SolutionSet selectNextGeneration(SolutionSet union, int count) throws JMException {
		union.sort(new CrowdingDistanceComparator());
		SolutionSet offsprings = new SolutionSet(count);
		for (int i=0; i < count; i++)
		{
			offsprings.add(union.get(i));
		}
        return offsprings;
	}
} // PESA2

