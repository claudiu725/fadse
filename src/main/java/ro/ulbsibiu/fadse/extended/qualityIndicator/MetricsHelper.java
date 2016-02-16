package ro.ulbsibiu.fadse.extended.qualityIndicator;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetricsHelper {

	static Logger logger = LogManager.getLogger();
	public MetricsHelper() {
	}
	
	public static void main(String[] args) {
		String path = "2016-02-16_09-56-57";
		if (args.length >= 2)
			path = args[1];
		Path output = Paths.get(System.getProperty("user.dir"), "results", path);
		computeAll(output);
	}
	
	public static void computeAll(Path resultsFolder)
	{
		Metadata metadata = Metadata.load(resultsFolder);
		logger.info(MetricsUtil.getListOfFiles(resultsFolder.resolve("offMONSGAII")).size());
		CoverageFromTwoFolders.coverageFromTwoFolders(metadata,
				resultsFolder.resolve("offMONSGAII"),
				resultsFolder.resolve("offMOSPEA2"),
				resultsFolder.resolve("comparison1")
				);
		MetricsUtil.getListOfFiles(resultsFolder.resolve("filled"));
		MetricsUtil.computeHypervolumeAndSevenPoint(metadata, resultsFolder.resolve("metricsComputed"), resultsFolder.resolve("filled"));
	}
	
}
