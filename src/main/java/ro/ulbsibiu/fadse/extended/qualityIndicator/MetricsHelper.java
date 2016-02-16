package ro.ulbsibiu.fadse.extended.qualityIndicator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetricsHelper {

	static Logger logger = LogManager.getLogger();

	public MetricsHelper() {
	}

	public static void main(String[] args) {
		if (args.length < 1)
		{
			logger.error("Please provide the results folder");
			return;
		}
		String path = args[0];
		Path output = Paths.get(System.getProperty("user.dir"), "results", path);
		computeAll(output);
	}

	public static void computeAll(Path resultsFolder) {
		logger.info("Building metrics for " + resultsFolder.toString());
		Metadata metadata = Metadata.load(resultsFolder);
		Path offMONSGAIIPath = resultsFolder.resolve("offMONSGAII");
		Path offMOSPEAIIPath = resultsFolder.resolve("offMOSPEA2");
		Path metricsComputedFolder = resultsFolder.resolve("metricsComputed");
		Path corrected = resultsFolder.resolve("corrected");
		Path filled = resultsFolder.resolve("filled");
		Path metricsComputed = resultsFolder.resolve("metricsComputed");
		if (offMONSGAIIPath.toFile().exists())
			CoverageFromTwoFolders.coverageFromTwoFolders(metadata, offMONSGAIIPath, offMOSPEAIIPath, metricsComputedFolder);
		if (filled.toFile().exists())
			MetricsUtil.computeHypervolumeAndSevenPoint(metadata, metricsComputed, filled);
		else if (corrected.toFile().exists())
			MetricsUtil.computeHypervolumeAndSevenPoint(metadata, metricsComputed, corrected);
		logger.info("Done building metrics for " + resultsFolder.toString());
	}

}
