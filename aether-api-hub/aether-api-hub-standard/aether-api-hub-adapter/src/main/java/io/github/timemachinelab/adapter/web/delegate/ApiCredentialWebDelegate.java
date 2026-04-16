package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.CreateApiCredentialReq;
import io.github.timemachinelab.api.resp.ApiCredentialPageResp;
import io.github.timemachinelab.api.resp.ApiCredentialResp;
import io.github.timemachinelab.api.resp.IssuedApiCredentialResp;
import io.github.timemachinelab.api.resp.LastUsedSnapshotResp;
import io.github.timemachinelab.service.model.ApiCredentialModel;
import io.github.timemachinelab.service.model.ApiCredentialPageResult;
import io.github.timemachinelab.service.model.DisableApiCredentialCommand;
import io.github.timemachinelab.service.model.EnableApiCredentialCommand;
import io.github.timemachinelab.service.model.GetApiCredentialDetailQuery;
import io.github.timemachinelab.service.model.IssueApiCredentialCommand;
import io.github.timemachinelab.service.model.IssuedApiCredentialModel;
import io.github.timemachinelab.service.model.ListApiCredentialQuery;
import io.github.timemachinelab.service.model.RevokeApiCredentialCommand;
import io.github.timemachinelab.service.port.in.ApiCredentialUseCase;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

/**
 * API 凭证 Web Delegate。
 */
@Component
public class ApiCredentialWebDelegate {

    private final ApiCredentialUseCase apiCredentialUseCase;

    public ApiCredentialWebDelegate(ApiCredentialUseCase apiCredentialUseCase) {
        this.apiCredentialUseCase = apiCredentialUseCase;
    }

    public IssuedApiCredentialResp createApiCredential(String currentUserId, CreateApiCredentialReq req) {
        IssuedApiCredentialModel model = apiCredentialUseCase.issueApiCredential(new IssueApiCredentialCommand(
                currentUserId,
                req.getCredentialName(),
                req.getCredentialDescription(),
                parseInstant(req.getExpireAt())
        ));
        return toIssuedResp(model);
    }

    public ApiCredentialPageResp listApiCredentials(String currentUserId, String status, int page, int size) {
        ApiCredentialPageResult result = apiCredentialUseCase.listApiCredentials(
                new ListApiCredentialQuery(currentUserId, status, page, size));
        return new ApiCredentialPageResp(
                result.getItems().stream().map(this::toResp).collect(Collectors.toList()),
                result.getPage(),
                result.getSize(),
                result.getTotal()
        );
    }

    public ApiCredentialResp getApiCredentialDetail(String currentUserId, String credentialId) {
        return toResp(apiCredentialUseCase.getApiCredentialDetail(
                new GetApiCredentialDetailQuery(currentUserId, credentialId)));
    }

    public ApiCredentialResp enableApiCredential(String currentUserId, String credentialId) {
        return toResp(apiCredentialUseCase.enableApiCredential(
                new EnableApiCredentialCommand(currentUserId, credentialId)));
    }

    public ApiCredentialResp disableApiCredential(String currentUserId, String credentialId) {
        return toResp(apiCredentialUseCase.disableApiCredential(
                new DisableApiCredentialCommand(currentUserId, credentialId)));
    }

    public ApiCredentialResp revokeApiCredential(String currentUserId, String credentialId) {
        return toResp(apiCredentialUseCase.revokeApiCredential(
                new RevokeApiCredentialCommand(currentUserId, credentialId)));
    }

    private ApiCredentialResp toResp(ApiCredentialModel model) {
        return new ApiCredentialResp(
                model.getCredentialId(),
                model.getCredentialCode(),
                model.getCredentialName(),
                model.getCredentialDescription(),
                model.getMaskedKey(),
                model.getKeyPrefix(),
                model.getStatus(),
                model.getExpireAt(),
                model.getRevokedAt(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                toSnapshotResp(model)
        );
    }

    private IssuedApiCredentialResp toIssuedResp(IssuedApiCredentialModel model) {
        return new IssuedApiCredentialResp(
                model.getCredentialId(),
                model.getCredentialCode(),
                model.getCredentialName(),
                model.getCredentialDescription(),
                model.getMaskedKey(),
                model.getKeyPrefix(),
                model.getStatus(),
                model.getExpireAt(),
                model.getRevokedAt(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                toSnapshotResp(model),
                model.getPlaintextKey()
        );
    }

    private LastUsedSnapshotResp toSnapshotResp(ApiCredentialModel model) {
        return model.getLastUsedSnapshot() == null
                ? new LastUsedSnapshotResp(null, null, null)
                : new LastUsedSnapshotResp(
                        model.getLastUsedSnapshot().getLastUsedAt(),
                        model.getLastUsedSnapshot().getLastUsedChannel(),
                        model.getLastUsedSnapshot().getLastUsedResult()
                );
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("ExpireAt must be a valid ISO-8601 date-time");
        }
    }
}
