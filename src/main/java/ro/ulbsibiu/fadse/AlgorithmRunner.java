package ro.ulbsibiu.fadse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.SolutionSet;
import jmetal.experiments.Settings;
import jmetal.experiments.SettingsFactory;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;

/*
 *
 *
 * This file is part of the FADSE tool.
 *
 * Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}, Andrei
 * Zorila Copyright (c) 2009-2010 All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The names of its contributors NOT may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */
/**
 *
 * @author Horia Calborean <horia.calborean at ulbsibiu.ro>
 */
public class AlgorithmRunner {

    public static Logger logger = LogManager.getLogger(AlgorithmRunner.class); // Logger object
    private Algorithm algorithm = null; // The algorithm to use

    public void run(Environment env) throws JMException, SecurityException,
            IOException, IllegalArgumentException, IllegalAccessException,
            ClassNotFoundException {
        // Runtime.getRuntime().addShutdownHook(new Thread(new
        // PerformCleanup()));
        Problem problem; // The problem to solve

        Properties properties;
        Settings settings = null;
        String algorithmName = env.getInputDocument().getMetaheuristicName();
        String problemName = env.getInputDocument().getSimulatorName();

        properties = new Properties();
        String path = "N/A";
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current folder is: " + currentDir);
        try {
            path = env.getInputDocument().getMetaheuristicConfigPath();
            properties.load(new FileInputStream(currentDir+ File.separator + path));
        } catch (Exception e) {
            System.out.println("BAD properties file [" + path + "]. going with default values");
        }
        long initTime = System.currentTimeMillis();
        
        logger.info("Using log file : " + env.getAlgorithmFolder(algorithmName).toString());

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createLayout(PatternLayout.SIMPLE_CONVERSION_PATTERN, null, config, null,
            null,false, false, null, null);
        Appender appender = FileAppender.createAppender("target/test.log", "false", "false", "File", "true",
            "false", "false", "4000", layout, null, "false", null, config);
        appender.start();
        config.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};
        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.INFO, "org.apache.logging.log4j",
            "true", refs, null, config, null );
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("org.apache.logging.log4j", loggerConfig);
        ctx.updateLoggers();

        
        SolutionSet population = null;
        System.out.println(env.getInputDocument().getSimulatorType());
        if (env.getInputDocument().getSimulatorType().equalsIgnoreCase("synthetic")) {
            // it is a synthetic problem
            problem = null;
            Object[] problemParams = {"Real"};// TODO configure the problem
            // param type, nr of variables,
            // nr of objectives
            if (problemName.startsWith("DTLZ")) {
                problemParams = new Object[3];
                problemParams[0] = "Real";
                problemParams[1] = env.getInputDocument().getParameters().length;
                problemParams[2] = env.getInputDocument().getObjectives().size();
            }
            problem = (new ProblemFactory()).getProblem(problemName,
                    problemParams);
        } else {
            // is a simulator
            Object[] problemParams = {env};
            problem = (new ProblemFactory()).getProblem(problemName,
                    problemParams);
        }
        Object[] settingsParams = {problem};
        settings = (new SettingsFactory()).getSettingsObject(algorithmName,
                settingsParams);
        algorithm = settings.configure(properties);
        try {
            algorithm.getOperator("mutation").setParameter("environment", env);
        } catch (Exception e) {
            System.out.println("MUTATION was not defined");
        }
        try {
            algorithm.getOperator("crossover").setParameter("environment", env);
        } catch (Exception e) {
            System.out.println("CROSSOVER was not defined");
        }
        // Algorithm parameters htey work only for NSGA-II for other algorithms
        // we need to define others, we have to see how to do it more easily
        // probably with configuration files
        if (env.getCheckpointFileParameter() != null
                && !env.getCheckpointFileParameter().equals("")) {
            algorithm.setInputParameter("checkpointFile",
                    env.getCheckpointFileParameter());
        }
        if (env.getInputDocument().getSimulatorParameter(
                "forceFeasibleFirstGeneration") != null) {
            algorithm.setInputParameter(
                    "forceFeasibleFirstGeneration",
                    env.getInputDocument().getSimulatorParameter(
                    "forceFeasibleFirstGeneration"));
        }
        if (env.getInputDocument().getSimulatorParameter(
                "forceMinimumPercentageFeasibleIndividuals") != null) {
            algorithm.setInputParameter(
                    "forceMinimumPercentageFeasibleIndividuals",
                    env.getInputDocument().getSimulatorParameter(
                    "forceMinimumPercentageFeasibleIndividuals"));
        } else {
            algorithm.setInputParameter(
                    "forceMinimumPercentageFeasibleIndividuals", "0");
        }

        Path outputPath = env.getInputDocument().getOutputPath();
        if (outputPath == null)
        {
        	// use the default environment output path 
        	// if the output path is not present in the xml config
        	outputPath = env.getResultsFolder();
        	env.getInputDocument().setOutputPath(outputPath);
        }
        env.setResultsFolder(outputPath);

        algorithm.setInputParameter("outputPath", outputPath);

        SimulationStatus.getInstance().setAlgorithm(algorithm);
        SimulationStatus.getInstance().setEnvironment(env);
        // Execute the Algorithm
        population = algorithm.execute();

        population.printObjectivesToFile(outputPath.resolve("FUN").toString());
        population.printVariablesToFile(outputPath.resolve("VAR").toString());
        long estimatedTime = System.currentTimeMillis() - initTime;
        // Result messages
        logger.info("Total execution time: " + estimatedTime + "ms");
        logger.info("Objectives values have been writen to file " + outputPath.resolve("FUN").toString());
        logger.info("Variables values have been writen to file " + outputPath.resolve("VAR").toString());
    } // main
} // main

