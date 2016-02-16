package ro.ulbsibiu.fadse.extended.qualityIndicator;

import java.nio.file.Path;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.ulbsibiu.fadse.environment.Environment;

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

	public static Metadata loadFromEnvironment(Environment env) {
		Metadata metadata = new Metadata();
		metadata.numberOfVariables = env.getInputDocument().getParameters().length;
		metadata.numberOfObjectives = env.getInputDocument().getObjectives().size();
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
