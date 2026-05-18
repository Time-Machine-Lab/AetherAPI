package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderBackedApiImportAgentPlannerTest {

    @Test
    @DisplayName("planner should delegate to the first supporting provider")
    void shouldDelegateToFirstSupportingProvider() {
        ProviderBackedApiImportAgentPlanner planner = planner(false, List.of(
                new StubPlannerProvider(false, "skipped"),
                new StubPlannerProvider(true, "chosen"),
                new StubPlannerProvider(true, "later")
        ));

        ImportAgentPlannerResult result = planner.plan(new ImportAgentPlannerRequest(
                null,
                null,
                "import weather api",
                "continue",
                null,
                3,
                List.of()
        ));

        assertEquals("chosen", result.getAgentMessage());
        assertEquals(3, result.getPlan().getVersion());
    }

    @Test
    @DisplayName("planner should fall back to next provider when earlier provider fails and fallback is enabled")
    void shouldFallBackWhenEarlierProviderFailsAndFallbackEnabled() {
        ProviderBackedApiImportAgentPlanner planner = planner(true, List.of(
                new FailingPlannerProvider(),
                new StubPlannerProvider(true, "fallback")
        ));

        ImportAgentPlannerResult result = planner.plan(new ImportAgentPlannerRequest(
                null,
                null,
                "import weather api",
                "continue",
                null,
                4,
                List.of()
        ));

        assertEquals("fallback", result.getAgentMessage());
        assertEquals(4, result.getPlan().getVersion());
    }

    @Test
    @DisplayName("planner should fail fast when earlier provider fails and fallback is disabled")
    void shouldFailFastWhenEarlierProviderFailsAndFallbackDisabled() {
        ProviderBackedApiImportAgentPlanner planner = planner(false, List.of(
                new FailingPlannerProvider(),
                new StubPlannerProvider(true, "fallback")
        ));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> planner.plan(new ImportAgentPlannerRequest(
                null,
                null,
                "import weather api",
                "continue",
                null,
                5,
                List.of()
        )));

        assertEquals("Import agent planner provider failed and provider fallback is disabled", exception.getMessage());
        assertEquals("simulated llm failure", exception.getCause().getMessage());
    }

    private static ProviderBackedApiImportAgentPlanner planner(
            boolean allowProviderFallback,
            List<ImportAgentPlannerProvider> providers) {
        ImportAgentPlannerProperties properties = new ImportAgentPlannerProperties();
        properties.setAllowProviderFallback(allowProviderFallback);
        return new ProviderBackedApiImportAgentPlanner(providers, properties);
    }

    private static final class StubPlannerProvider implements ImportAgentPlannerProvider {

        private final boolean supported;
        private final String message;

        private StubPlannerProvider(boolean supported, String message) {
            this.supported = supported;
            this.message = message;
        }

        @Override
        public boolean supports(ImportAgentPlannerRequest request) {
            return supported;
        }

        @Override
        public ImportAgentPlannerResult plan(ImportAgentPlannerRequest request) {
            return new ImportAgentPlannerResult(
                    new ImportAgentPlanModel(request.getNextPlanVersion(), false, message, List.of(), List.of(), List.of()),
                    message
            );
        }
    }

    private static final class FailingPlannerProvider implements ImportAgentPlannerProvider {

        @Override
        public boolean supports(ImportAgentPlannerRequest request) {
            return true;
        }

        @Override
        public ImportAgentPlannerResult plan(ImportAgentPlannerRequest request) {
            throw new IllegalStateException("simulated llm failure");
        }
    }
}