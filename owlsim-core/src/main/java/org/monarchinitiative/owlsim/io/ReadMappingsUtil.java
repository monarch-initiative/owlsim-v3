package org.monarchinitiative.owlsim.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class ReadMappingsUtil {
	
	public static Map<String,String> readPairwiseMappingsFromTsv(String path) throws IOException {
		File f = new File(path);
		Map<String,String> mappings = new HashMap<>();
		List<String> lines = FileUtils.readLines(f);
		for (String line : lines) {
			String[] vals = line.split("\t", 2);
			mappings.put(vals[0], vals[1]);
		}
		return mappings;
	}
}
