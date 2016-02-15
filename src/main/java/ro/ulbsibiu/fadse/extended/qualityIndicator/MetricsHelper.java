package ro.ulbsibiu.fadse.extended.qualityIndicator;

import java.nio.file.Path;
import java.nio.file.Paths;

import ro.ulbsibiu.fadse.environment.Environment;

public class MetricsHelper {

	public MetricsHelper() {
	}
	
	public static void main(String[] args) {
		Environment env = new Environment();
		//Path output = Paths.get("C:\Users\Claudiu\git\fadse\results\2016-02-15_18-29-14");
		//env.setResultsFolder(output.toString());
		//computeAll(Paths.get(args[1]).toString());
	}
	
	public static void computeAll(Environment env)
	{
		Path outputFolder = Paths.get(env.getResultsFolder());
		MetricsUtil.getListOfFiles(outputFolder);
	}
	
}
