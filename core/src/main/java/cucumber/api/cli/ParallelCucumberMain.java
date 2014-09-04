package cucumber.api.cli;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelCucumberMain {
    private static final int ERRORS = 0x1;
    private static final int NO_ERRORS = 0x00;
    private static byte exitStatus = NO_ERRORS;

    public static void main(String[] args) throws Throwable {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions(Arrays.asList(args));
        List<CucumberFeature> cucumberFeatures = resolveFeaturesToRun(classLoader, runtimeOptions);
        List<Object> realPlugins = runtimeOptions.getPlugins();

        ExecutorService executorService = createExecutorService();

        for (final CucumberFeature cucumberFeature : cucumberFeatures) {
            asyncRunFeature(args, classLoader, executorService, cucumberFeature, realPlugins);
        }
        awaitTermination(executorService);

        finishFormatting(runtimeOptions.pluginProxy(classLoader, Formatter.class));
        System.exit(exitStatus);
    }

    private static void finishFormatting(final Formatter formatter) {
        formatter.done();
        formatter.close();
    }

    private static List<CucumberFeature> resolveFeaturesToRun(final ClassLoader classLoader,
                                                              final RuntimeOptions runtimeOptions) {
        return runtimeOptions.cucumberFeatures(new MultiLoader(classLoader));
    }

    private static ExecutorService createExecutorService() {
        int numberOfThreads = getNumberOfThreads();
        return Executors.newFixedThreadPool(numberOfThreads, new ThreadFactory() {
            private AtomicInteger threadNumber = new AtomicInteger();

            @Override
            public Thread newThread(final Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("cucumber_" + threadNumber.incrementAndGet());

                return thread;
            }
        });
    }

    private static void asyncRunFeature(final String[] argv, final ClassLoader classLoader,
                                        final ExecutorService executorService, final CucumberFeature cucumberFeature,
                                        final List<Object> realPlugins) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    RuntimeOptions options = new RuntimeOptions(Arrays.asList(argv));
                    options.getPlugins().clear();
                    options.addPlugin(new RecordingPluginImpl());
                    options.getFeaturePaths().clear();
                    options.getFeaturePaths().add(cucumberFeature.getPath());

                    runFeature(classLoader, options);

                    RecordingPlugin recordingPlugin = options.pluginProxy(classLoader, RecordingPlugin.class);
                    replayOnRealFormatters(recordingPlugin, realPlugins);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        executorService.submit(runnable);
    }

    private synchronized static void replayOnRealFormatters(final RecordingPlugin recordingPlugin,
                                                            final List<Object> realPlugins) {
        for (Object plugin : realPlugins) {
            recordingPlugin.replay(plugin);
        }
    }

    private static void runFeature(final ClassLoader classLoader, final RuntimeOptions fixedRuntimeOptions) {
        try {
            final MultiLoader resourceLoader = new MultiLoader(classLoader);
            final ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
            Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, fixedRuntimeOptions);
            runtime.run();
            exitStatus |= runtime.exitStatus();
        } catch (Exception e) {
            exitStatus = ERRORS;
            e.printStackTrace();
        }
    }

    private static void awaitTermination(final ExecutorService executorService) {
        try {
            executorService.shutdown();
            executorService.awaitTermination(getTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            exitStatus = ERRORS;
            e.printStackTrace();
        }
    }

    // 15 minutes timeout
    private static int getTimeout() {
        return 60 * 15;
    }

    private static int getNumberOfThreads() {
        return 2;
    }
}
