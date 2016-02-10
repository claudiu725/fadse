package ro.ulbsibiu.fadse.environment;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.io.XMLInputReader;
import ro.ulbsibiu.fadse.persistence.ConnectionPool;

public class Environment implements Serializable {

	static Logger logger = Logger.getLogger(Environment.class.getName());
    private String neighborsConfigFile;
    private CheckpointFileParameter checkpointFileParam;
    private String fuzzyInputFile;
    private InputDocument inputDocument;
    private Path resultsFolder;

    public Environment(String inputFilePath)
    {
    	init(Paths.get(inputFilePath));
    }
    
    public Environment(Path inputFilePath) {
        init(inputFilePath);
    }
    
    private void init(Path inputFilePath)
    {
    	inputDocument = (new XMLInputReader()).parse(inputFilePath);
        ConnectionPool.setInputDocument(inputDocument);
        String currentdir = System.getProperty("user.dir");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        String date = dateFormat.format(new Date());
        Path resultsFolder = Paths.get(currentdir, "results", date);
        try {
			Files.createDirectories(resultsFolder);
		} catch (IOException e) {
			logger.fatal("Could not create results folder " + resultsFolder.toString(), e);
		}
        logger.info("Output folder is " + resultsFolder.toString());
    }
    
    public Path getAlgorithmFolder(String algorithmName)
    {
    	return resultsFolder.resolve(algorithmName + ".log");
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

    public Path getResultsFolder() {
        return resultsFolder;
    }

    public void setResultsFolder(Path resultsFolder) {
        this.resultsFolder = resultsFolder;
    }

    public String getFuzzyInputFile() {
        return fuzzyInputFile;
    }

    public void setFuzzyInputFile(String fuzzyInputFile) {
        this.fuzzyInputFile = fuzzyInputFile;
    }

    
}
