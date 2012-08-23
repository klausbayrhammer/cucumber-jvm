package cucumber.runtime.model;

import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberScenarioTest {

    @Test
    public void getScenarioName() {

        Scenario gherkinScenario = mock(Scenario.class);
        when(gherkinScenario.getName()).thenReturn("scenarioName");

        CucumberScenario scenario = new CucumberScenario(
                mock(CucumberFeature.class), mock(CucumberBackground.class), gherkinScenario);

        String actualScenarioName = scenario.getScenarioName();
        assertThat(actualScenarioName, is("scenarioName"));
    }

    @Test
    public void getTags() {
        Tag expectedTag1 = new Tag("tag", 1);
        Tag expectedTag2 = new Tag("tag2", 2);


        Feature feature = mock(Feature.class);
        when(feature.getTags()).thenReturn(Arrays.asList(expectedTag1));
        CucumberFeature cucumberFeature = mock(CucumberFeature.class);
        when(cucumberFeature.getFeature()).thenReturn(feature);

        Scenario gherkinScenario = mock(Scenario.class);
        when(gherkinScenario.getTags()).thenReturn(Arrays.asList(expectedTag2));

        CucumberScenario scenario = new CucumberScenario(
                cucumberFeature, mock(CucumberBackground.class), gherkinScenario);

        Set<Tag> tags = scenario.getTags();

        assertThat(tags.size(), is(2));

        assertTrue(tags.contains(expectedTag1));
        assertTrue(tags.contains(expectedTag2));
    }
}
