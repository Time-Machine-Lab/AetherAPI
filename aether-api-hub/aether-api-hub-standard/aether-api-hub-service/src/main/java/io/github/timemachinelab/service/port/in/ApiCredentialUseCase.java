package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ApiCredentialModel;
import io.github.timemachinelab.service.model.ApiCredentialPageResult;
import io.github.timemachinelab.service.model.DisableApiCredentialCommand;
import io.github.timemachinelab.service.model.EnableApiCredentialCommand;
import io.github.timemachinelab.service.model.GetApiCredentialDetailQuery;
import io.github.timemachinelab.service.model.IssueApiCredentialCommand;
import io.github.timemachinelab.service.model.IssuedApiCredentialModel;
import io.github.timemachinelab.service.model.ListApiCredentialQuery;
import io.github.timemachinelab.service.model.RevokeApiCredentialCommand;

/**
 * API 凭证管理用例入口。
 */
public interface ApiCredentialUseCase {

    IssuedApiCredentialModel issueApiCredential(IssueApiCredentialCommand command);

    ApiCredentialPageResult listApiCredentials(ListApiCredentialQuery query);

    ApiCredentialModel getApiCredentialDetail(GetApiCredentialDetailQuery query);

    ApiCredentialModel enableApiCredential(EnableApiCredentialCommand command);

    ApiCredentialModel disableApiCredential(DisableApiCredentialCommand command);

    ApiCredentialModel revokeApiCredential(RevokeApiCredentialCommand command);
}
