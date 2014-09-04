package cucumber.api.cli;

import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

public class RecordingPluginImpl implements RecordingPlugin {
    List<Command> commands = new ArrayList<Command>();

    public void replay(Object plugin) {
        for (Command command : commands) {
            command.apply(plugin);
        }
    }

    @Override
    public void uri(final String uri) {
        commands.add(new UriCommand(uri));
    }

    @Override
    public void feature(final Feature feature) {
        commands.add(new FeatureCommand(feature));
    }

    @Override
    public void background(final Background background) {
        commands.add(new BackgroundCommand(background));
    }

    @Override
    public void scenario(Scenario scenario) {
        commands.add(new ScenarioCommand(scenario));
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        commands.add(new ScenarioOutlineCommand(scenarioOutline));
    }

    @Override
    public void examples(Examples examples) {
        commands.add(new ExamplesCommand(examples));
    }

    @Override
    public void startOfScenarioLifeCycle(final Scenario scenario) {
        commands.add(new StartOfScenarioLifeCycleCommand(scenario));
    }

    @Override
    public void step(Step step) {
        commands.add(new StepCommand(step));
    }

    @Override
    public void endOfScenarioLifeCycle(final Scenario scenario) {
        commands.add(new EndOfScenarioLifeCycleCommand(scenario));
    }

    @Override
    public void eof() {
        commands.add(new EofCommand());
    }

    @Override
    public void syntaxError(final String state, final String event, final List<String> legalEvents, final String uri,
                            final Integer line) {
        commands.add(new SyntaxErrorCommand(state, event, legalEvents, uri, line));
    }

    @Override
    public void done() {
        // do not keep the done command
    }

    @Override
    public void close() {
        // do not keep the close command
    }

    @Override
    public void before(Match match, Result result) {
        commands.add(new BeforeCommand(match, result));
    }

    @Override
    public void result(Result result) {
        commands.add(new ResultCommand(result));
    }

    @Override
    public void after(Match match, Result result) {
        commands.add(new AfterCommand(match, result));
    }

    @Override
    public void match(final Match match) {
        commands.add(new MatchCommand(match));
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        commands.add(new EmbeddingCommand(mimeType, data));
    }

    @Override
    public void write(String text) {
        commands.add(new WriteCommand(text));
    }

    @Override
    public void stepDefinition(StepDefinition stepDefinition) {
        commands.add(new StepDefinitionCommand(stepDefinition));
    }

    private interface Command {

        void apply(Object plugin);
    }

    private static class SyntaxErrorCommand implements Command {
        private final String state;
        private final String event;
        private final List<String> legalEvents;
        private final String uri;
        private final Integer line;

        public SyntaxErrorCommand(final String state, final String event, final List<String> legalEvents,
                                  final String uri, final Integer line) {
            this.state = state;
            this.event = event;
            this.legalEvents = legalEvents;
            this.uri = uri;
            this.line = line;
        }

        @Override
        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).syntaxError(state, event, legalEvents, uri, line);
            }
        }
    }

    private static class ScenarioOutlineCommand implements Command {
        private ScenarioOutline scenarioOutline;

        public ScenarioOutlineCommand(ScenarioOutline scenarioOutline) {
            this.scenarioOutline = scenarioOutline;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).scenarioOutline(scenarioOutline);
            }
        }
    }

    private static class ExamplesCommand implements Command {
        private Examples examples;

        public ExamplesCommand(Examples examples) {
            this.examples = examples;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).examples(examples);
            }
        }
    }

    private static class StepCommand implements Command {
        private Step step;

        public StepCommand(Step step) {
            this.step = step;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).step(step);
            }
        }
    }

    private static class StartOfScenarioLifeCycleCommand implements Command {
        private Scenario scenario;

        private StartOfScenarioLifeCycleCommand(Scenario scenario) {
            this.scenario = scenario;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).startOfScenarioLifeCycle(scenario);
            }
        }
    }

    private static class EndOfScenarioLifeCycleCommand implements Command {
        private Scenario scenario;

        private EndOfScenarioLifeCycleCommand(Scenario scenario) {
            this.scenario = scenario;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).endOfScenarioLifeCycle(scenario);
            }
        }
    }


    private static class ResultCommand implements Command {
        private Result result;

        public ResultCommand(Result result) {
            this.result = result;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Reporter) {
                ((Reporter) plugin).result(result);
            }
        }
    }

    private static class WriteCommand implements Command {
        private String text;

        public WriteCommand(String text) {
            this.text = text;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Reporter) {
                ((Reporter) plugin).write(text);
            }
        }
    }

    private static class StepDefinitionCommand implements Command {
        private StepDefinition stepDefinition;

        public StepDefinitionCommand(StepDefinition stepDefinition) {
            this.stepDefinition = stepDefinition;
        }

        public void apply(Object plugin) {
            if (plugin instanceof StepDefinitionReporter) {
                ((StepDefinitionReporter) plugin).stepDefinition(stepDefinition);
            }
        }
    }

    private static class ScenarioCommand implements Command {
        private Scenario scenario;

        public ScenarioCommand(Scenario scenario) {
            this.scenario = scenario;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).scenario(scenario);
            }
        }
    }

    private static class UriCommand implements Command {
        private String uri;

        UriCommand(final String uri) {
            this.uri = uri;
        }


        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).uri(uri);
            }
        }
    }

    private static class FeatureCommand implements Command {
        private Feature feature;

        FeatureCommand(final Feature feature) {
            this.feature = feature;
        }


        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).feature(feature);
            }
        }
    }

    private static class MatchCommand implements Command {
        private Match match;

        MatchCommand(final Match match) {
            this.match = match;
        }

        @Override
        public void apply(Object plugin) {
            if (plugin instanceof Reporter) {
                ((Reporter) plugin).match(match);
            }
        }
    }

    private static class BackgroundCommand implements Command {
        private Background background;

        BackgroundCommand(final Background background) {
            this.background = background;
        }


        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).background(background);
            }
        }
    }

    private static class BeforeCommand implements Command {
        private Match match;
        private Result result;

        public BeforeCommand(Match match, Result result) {
            this.match = match;
            this.result = result;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Reporter) {
                ((Reporter) plugin).before(match, result);
            }
        }
    }

    private static class EmbeddingCommand implements Command {
        private String mimeType;
        private byte[] data;

        public EmbeddingCommand(String mimeType, byte[] data) {
            this.mimeType = mimeType;
            this.data = data;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Reporter) {
                ((Reporter) plugin).embedding(mimeType, data);
            }
        }
    }

    private static class AfterCommand implements Command {
        private Match match;
        private Result result;

        public AfterCommand(Match match, Result result) {
            this.match = match;
            this.result = result;
        }

        public void apply(Object plugin) {
            if (plugin instanceof Reporter) {
                ((Reporter) plugin).after(match, result);
            }
        }
    }

    private static class EofCommand implements Command {

        public void apply(Object plugin) {
            if (plugin instanceof Formatter) {
                ((Formatter) plugin).eof();
            }
        }
    }

}
