package hooks;

import factory.DriverFactory;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

/**
 * Wraps each Gherkin step in a Playwright tracing group so the trace file
 * records which step each browser action belongs to.
 *
 * Register in TestRunner: plugin = {..., "hooks.StepTracingPlugin"}
 */
public class StepTracingPlugin implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, this::onStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onStepFinished);
    }

    private void onStepStarted(TestStepStarted event) {
        if (!(event.getTestStep() instanceof PickleStepTestStep)) {
            return;
        }
        PickleStepTestStep step = (PickleStepTestStep) event.getTestStep();
        String keyword = step.getStep().getKeyword().trim();
        String text = step.getStep().getText();
        String label = keyword + " " + text;
        try {
            DriverFactory.getContext().tracing().group(label);
        } catch (Exception ignored) {
            // Context or tracing not ready — skip silently
        }
    }

    private void onStepFinished(TestStepFinished event) {
        if (!(event.getTestStep() instanceof PickleStepTestStep)) {
            return;
        }
        try {
            DriverFactory.getContext().tracing().groupEnd();
        } catch (Exception ignored) {
            // Step group may not have been opened — skip silently
        }
    }
}