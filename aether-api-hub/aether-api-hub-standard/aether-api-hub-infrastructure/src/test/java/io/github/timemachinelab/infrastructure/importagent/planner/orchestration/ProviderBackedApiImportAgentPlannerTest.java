package io.github.timemachinelab.infrastructure.importagent.planner.orchestration;

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
        ProviderBackedApiImportAgentPlanner planner = new ProviderBackedApiImportAgentPlanner(List.of(
                new StubPlannerProvider(false, "skipped"),
                new StubPlannerProvider(true, "chosen"),
                new StubPlannerProvider(true, "later")
        ));

        ImportAgentPlannerResult result = planner.plan(request(3));

        assertEquals("chosen", result.getAgentMessage());
        assertEquals(3, result.getPlan().getVersion());
    }

    @Test
    @DisplayName("planner should fail fast when earlier provider fails")
    void shouldFailFastWhenEarlierProviderFails() {
        ProviderBackedApiImportAgentPlanner planner = new ProviderBackedApiImportAgentPlanner(List.of(
                new FailingPlannerProvider(),
                new StubPlannerProvider(true, "fallback")
        ));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> planner.plan(request(5)));

        assertEquals("Import agent planner provider failed", exception.getMessage());
        assertEquals("simulated llm failure", exception.getCause().getMessage());
    }

    private static ImportAgentPlannerRequest request(int version) {
        return new ImportAgentPlannerRequest(null, null, "import weather api", "continue", null, version, List.of());
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
                    message);
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
