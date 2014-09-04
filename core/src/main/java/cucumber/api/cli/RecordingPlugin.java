package cucumber.api.cli;

import cucumber.api.StepDefinitionReporter;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

public interface RecordingPlugin extends Reporter, Formatter, StepDefinitionReporter {

    void replay(Object plugin);
}
