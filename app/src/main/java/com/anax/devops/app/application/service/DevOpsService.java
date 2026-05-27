package com.anax.devops.app.application.service;

import com.anax.devops.app.application.port.in.ProcessMessageUseCase;
import com.anax.devops.app.domain.model.DevOpsRequest;
import com.anax.devops.app.domain.model.DevOpsResponse;
import org.springframework.stereotype.Service;

@Service
public class DevOpsService implements ProcessMessageUseCase {

    private static final String RESPONSE_TEMPLATE = "Hello %s your message will be sent";

    @Override
    public DevOpsResponse execute(DevOpsRequest request) {
        String formattedMessage = String.format(RESPONSE_TEMPLATE, request.getTo());
        return new DevOpsResponse(formattedMessage);
    }
}