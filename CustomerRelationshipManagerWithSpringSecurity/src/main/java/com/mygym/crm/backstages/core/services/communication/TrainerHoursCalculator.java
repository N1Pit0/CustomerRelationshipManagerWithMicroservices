package com.mygym.crm.backstages.core.services.communication;

import com.mygym.crm.sharedmodule.MyClient;
import com.mygym.crm.sharedmodule.TrainerWorkloadDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "trainer-contribution-calculator")
@CircuitBreaker(name = "trainerHoursCircuitBreaker", fallbackMethod = "fallbackAcceptWorkload")
public interface TrainerHoursCalculator extends MyClient {

    Logger logger = LoggerFactory.getLogger(TrainerHoursCalculator.class);


    default boolean fallbackAcceptWorkload(TrainerWorkloadDto trainingWorkloadDto, Throwable throwable) {
        logger.error("Error occurred while calling acceptWorkload method: {}", throwable.getMessage(), throwable);

        return false;
    }
}
