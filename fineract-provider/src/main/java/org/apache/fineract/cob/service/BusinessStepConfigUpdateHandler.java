/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cob.service;

import com.google.common.base.Splitter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.cob.exceptions.BusinessStepNotBelongsToJobException;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@CommandType(entity = "BATCH_BUSINESS_STEP", action = "UPDATE")
public class BusinessStepConfigUpdateHandler implements NewCommandSourceHandler {

    private final ConfigJobParameterService configJobParameterService;

    @Override
    @Transactional
    public CommandProcessingResult processCommand(JsonCommand command) throws BusinessStepNotBelongsToJobException, BusinessStepException {
        List<String> split = Splitter.on("/").splitToList(command.getUrl());
        String jobName = split.get(split.size() - 2);
        if ("null".equals(jobName)) {
            jobName = null;
        }
        return configJobParameterService.updateStepConfigByJobName(command, jobName);
    }
}
