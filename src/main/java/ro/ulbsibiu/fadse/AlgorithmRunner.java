package ro.ulbsibiu.fadse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jmetal.base.Algorithm;
import jmetal.base.SolutionSet;
import jmetal.util.JMException;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.base.factory.AlgorithmFactory;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;
import ro.ulbsibiu.fadse.utils.Utils;

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

    static Logger logger = LogManager.getLogger();
    private Algorithm algorithm = null; // The algorithm to use

    public void run(Environment env) throws JMException, SecurityException,
            IOException, IllegalArgumentException, IllegalAccessException,
            ClassNotFoundException {
        // Runtime.getRuntime().addShutdownHook(new Thread(new
        // PerformCleanup()));

    	Utils.setEnv(env);
        algorithm = AlgorithmFactory.createFromInputDocument(env.getInputDocument(), env);
        long initTime = System.currentTimeMillis();

        SimulationStatus.getInstance().setAlgorithm(algorithm);
        SimulationStatus.getInstance().setEnvironment(env);
        // Execute the Algorithm
        SolutionSet population = algorithm.execute();

        String objectivesPath = Paths.get(env.getResultsFolder()).resolve("Objectives").toString();
        String variablesPath = Paths.get(env.getResultsFolder()).resolve("Variables").toString();
        population.printObjectivesToFile(objectivesPath);
        population.printVariablesToFile(variablesPath);
		long estimatedTime = System.currentTimeMillis() - initTime ;
        // Result messages
        logger.info("Total execution time: " + estimatedTime + "ms");
        logger.info("Objectives values have been writen to file " + objectivesPath);
        logger.info("Variables values have been writen to file " + variablesPath);
    } // main
} // main

