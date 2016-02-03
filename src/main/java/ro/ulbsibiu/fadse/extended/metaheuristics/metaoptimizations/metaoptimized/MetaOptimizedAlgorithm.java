/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metaoptimized;

import jmetal.base.Algorithm;
import jmetal.base.SolutionSet;
import jmetal.util.JMException;

/**
 *
 * @author Cristian
 */
// this class extends Algorithm because we want to be benefit of the
//      functionality of the class, i.e. the input/output/operators management
public abstract class MetaOptimizedAlgorithm extends Algorithm {
    
    public abstract String getName();
    
    // generates offsprings using selection, mutation and crossover operators
    //      and the input parameters (ex. mutation probability)
    public abstract SolutionSet generateOffsprings(SolutionSet parents, int count) throws JMException;
    
    // selects the best individuals for the next generations from
    //      the union of the offsprings and the parents
    // for the moment, in the implementations we will throw
    //      UnsupportedOperationException. maybe in the future, we will
    //      modify all of the meta-heuristics to be metaoptimized
    public abstract SolutionSet selectNextGeneration(SolutionSet union, int count) throws JMException;
    
}
