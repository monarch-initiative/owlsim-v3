package org.monarchinitiative.owlsim.services.modules;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;

import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

// CLONED FROM SERVICES
public class MatcherModule extends AbstractModule {

	// The package containing ProfileMatcher implementations
	private static final String matcherPackage = "org.monarchinitiative.owlsim.compute.matcher.impl";

	@Override
	protected void configure() {
	}

	/***
	 * Gets of map of ProfileMatchers.
	 * 
	 * <p>
	 * A convenience method to obviate maintaining hard coded instances of
	 * ProfileMatchers. <em>matcherPackage</em> is inspected for any
	 * non-abstract class that implements ProfileMatcher and a map is created
	 * between that ProfileMatcher's shortName and an instance of the matcher.
	 * 
	 * <p>
	 * <em>Note:</em> The class must be injectable by Guice.
	 * 
	 * @param injector
	 * @return A mapping of ProfileMatchers
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Provides
	Map<String, ProfileMatcher> getMatchers() throws IOException, InstantiationException, IllegalAccessException {
		ClassPath classpath = ClassPath.from(getClass().getClassLoader());

		Map<String, ProfileMatcher> matcherMap = new HashMap<>();
		for (ClassPath.ClassInfo info : classpath.getTopLevelClasses(matcherPackage)) {
			Class<?> clazz = info.load();
			if (!Modifier.isAbstract(clazz.getModifiers()) && ProfileMatcher.class.isAssignableFrom(info.load())) {

				ProfileMatcher matcher = (ProfileMatcher) clazz.newInstance();
				matcherMap.put(matcher.getShortName(), matcher);
			}
		}
		return matcherMap;
	}

}
