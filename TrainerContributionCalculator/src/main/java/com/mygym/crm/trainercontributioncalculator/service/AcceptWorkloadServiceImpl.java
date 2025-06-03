package com.mygym.crm.trainercontributioncalculator.service;

import com.mygym.crm.sharedmodule.TrainerWorkloadDto;
import com.mygym.crm.trainercontributioncalculator.domain.models.MonthlySummary;
import com.mygym.crm.trainercontributioncalculator.domain.models.TrainerSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcceptWorkloadServiceImpl implements AcceptWorkload {
    private static final Logger logger = LoggerFactory.getLogger(AcceptWorkloadServiceImpl.class);

    private final TrainerSummaryService trainerSummaryService;
    private final MonthlySummaryService monthlySummaryService;

    @Autowired
    public AcceptWorkloadServiceImpl(TrainerSummaryService trainerSummaryService, MonthlySummaryService monthlySummaryService) {
        this.trainerSummaryService = trainerSummaryService;
        this.monthlySummaryService = monthlySummaryService;
    }

    @Transactional
    public boolean acceptWorkload(TrainerWorkloadDto trainerWorkloadDto) {
        logger.info("Accepting workload for trainer: {}", trainerWorkloadDto.getUserName());
        return switch (trainerWorkloadDto.getActionType()) {
            case ADD -> addTrainingHours(trainerWorkloadDto);
            case DELETE -> removeTrainingHours(trainerWorkloadDto);
        };
    }

    private boolean addTrainingHours(TrainerWorkloadDto trainerWorkloadDto) {
        logger.debug("Adding training hours for trainer: {}", trainerWorkloadDto.getUserName());
        TrainerSummary trainerSummary = trainerSummaryService.findByUsername(trainerWorkloadDto.getUserName());

        if (trainerSummary == null) {
            logger.debug("Creating new trainer summary for trainer: {}", trainerWorkloadDto.getUserName());
            trainerSummary = trainerSummaryService.createTrainerSummary(trainerWorkloadDto);
        }

        MonthlySummary monthlySummary = monthlySummaryService.findOrCreateMonthlySummary(trainerSummary, trainerWorkloadDto);
        monthlySummary.setTrainingDuration(monthlySummary.getTrainingDuration() + trainerWorkloadDto.getTrainingDuration());

        trainerSummary.getMonthlySummaries().add(monthlySummary);

        trainerSummaryService.updateTrainerSummary(trainerSummary);
        logger.info("Training hours added successfully for trainer: {}", trainerWorkloadDto.getUserName());
        return true;
    }

    private boolean removeTrainingHours(TrainerWorkloadDto trainerWorkloadDto) {
        logger.debug("Removing training hours for trainer: {}", trainerWorkloadDto.getUserName());
        TrainerSummary trainerSummary = trainerSummaryService.findByUsername(trainerWorkloadDto.getUserName());

        if (trainerSummary == null) {
            logger.warn("Trainer summary not found for trainer: {}", trainerWorkloadDto.getUserName());
            return false;
        }

        MonthlySummary monthlySummary = monthlySummaryService.findMonthlySummary(trainerSummary, trainerWorkloadDto);


        if (monthlySummary == null) {
            logger.warn("Monthly summary not found for trainer: {}", trainerWorkloadDto.getUserName());
            return false;
        }

        int updatedDuration = monthlySummary.getTrainingDuration() - trainerWorkloadDto.getTrainingDuration();

        if (updatedDuration <= 0) {
            logger.debug("Removing monthly summary for trainer: {}", trainerWorkloadDto.getUserName());
            trainerSummary.getMonthlySummaries().remove(monthlySummary);
        } else {
            logger.debug("Updating training duration for trainer: {}", trainerWorkloadDto.getUserName());
            monthlySummary.setTrainingDuration(updatedDuration);
        }

        trainerSummaryService.updateTrainerSummary(trainerSummary);
        return true;
    }
}