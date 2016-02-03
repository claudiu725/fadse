/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metrics;

import jmetal.base.SolutionSet;

/**
 *
 * @author Cristian
 */
public interface Metric {
 
    public abstract double compute(SolutionSet... solutionSets);

}
