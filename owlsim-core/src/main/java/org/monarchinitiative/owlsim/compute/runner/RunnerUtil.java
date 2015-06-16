package org.monarchinitiative.owlsim.compute.runner;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author cjm
 *
 */
/**
 * @author cjm
 *
 */
public class RunnerUtil {

	/**
	 * @param jsonString
	 * @return
	 */
	public static RunConfiguration generateRunConfigurationFromJsonString(String jsonString) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(jsonString, RunConfiguration.class);
	}

	public static RunConfiguration generateRunConfigurationFromJsonFile(String fn) throws IOException {
		String jsonString = FileUtils.readFileToString(new File(fn).getCanonicalFile());
		return generateRunConfigurationFromJsonString(jsonString);


	}

}
