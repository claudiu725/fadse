/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.network.client;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.extended.problems.SimulatorWrapper;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;

/**
 *
 * @author Horia Calborean
 */
public class ClientSimulatorRunner implements Runnable {
	
	Logger logger = LogManager.getLogger();
	
    private Individual individual;
    private SimulatorWrapper simulator;
    private Message m;

    public ClientSimulatorRunner(Individual individual, SimulatorWrapper simulator, Message m) {
        logger.info("Client Simulator Runner configured");
        this.individual = individual;
        this.simulator = simulator;
        this.m = m;
    }

    public void run() {
        try {
            //simualtor.evaluate(individual.getSolution());
        	logger.info("Evaluating individual");
            simulator.performSimulation(individual);
            //retrieve the results of the simulation
            logger.info("ClientSimulatorRunner: Simulation ended. Preparing to send back the results");
            ResultsSender resSender = new ResultsSender();
            resSender.send(individual, m);
        } catch (IOException ex) {
            logger.error("IOException", ex);
        }
    }

    public Individual getIndividual() {
        return individual;
    }
}
