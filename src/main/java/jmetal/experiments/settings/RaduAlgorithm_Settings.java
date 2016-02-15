/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.experiments.settings;

import ro.ulbsibiu.fadse.environment.document.InputDocument;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.experiments.Settings;
import jmetal.experiments.SettingsFactory;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.RaduAlgorithm;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metaoptimized.MetaOptimizedAlgorithm;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metrics.Metric;
import ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metrics.MetricsFactory;
import jmetal.util.JMException;

/**
 *
 * @author Cristian
 */
public class RaduAlgorithm_Settings extends Settings {

	Logger logger = LogManager.getLogger();
	
    public int populationSize_ = 100;
    public int maxEvaluations_ = 2500;
    public double minimumPercentage_ = 0.1;

    private final List<InputDocument.InputMetaOptimizedAlgorithm> inputMoas;
    private final List<InputDocument.InputMetric> inputMetrics;

    /**
     * Creates an IsmailAlgorithm_Settings object for the creation of an
     * instance of IsmailAlgorithm. It receives the problem to solve and the
     * parameters from the configuration file for the meta-optimized algorithms
     * and for the metrics to be used.
     *
     * @param problem the problem to solve
     * @param inputMoas configuration for the meta-optimized algorithms
     * @param inputMetrics configuration for the metrics
     * @throws JMException
     */
    public RaduAlgorithm_Settings(Problem problem, List<InputDocument.InputMetaOptimizedAlgorithm> inputMoas, List<InputDocument.InputMetric> inputMetrics) throws JMException {
        super(problem);
        this.inputMoas = inputMoas;
        this.inputMetrics = inputMetrics;
    }

    /**
     * Returns an IsmailAlgorithm configured from the input files. It creates
     * the Algorithm and then it sets its input parameters: it computes the
     * minimum and initial percentages and the weights and it creates the
     * meta-optimized algorithms and the metrics to be used.
     *
     * @return the IsmailAlgorithm configured
     * @throws JMException
     */
    @Override
    public Algorithm configure() throws JMException {
        RaduAlgorithm algorithm = new RaduAlgorithm(problem_);

        algorithm.setInputParameter("populationSize", populationSize_);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);

        algorithm.setInputParameter("minimumPercentages", configureMinimumPercentages());
        algorithm.setInputParameter("initialPercentages", configureInitialPercentages());
        algorithm.setInputParameter("weights", configureInitialWeights());

        List<MetaOptimizedAlgorithm> moas = new ArrayList<>(inputMoas.size());
        for (InputDocument.InputMetaOptimizedAlgorithm inputMoa : inputMoas) {
            Settings settings = (new SettingsFactory()).getSettingsObject(
                    "MetaOptimized" + inputMoa.getName(), problem_);

            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(inputMoa.getConfigPath()));
            } catch (IOException ex) {
                logger.error("", ex);
            }

            MetaOptimizedAlgorithm moa = null;
            try {
                moa = (MetaOptimizedAlgorithm) (settings.configure(properties));
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | JMException ex) {
            	logger.error("", ex);
            }

            moas.add(moa);
        }
        algorithm.setInputParameter("moas", moas);

        List<Metric> metrics = new ArrayList<>(inputMetrics.size());
        for (InputDocument.InputMetric inputMetric : inputMetrics) {
            Metric metric = MetricsFactory.getInstance().createMetrics(inputMetric.getName());
            metrics.add(metric);
        }
        algorithm.setInputParameter("metrics", metrics);

        return algorithm;
    }

    /**
     * Returns an array with the weights of the metrics. If the sum of the
     * weights exceeds 1.0 the method throws an error. If not the remainder up
     * to 1.0 is divided equally among the metrics which don't have a weight
     * specified.
     *
     * @return the array with the weights
     * @throws JMException
     */
    private double[] configureInitialWeights() throws JMException {
        double weightsSum = 0.0;
        int count = 0;

        for (InputDocument.InputMetric inputMetric : inputMetrics) {
            if (inputMetric.getWeight() != null) {
                if (inputMetric.getWeight() < 0.0) {
                    inputMetric.setWeight(null);
                    continue;
                }
                weightsSum += inputMetric.getWeight();
                count++;
            }
        }

        if (weightsSum >= 1.0) {
            throw new JMException("The weights added together yield a value above 1.0!");
        }

        double diff = (1.0 - weightsSum) / count;
        for (InputDocument.InputMetric inputMetric : inputMetrics) {
            if (inputMetric.getWeight() == null) {
                inputMetric.setWeight(diff);
            }
        }

        double[] weights = new double[inputMetrics.size()];
        for (int i = 0; i < inputMetrics.size(); ++i) {
            weights[i] = inputMetrics.get(i).getWeight();
        }

        return weights;
    }

    /**
     * Return an array with the initial percentages of the meta-optimized
     * algorithms. If the initial percentage is below the minimum or it is not
     * assigned then it is adjusted accordingly. If the sum of the initial
     * percentages exceeds 1.0 the method throws an error. The remainder up to
     * 1.0 is split equally among those that initially didn't have a percentage
     * assigned.
     *
     * @return array with the initial percentages of the meta-optimized
     * algorithms
     * @throws JMException
     */
    private double[] configureInitialPercentages() throws JMException {
        double initialSum = 0.0;
        Set<InputDocument.InputMetaOptimizedAlgorithm> notAssigned = new HashSet<>();

        for (InputDocument.InputMetaOptimizedAlgorithm inputMoa : inputMoas) {
            if (inputMoa.getInitialPercentage() == null) {
                notAssigned.add(inputMoa);
            }
            if (inputMoa.getInitialPercentage() == null
                    || inputMoa.getInitialPercentage() < inputMoa.getMinimumPercentage()) {
                inputMoa.setInitialPercentage(inputMoa.getMinimumPercentage());
            }
            initialSum += inputMoa.getInitialPercentage();
        }

        if (initialSum > 1.0) {
            throw new JMException("The initial percentages added together yield a value above 1.0!");
        }

        double diff = (1.0 - initialSum) / (notAssigned.size());
        for (InputDocument.InputMetaOptimizedAlgorithm inputMoa : notAssigned) {
            inputMoa.setInitialPercentage(inputMoa.getInitialPercentage() + diff);
        }

        double[] initialPercentages = new double[inputMoas.size()];
        for (int i = 0; i < inputMoas.size(); ++i) {
            initialPercentages[i] = inputMoas.get(i).getInitialPercentage();
        }

        return initialPercentages;
    }

    /**
     * Return an array with the minimum percentages of the meta-optimized
     * algorithms. Those which do not have a minimum percentage specified are
     * assigned the default minimum percentage of IsmailAlgorithm. If the sum of
     * the percentages exceeds 1.0 the method throws an error.
     *
     * @return the array with the minimum percentages of the meta-optimized
     * algorithms
     * @throws JMException
     */
    private double[] configureMinimumPercentages() throws JMException {
        double minimumSum = 0.0;

        for (InputDocument.InputMetaOptimizedAlgorithm inputMoa : inputMoas) {
            if (inputMoa.getMinimumPercentage() == null
                    || inputMoa.getMinimumPercentage() < 0.0) {
                inputMoa.setMinimumPercentage(minimumPercentage_);
            }
            minimumSum += inputMoa.getMinimumPercentage();
        }

        if (minimumSum > 1.0) {
            throw new JMException("The minimum percentages added together yield a value above 1.0!");
        }

        double[] minimumPercentages = new double[inputMoas.size()];
        for (int i = 0; i < inputMoas.size(); ++i) {
            minimumPercentages[i] = inputMoas.get(i).getMinimumPercentage();
        }

        return minimumPercentages;
    }
}