package software.betamax.junit;

import com.google.common.base.Strings;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import software.betamax.ComposedMatchRule;
import software.betamax.MatchRule;
import software.betamax.Recorder;
import software.betamax.TapeMode;

import java.util.logging.Logger;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class RecorderResource extends ExternalResource {

    private static final Logger log = Logger.getLogger(RecorderResource.class.getName());

    private final Description description;
    private final Recorder recorder;

    public RecorderResource(final Description description,
                            final Recorder recorder) {
        this.description = description;
        this.recorder = recorder;
    }

    @Override
    protected void before() throws Throwable {

        final Betamax annotation = description.getAnnotation(Betamax.class);
        if (annotation == null) {
            log.fine(String.format("no @Betamax annotation on '%s'", description.getDisplayName()));
            return;
        }

        log.fine(String.format("found @Betamax annotation on '%s'", description.getDisplayName()));

        String tapeName = annotation.tape();
        if (Strings.isNullOrEmpty(tapeName)) {
            tapeName = defaultTapeName(description);
        }

        final TapeMode tapeMode = annotation.mode();
        final MatchRule matchRule = ComposedMatchRule.of(annotation.match());

        recorder.start(tapeName, tapeMode, matchRule);
    }

    @Override
    protected void after() {

        // don't stop the recorder if there was no annotation on the class or method
        final Betamax annotation = description.getAnnotation(Betamax.class);
        if (annotation != null) {
            recorder.stop();
        }
    }

    protected String defaultTapeName(final Description description) {
        String name;
        if (description.getMethodName() != null) {
            name = LOWER_CAMEL.to(LOWER_UNDERSCORE, description.getMethodName());
        } else {
            name = UPPER_CAMEL.to(LOWER_UNDERSCORE, description.getTestClass().getSimpleName());
        }

        return name.replace('_', ' ');
    }
}