package org.monarchinitiative.owlsim.compute.matcher;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.impl.JaccardSimilarityProfileMatcher;

import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

public class MatcherMapModule extends AbstractModule {

    private Logger LOG = Logger.getLogger(MatcherMapModule.class);

	// The package containing ProfileMatcher implementations
	private static final String matcherPackage = "org.monarchinitiative.owlsim.compute.matcher.impl";

	@Override
	protected void configure() {
	}

	/***
	 * Gets of map of ProfileMatchers.
	 * 
	 * <p>A convenience method to obviate maintaining hard coded instances of ProfileMatchers.
	 * <em>matcherPackage</em> is inspected for any non-abstract class that implements ProfileMatcher
	 * and a map is created between that ProfileMatcher's shortName and an instance of the matcher.
	 * 
	 * <p><em>Note:</em> The class must be injectable by Guice.
	 * 
	 * @param injector
	 * @return A mapping of ProfileMatchers
	 * @throws IOException
	 */
	@Provides
	Map<String, ProfileMatcher> getMatchers(Injector injector) throws IOException {
		ClassPath classpath = ClassPath.from(getClass().getClassLoader());
		LOG.info("Fetchig classes from: "+classpath.getClass());
		LOG.info("top level of :"+matcherPackage);

		Map<String, ProfileMatcher> matcherMap = new HashMap<>();
		for (ClassPath.ClassInfo info: classpath.getTopLevelClasses(matcherPackage)) {
 			Class<?> clazz = info.load();
            LOG.info(" Adding: "+info + " class: "+clazz + " ISAB:"+
                    Modifier.isAbstract(clazz.getModifiers()));
			if (!Modifier.isAbstract(clazz.getModifiers()) &&
					ProfileMatcher.class.isAssignableFrom(info.load())) {
				ProfileMatcher matcher = (ProfileMatcher) injector.getInstance(clazz);
				matcherMap.put(matcher.getShortName(), matcher);
			}
		}
		
		return matcherMap;
	}

}
