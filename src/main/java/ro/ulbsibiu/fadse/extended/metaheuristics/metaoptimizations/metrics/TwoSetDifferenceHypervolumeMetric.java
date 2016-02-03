/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metrics;

import ro.ulbsibiu.fadse.extended.qualityIndicator.HypervolumeTwoSetDiference;
import ro.ulbsibiu.fadse.extended.qualityIndicator.MetricsUtil;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.util.Ranking;

/**
 *
 * @author Cristian
 */
public class TwoSetDifferenceHypervolumeMetric extends HypervolumeTwoSetDiference implements Metric {

    @Override
    public double compute(SolutionSet... solutionSets) {
        Ranking ranking = new Ranking(solutionSets[0]);
        SolutionSet front1 = new SolutionSet();
        //if (ranking.getNumberOfSubfronts() > 0) {
            front1 = ranking.getSubfront(0);
//        } else {
//            UUID guid = UUID.randomUUID();
//            System.err.println("SolutionSets Size is: " + solutionSets.length);
//            System.err.println("SolutionSet1 Size is: " + solutionSets[0].size());
//            System.err.println("SolutionSet2 Size is: " + solutionSets[1].size());
//            solutionSets[0].printVariablesToFile("D:\\fadse\\output\\GAP\\" + guid + "1var.txt");
//            solutionSets[0].printObjectivesToFile("D:\\fadse\\output\\GAP\\" + guid + "1obj.txt");
//            solutionSets[1].printVariablesToFile("D:\\fadse\\output\\GAP\\" + guid + "2var.txt");
//            solutionSets[1].printObjectivesToFile("D:\\fadse\\output\\GAP\\" + guid + "2obj.txt");
//        }

        ranking = new Ranking(solutionSets[1]);
        SolutionSet front2 = new SolutionSet();
//        if (ranking.getNumberOfSubfronts() > 0) {
            front2 = ranking.getSubfront(0);
//        } else {
//            UUID guid = UUID.randomUUID();
//            System.err.println("SolutionSets Size is: " + solutionSets.length);
//            System.err.println("SolutionSet1 Size is: " + solutionSets[0].size());
//            System.err.println("SolutionSet2 Size is: " + solutionSets[1].size());
//            solutionSets[0].printVariablesToFile("D:\\fadse\\output\\GAP\\" + guid + "1var.txt");
//            solutionSets[0].printObjectivesToFile("D:\\fadse\\output\\GAP\\" + guid + "1obj.txt");
//            solutionSets[1].printVariablesToFile("D:\\fadse\\output\\GAP\\" + guid + "2var.txt");
//            solutionSets[1].printObjectivesToFile("D:\\fadse\\output\\GAP\\" + guid + "2obj.txt");
//        }

        int nrObjectives = front1.get(0).numberOfObjectives();

        double[][] firstFront = new double[front1.size()][nrObjectives];
        double[][] secondFront = new double[front2.size()][nrObjectives];

        double[] maxObjectives = new double[nrObjectives];

        for (int i = 0; i < front1.size(); ++i) {
            Solution sol = front1.get(i);

            for (int j = 0; j < nrObjectives; ++j) {
                firstFront[i][j] = sol.getObjective(j);
                if (firstFront[i][j] > maxObjectives[j]) {
                    maxObjectives[j] = firstFront[i][j];
                }
            }
        }

        for (int i = 0; i < front2.size(); ++i) {
            Solution sol = front2.get(i);

            for (int j = 0; j < nrObjectives; ++j) {
                secondFront[i][j] = sol.getObjective(j);
                if (secondFront[i][j] > maxObjectives[j]) {
                    maxObjectives[j] = secondFront[i][j];
                }
            }
        }

        MetricsUtil.repairParetoOptimalSet(firstFront, front1.size(), nrObjectives);
        MetricsUtil.repairParetoOptimalSet(secondFront, front2.size(), nrObjectives);

        return hypervolumeTwoSetDifference(firstFront, secondFront, maxObjectives, nrObjectives);
    }

}
