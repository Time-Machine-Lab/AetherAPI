package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.domain.importagent.model.ImportAgentDomainException;
import io.github.timemachinelab.service.application.ApiImportAgentApplicationService;
import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.ImportAgentRunStatus;
import io.github.timemachinelab.service.model.ImportAgentStepType;
import io.github.timemachinelab.service.model.ImportAiProfileModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.service.model.ConfirmImportAgentPlanCommand;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentSessionStatus;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportStepResultStatus;
import io.github.timemachinelab.service.model.StartImportAgentRunCommand;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import io.github.timemachinelab.service.port.in.CategoryUseCase;
import io.github.timemachinelab.service.port.out.ApiImportAgentPlannerPort;
import io.github.timemachinelab.service.port.out.ApiImportAgentRunRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiImportAgentSessionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class ApiImportAgentApplicationServiceTest {

    @Test
    @DisplayName("run should reject execution before explicit confirmation")
    void shouldRejectExecutionBeforeConfirmation() {
        ApiImportAgentSessionRepositoryPort sessionRepositoryPort = mock(ApiImportAgentSessionRepositoryPort.class);
        ApiImportAgentRunRepositoryPort runRepositoryPort = mock(ApiImportAgentRunRepositoryPort.class);
        ApiImportAgentPlannerPort plannerPort = mock(ApiImportAgentPlannerPort.class);
        CategoryUseCase categoryUseCase = mock(CategoryUseCase.class);
        ApiAssetUseCase apiAssetUseCase = mock(ApiAssetUseCase.class);
        ApiImportAgentApplicationService service = new ApiImportAgentApplicationService(
                sessionRepositoryPort,
                runRepositoryPort,
                plannerPort,
                categoryUseCase,
                apiAssetUseCase
        );
        ApiImportAgentSessionModel session = new ApiImportAgentSessionModel(
                "session-1",
                "user-1",
                ImportAgentSessionStatus.WAITING_FOR_CONFIRMATION,
                "https://docs.example.com/weather",
                "{}",
                "import weather api",
                "Alice",
                1,
                null,
                null,
                null,
                new ImportAgentPlanModel(
                        1,
                        true,
                        "one asset ready",
                        List.of(),
                        List.of(),
                        List.of(new ImportAssetPlanModel(
                                "weather-forecast",
                                "Weather Forecast",
                                AssetType.STANDARD_API,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                false,
                                null
                        ))
                ),
                List.of(),
                "2026-05-18T10:00:00Z",
                "2026-05-18T10:00:00Z"
        );
        when(sessionRepositoryPort.findOwnedSession("user-1", "session-1")).thenReturn(Optional.of(session));

        ImportAgentDomainException exception = assertThrows(
                ImportAgentDomainException.class,
                () -> service.startRun(new StartImportAgentRunCommand("user-1", "Alice", "session-1", 1))
        );

        assertEquals("Import plan confirmation required", exception.getMessage());
        verify(runRepositoryPort, never()).saveRun(any());
        verify(apiAssetUseCase, never()).registerAsset(any());
        verify(categoryUseCase, never()).createCategory(any());
    }

    @Test
    @DisplayName("confirm should reject non executable plan")
    void shouldRejectConfirmingNonExecutablePlan() {
        ApiImportAgentSessionRepositoryPort sessionRepositoryPort = mock(ApiImportAgentSessionRepositoryPort.class);
        ApiImportAgentRunRepositoryPort runRepositoryPort = mock(ApiImportAgentRunRepositoryPort.class);
        ApiImportAgentPlannerPort plannerPort = mock(ApiImportAgentPlannerPort.class);
        CategoryUseCase categoryUseCase = mock(CategoryUseCase.class);
        ApiAssetUseCase apiAssetUseCase = mock(ApiAssetUseCase.class);
        ApiImportAgentApplicationService service = new ApiImportAgentApplicationService(
                sessionRepositoryPort,
                runRepositoryPort,
                plannerPort,
                categoryUseCase,
                apiAssetUseCase
        );
        ApiImportAgentSessionModel session = new ApiImportAgentSessionModel(
                "session-1",
                "user-1",
                ImportAgentSessionStatus.WAITING_FOR_CLARIFICATION,
                null,
                null,
                "import weather api",
                "Alice",
                2,
                null,
                null,
                null,
                new ImportAgentPlanModel(2, false, "need more fields", List.of("apiCode missing"), List.of(), List.of()),
                List.of(),
                "2026-05-18T10:00:00Z",
                "2026-05-18T10:00:00Z"
        );
        when(sessionRepositoryPort.findOwnedSession("user-1", "session-1")).thenReturn(Optional.of(session));

        ImportAgentDomainException exception = assertThrows(
                ImportAgentDomainException.class,
                () -> service.confirmPlan(new ConfirmImportAgentPlanCommand("user-1", "session-1", 2))
        );

        assertEquals("Import plan is not executable", exception.getMessage());
        verify(sessionRepositoryPort, never()).saveSession(any());
    }

    @Test
    @DisplayName("run should orchestrate category ensure, register, ai profile attach and publish")
    void shouldExecuteConfirmedPlanThroughExistingUseCases() {
        ApiImportAgentSessionRepositoryPort sessionRepositoryPort = mock(ApiImportAgentSessionRepositoryPort.class);
        ApiImportAgentRunRepositoryPort runRepositoryPort = mock(ApiImportAgentRunRepositoryPort.class);
        ApiImportAgentPlannerPort plannerPort = mock(ApiImportAgentPlannerPort.class);
        CategoryUseCase categoryUseCase = mock(CategoryUseCase.class);
        ApiAssetUseCase apiAssetUseCase = mock(ApiAssetUseCase.class);
        ApiImportAgentApplicationService service = new ApiImportAgentApplicationService(
                sessionRepositoryPort,
                runRepositoryPort,
                plannerPort,
                categoryUseCase,
                apiAssetUseCase
        );
        when(sessionRepositoryPort.findOwnedSession("user-1", "session-1"))
                .thenReturn(Optional.of(confirmedSession(executablePlan())));
        when(categoryUseCase.getCategoryByCode("tools")).thenThrow(new RuntimeException("Category not found"));
        when(apiAssetUseCase.getAssetByCode("user-1", "weather-forecast"))
                .thenThrow(new AssetDomainException("Asset not found"));

        ApiImportAgentRunModel result = service.startRun(new StartImportAgentRunCommand("user-1", "Alice", "session-1", 1));

        verify(categoryUseCase).createCategory(any());
        verify(apiAssetUseCase).registerAsset(any());
        verify(apiAssetUseCase).attachAiCapabilityProfile(any());
        verify(apiAssetUseCase).publishAsset("user-1", "Alice", "weather-forecast");
        verify(runRepositoryPort, atLeastOnce()).saveRun(any());
        assertEquals(ImportAgentRunStatus.SUCCEEDED, result.getStatus());
        assertEquals(List.of("weather-forecast"), result.getAffectedApiCodes());
        assertEquals(4, result.getStepResults().size());
        assertEquals(ImportAgentStepType.ENSURE_CATEGORY, result.getStepResults().get(0).getStepType());
        assertEquals(ImportAgentStepType.PUBLISH_ASSET, result.getStepResults().get(3).getStepType());
    }

    @Test
    @DisplayName("run should record failed execution steps and final failure state")
    void shouldRecordFailureWhenAssetExecutionFails() {
        ApiImportAgentSessionRepositoryPort sessionRepositoryPort = mock(ApiImportAgentSessionRepositoryPort.class);
        ApiImportAgentRunRepositoryPort runRepositoryPort = mock(ApiImportAgentRunRepositoryPort.class);
        ApiImportAgentPlannerPort plannerPort = mock(ApiImportAgentPlannerPort.class);
        CategoryUseCase categoryUseCase = mock(CategoryUseCase.class);
        ApiAssetUseCase apiAssetUseCase = mock(ApiAssetUseCase.class);
        ApiImportAgentApplicationService service = new ApiImportAgentApplicationService(
                sessionRepositoryPort,
                runRepositoryPort,
                plannerPort,
                categoryUseCase,
                apiAssetUseCase
        );
        when(sessionRepositoryPort.findOwnedSession("user-1", "session-1"))
                .thenReturn(Optional.of(confirmedSession(executablePlan())));
        when(categoryUseCase.getCategoryByCode("tools")).thenThrow(new RuntimeException("Category not found"));
        when(apiAssetUseCase.getAssetByCode("user-1", "weather-forecast"))
                .thenThrow(new AssetDomainException("Asset not found"));
        doThrow(new RuntimeException("register failed"))
                .when(apiAssetUseCase).registerAsset(any());

        ApiImportAgentRunModel result = service.startRun(new StartImportAgentRunCommand("user-1", "Alice", "session-1", 1));

        ArgumentCaptor<ApiImportAgentRunModel> runCaptor = ArgumentCaptor.forClass(ApiImportAgentRunModel.class);
        verify(runRepositoryPort, atLeastOnce()).saveRun(runCaptor.capture());
        ApiImportAgentRunModel finalRun = runCaptor.getAllValues().get(runCaptor.getAllValues().size() - 1);
        assertEquals(ImportAgentRunStatus.PARTIALLY_FAILED, result.getStatus());
        assertEquals(ImportAgentRunStatus.PARTIALLY_FAILED, finalRun.getStatus());
        assertEquals("register failed", finalRun.getFailureReason());
        assertNotNull(finalRun.getStepResults());
        assertEquals(2, finalRun.getStepResults().size());
        assertEquals(ImportAgentStepType.REVISE_ASSET, finalRun.getStepResults().get(1).getStepType());
        assertEquals(ImportStepResultStatus.FAILED, finalRun.getStepResults().get(1).getStatus());
    }

    private ApiImportAgentSessionModel confirmedSession(ImportAgentPlanModel plan) {
        return new ApiImportAgentSessionModel(
                "session-1",
                "user-1",
                ImportAgentSessionStatus.CONFIRMED,
                "https://docs.example.com/weather",
                "summary",
                "import weather api",
                "Alice",
                1,
                1,
                null,
                "2026-05-18T10:00:00Z",
                plan,
                List.of(),
                "2026-05-18T09:55:00Z",
                "2026-05-18T10:00:00Z"
        );
    }

    private ImportAgentPlanModel executablePlan() {
        return new ImportAgentPlanModel(
                1,
                true,
                "ready",
                List.of(),
                List.of(new ImportCategoryPlanModel("tools", "Tools", ImportCategoryPlanAction.CREATE_IF_MISSING)),
                List.of(new ImportAssetPlanModel(
                        "weather-forecast",
                        "Weather Forecast",
                        AssetType.AI_API,
                        "tools",
                        RequestMethod.GET,
                        "https://upstream.example.com/weather",
                        AuthScheme.HEADER_TOKEN,
                        "Authorization: Bearer upstream-token",
                        "template",
                        "{\"city\":\"Shanghai\"}",
                        "{\"temperature\":26}",
                        "{\"type\":\"object\"}",
                        "{\"type\":\"object\"}",
                        true,
                        new ImportAiProfileModel("OpenAI", "gpt-4.1", true, List.of("chat"))
                ))
        );
    }
}