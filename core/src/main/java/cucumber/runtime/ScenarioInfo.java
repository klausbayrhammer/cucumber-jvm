package cucumber.runtime;

import gherkin.formatter.model.Tag;

import java.util.Set;

public interface ScenarioInfo {

    /**
     * Get the name of the scenario
     * @return the scenarios name
     */
    String getScenarioName();

    /**
     * A set of {@link Tag}s specified on the feature or the scenario
     * @return
     */
    Set<Tag> getTags();
}
