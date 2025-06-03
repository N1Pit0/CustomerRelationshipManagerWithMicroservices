package com.mygym.crm.trainercontributioncalculator.service;

import com.mygym.crm.sharedmodule.TrainerWorkloadDto;
import com.mygym.crm.trainercontributioncalculator.domain.models.MonthlySummary;
import com.mygym.crm.trainercontributioncalculator.domain.models.TrainerSummary;
import com.mygym.crm.trainercontributioncalculator.service.mappers.MonthlySummaryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonthlySummaryService {
    private final MonthlySummaryMapper monthlySummaryMapper;
    private static final Logger logger = LoggerFactory.getLogger(MonthlySummaryService.class);

    @Autowired
    public MonthlySummaryService(MonthlySummaryMapper monthlySummaryMapper) {
        this.monthlySummaryMapper = monthlySummaryMapper;
    }

    public MonthlySummary findOrCreateMonthlySummary(TrainerSummary trainerSummary, TrainerWorkloadDto trainerWorkloadDto) {
        logger.debug("Finding or creating monthly summary for trainer: {}", trainerSummary.getUsername());
        MonthlySummary monthlySummary = findMonthlySummary(trainerSummary, trainerWorkloadDto);

        if (monthlySummary == null) {
            monthlySummary = monthlySummaryMapper.toMonthlySummary(trainerWorkloadDto);
            monthlySummary.setTrainerSummary(trainerSummary);
            trainerSummary.getMonthlySummaries().add(monthlySummary);
            logger.info("New monthly summary created for trainer: {}", trainerSummary.getUsername());
        }

        return monthlySummary;
    }

    public MonthlySummary findMonthlySummary(TrainerSummary trainerSummary, TrainerWorkloadDto trainerWorkloadDto) {
        logger.debug("Finding monthly summary for trainer: {}", trainerSummary.getUsername());
        MonthlySummary monthlySummary = trainerSummary.getMonthlySummaries().stream()
                .filter(summary -> summary.getSummaryYear() == trainerWorkloadDto.getTrainingDate().getYear())
                .filter(summary -> summary.getSummaryMonth() == monthlySummaryMapper.integerToEnum(trainerWorkloadDto.getTrainingDate().getMonthValue()))
                .findFirst()
                .orElse(null);

        if (monthlySummary != null) {
            logger.debug("Monthly summary found for trainer: {}", trainerSummary.getUsername());
        } else {
            logger.debug("Monthly summary not found for trainer: {}", trainerSummary.getUsername());
        }

        return monthlySummary;
    }
}