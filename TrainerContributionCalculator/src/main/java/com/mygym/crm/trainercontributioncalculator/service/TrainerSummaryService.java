package com.mygym.crm.trainercontributioncalculator.service;

import com.mygym.crm.sharedmodule.TrainerWorkloadDto;
import com.mygym.crm.trainercontributioncalculator.domain.models.TrainerSummary;
import com.mygym.crm.trainercontributioncalculator.repositories.TrainingRepository;
import com.mygym.crm.trainercontributioncalculator.service.mappers.TrainerSummaryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainerSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerSummaryService.class);

    private final TrainingRepository trainingRepository;
    private final TrainerSummaryMapper trainerSummaryMapper;

    @Autowired
    public TrainerSummaryService(TrainingRepository trainingRepository, TrainerSummaryMapper trainerSummaryMapper) {
        this.trainingRepository = trainingRepository;
        this.trainerSummaryMapper = trainerSummaryMapper;
    }

    @Transactional
    public TrainerSummary createTrainerSummary(TrainerWorkloadDto trainerWorkloadDto) {
        logger.debug("Creating trainer summary for trainer: {}", trainerWorkloadDto.getUserName());
        TrainerSummary trainerSummary = trainerSummaryMapper.toTrainerSummary(trainerWorkloadDto);
        TrainerSummary savedTrainerSummary = trainingRepository.save(trainerSummary);
        logger.info("Trainer summary created for trainer: {}", savedTrainerSummary.getUsername());
        return savedTrainerSummary;
    }

    @Transactional
    public TrainerSummary updateTrainerSummary(TrainerSummary trainerSummary) {
        logger.debug("Updating trainer summary for trainer: {}", trainerSummary.getUsername());
        TrainerSummary updatedTrainerSummary = trainingRepository.save(trainerSummary);
        logger.info("Trainer summary updated for trainer: {}", updatedTrainerSummary.getUsername());
        return updatedTrainerSummary;
    }

    public TrainerSummary findByUsername(String username) {
        logger.debug("Finding trainer summary by username: {}", username);
        TrainerSummary trainerSummary = trainingRepository.findByUsername(username);
        if (trainerSummary != null) {
            logger.debug("Trainer summary found for username: {}", username);
        } else {
            logger.debug("Trainer summary not found for username: {}", username);
        }
        return trainerSummary;
    }
}