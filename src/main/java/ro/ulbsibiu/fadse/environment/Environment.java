package ro.ulbsibiu.fadse.environment;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.io.XMLInputReader;
import ro.ulbsibiu.fadse.persistence.ConnectionPool;

public class Environment implements Serializable {

	public static Logger logger = LogManager.getLogger(Environment.class.getName());
    private String neighborsConfigFile;
    private CheckpointFileParameter checkpointFileParam;
    private String fuzzyInputFile;
    private InputDocument inputDocument;
    private String resultsFolder;
    
    public Environment()
    {
    	
    }

    public Environment(String inputFilePath)
    {
    	init(Paths.get(inputFilePath));
    }
    
    public Environment(Path inputFilePath) {
        init(inputFilePath);
    }
    
	public static String generateResultsFolder(InputDocument document)
	{
		List<String> names = new LinkedList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String date = dateFormat.format(new Date());
        names.add(date);
		if (document.getMetaOptimizedAlgorithms() != null && !document.getMetaOptimizedAlgorithms().isEmpty())
        {
        	for (InputDocument.InputMetaOptimizedAlgorithm child : document.getMetaOptimizedAlgorithms())
        	names.add(child.getName());
        }
        else
        {
        	names.add(document.getMetaheuristicName());
        }
		if (document.getSimulatorType().equals("synthetic"))
		{
			names.add(document.getSimulatorName());
		}
		else
		{
			names.add(document.getSimulatorParameters().get("realSimulator"));
		}
		return String.join("_", names);
	}
    
    private void init(Path inputFilePath)
    {
    	inputDocument = (new XMLInputReader()).parse(inputFilePath.toString());
        ConnectionPool.setInputDocument(inputDocument);
        String currentdir = System.getProperty("user.dir");
        String outputName = generateResultsFolder(inputDocument);
        Path resultsFolder = Paths.get(currentdir, "results", outputName);
        this.resultsFolder = resultsFolder.toString();
        if (inputDocument.getOutputPath().isEmpty())
        	inputDocument.setOutputPath(resultsFolder.toString());
        try {
			Files.createDirectories(resultsFolder);
		} catch (IOException e) {
			logger.fatal("Could not create results folder " + resultsFolder.toString(), e);
		}
        logger.info("Output folder is " + resultsFolder.toString());
    }
    
    public String getAlgorithmFolder(String algorithmName)
    {
    	return Paths.get(resultsFolder).resolve(algorithmName + ".log").toString();
    }

    public String getNeighborsConfigFile() {
        return neighborsConfigFile;
    }

    public void setNeighborsConfigFile(String neighborsConfigFile) {
        this.neighborsConfigFile = neighborsConfigFile;
    }

    public InputDocument getInputDocument() {
        return inputDocument;
    }

    public void setInputDocument(InputDocument inputDocument) {
        this.inputDocument = inputDocument;
    }

    public CheckpointFileParameter getCheckpointFileParameter() {
        return checkpointFileParam;
    }

    public void setCheckpointFileParameter(CheckpointFileParameter checkpointFileParam) {
        this.checkpointFileParam = checkpointFileParam;
    }

    public String getResultsFolder() {
        return resultsFolder;
    }

    public void setResultsFolder(String resultsFolder) {
        this.resultsFolder = resultsFolder;
    }

    public String getFuzzyInputFile() {
        return fuzzyInputFile;
    }

    public void setFuzzyInputFile(String fuzzyInputFile) {
        this.fuzzyInputFile = fuzzyInputFile;
    }

    
}
