package com.anax.devops.app.application.port.in;

import com.anax.devops.app.domain.model.DevOpsRequest;
import com.anax.devops.app.domain.model.DevOpsResponse;

public interface ProcessMessageUseCase {
    DevOpsResponse execute(DevOpsRequest request);
}