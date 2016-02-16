package ro.ulbsibiu.fadse.extended.qualityIndicator;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmetal.base.Algorithm;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.document.InputDocument;

@XmlRootElement
public class Metadata {
	static Logger logger = LogManager.getLogger();

	@XmlElement
	public int numberOfVariables;
	@XmlElement
	public int numberOfObjectives;
	@XmlElement
	public int populationSize;
	@XmlElement
	public int maxEvaluations;
	@XmlElement
	public List<String> metaheuristics;
	
	public static Metadata loadFromEnvironment(Environment env, Algorithm algorithm) {
		Metadata metadata = new Metadata();
		metadata.numberOfVariables = env.getInputDocument().getParameters().length;
		metadata.numberOfObjectives = env.getInputDocument().getObjectives().size();
        if (algorithm.getInputParameter("populationSize") != null)
        {
        	// genetic algorithm
        	metadata.populationSize = (Integer) algorithm.getInputParameter("populationSize");
        	metadata.maxEvaluations = (Integer) algorithm.getInputParameter("maxEvaluations");
        }
        else
        {
        	// particle swarm
        	metadata.populationSize = (Integer) algorithm.getInputParameter("swarmSize");
        	metadata.maxEvaluations = (Integer) algorithm.getInputParameter("maxIterations") * metadata.populationSize;
        }
        metadata.metaheuristics = new LinkedList<>();
        if (env.getInputDocument().getMetaOptimizedAlgorithms() != null && !env.getInputDocument().getMetaOptimizedAlgorithms().isEmpty())
        {
        	for (InputDocument.InputMetaOptimizedAlgorithm child : env.getInputDocument().getMetaOptimizedAlgorithms())
        	metadata.metaheuristics.add(child.getName());
        }
        else
        {
        	metadata.metaheuristics.add(env.getInputDocument().getMetaheuristicName());
        }
		return metadata;
	}

	public void save(Path folder) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Metadata.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(this, folder.resolve("metadata.xml").toFile());
			jaxbMarshaller.marshal(this, System.out);
		} catch (JAXBException e) {
			logger.error("",e);
		}
	}

	public static Metadata load(Path folder) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Metadata.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Metadata metadata = (Metadata) jaxbUnmarshaller.unmarshal(folder.resolve("metadata.xml").toFile());

			return metadata;
		} catch (JAXBException e) {
			logger.error("",e);
		}
		return null;
	}

}
