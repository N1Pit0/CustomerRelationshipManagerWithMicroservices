package com.mygym.crm.backstages.controllers;

import com.mygym.crm.backstages.core.dtos.request.ChangePasswordDto;
import com.mygym.crm.backstages.core.dtos.request.common.CombineUserDtoWithSecurityDto;
import com.mygym.crm.backstages.core.dtos.request.trainerdto.TrainerDto;
import com.mygym.crm.backstages.core.dtos.response.trainerdto.select.SelectTrainerDto;
import com.mygym.crm.backstages.core.dtos.response.trainerdto.select.SelectTrainerTrainingsDtoSet;
import com.mygym.crm.backstages.core.dtos.response.trainerdto.select.TrainerCredentials;
import com.mygym.crm.backstages.core.dtos.response.trainerdto.update.UpdateTrainerDto;
import com.mygym.crm.backstages.core.services.UserService;
import com.mygym.crm.backstages.domain.models.Trainer;
import com.mygym.crm.backstages.domain.models.Training;
import com.mygym.crm.backstages.exceptions.custom.NoResourceException;
import com.mygym.crm.backstages.exceptions.custom.ResourceCreationException;
import com.mygym.crm.backstages.interfaces.services.TrainerServiceCommon;
import com.mygym.crm.backstages.core.services.mapper.TrainerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(value = "users/trainers")
public class TrainerController {
    private TrainerServiceCommon trainerService;
    private UserService userService;
    private TrainerMapper mapper;

    @Autowired
    public void setTrainerService(TrainerServiceCommon trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping(value = "/{userName:.+}", produces = "application/json")
    @Operation(summary = "Get a Trainer profile", description = "Gets a single Trainer profile and all its dependencies with specific userName")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the single Trainer profile"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainer resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<SelectTrainerDto> getTrainerProfile(@PathVariable("userName") String userName) {

        Optional<Trainer> optionalTrainer = trainerService.getByUserName(userName);

        return optionalTrainer.map(mapper::trainerToSelectTrainerDto)
                .map((trainer) -> new ResponseEntity<>(trainer, HttpStatus.OK))
                .orElseThrow(() -> new NoResourceException("No resource found for " + userName));
    }

    @GetMapping(value = "/{userName:.+}/list-trainer-trainings")
    @Operation(summary = "Get Trainings of Trainers", description = "Gets the list of Trainings that are of specific Trainers on given filters as query params")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the list of Trainings"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainer resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<SelectTrainerTrainingsDtoSet> getTrainerTrainings(@PathVariable("userName") String userName,
                                                                            @RequestParam(name = "periodFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
                                                                            @RequestParam(name = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
                                                                            @RequestParam(name = "traineeName", required = false) String traineeName) {

        Optional<Set<Training>> optionalTrainings = trainerService.getTrainerTrainings(
                userName,
                periodFrom,
                periodTo,
                traineeName
        );

        return optionalTrainings
                .map(mapper::trainingToSelectTrainerTrainingDtoSet)
                .map(trainings -> new ResponseEntity<>(trainings, HttpStatus.OK))
                .orElseThrow(() -> new NoResourceException("No resource found for " + userName));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(summary = "Create a new Trainer user", description = "Create a Trainer object and save it in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new Trainer"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainer resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<TrainerCredentials> registerTrainer(@RequestBody TrainerDto trainerDto) {

        userService.validateDto(trainerDto);

        Optional<Trainer> optionalTrainer = trainerService.create(trainerDto);

        Trainer trainer1 = optionalTrainer.orElseThrow(() -> new ResourceCreationException("could not create Trainer"));

        return new ResponseEntity<>(new TrainerCredentials(trainer1.getUserName(), userService.getPassword()),
                HttpStatus.CREATED);
    }

    @PutMapping(value = {"/{userName:.+}"}, consumes = "application/json", produces = "application/json")
    @Operation(summary = "Update Trainer user", description = "Updates an existing Trainer Object and save it in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated an existing Trainer object"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainer resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<UpdateTrainerDto> updateTrainerProfile(@PathVariable("userName") String userName,
                                                                 @RequestBody CombineUserDtoWithSecurityDto<TrainerDto> updateTrainerDtoWithSecurityDto) {

        userService.validateDto(updateTrainerDtoWithSecurityDto);
        userService.validateDto(updateTrainerDtoWithSecurityDto.getUserDto());

        Optional<Trainer> optionalTrainer = trainerService.updateByUserName(
                userName,
                updateTrainerDtoWithSecurityDto.getUserDto());

        return optionalTrainer
                .map(mapper::trainerToUpdateTrainerDto)
                .map((trainee) -> new ResponseEntity<>(trainee, HttpStatus.OK))
                .orElseThrow(() -> new ResourceCreationException("could not update Trainer Profile"));
    }

    @PutMapping(value = "/{userName:.+}/change-login", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Change password of Trainer user", description = "Changes password of an existing Trainer Object and save it in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed the password of user"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainer resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> changeLogin(@PathVariable("userName") String userName,
                                            @RequestBody ChangePasswordDto changePasswordDto) {
        userService.validateDto(changePasswordDto);

        boolean isPassed = trainerService.changePassword(
                userName,
                changePasswordDto.getNewPassword()
        );

        if (isPassed) return ResponseEntity.ok().build();

        throw new ResourceCreationException("could not update Trainer Profile");
    }

    @PatchMapping(value = "/{userName:.+}/toggleActive")
    @Operation(summary = "Update Trainer user", description = "Performs an action that lets user turn on and off their active status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully toggled isActive button"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainer resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> toggleIsActive(@PathVariable("userName") String userName
    ) {


        boolean isPerformed = trainerService.toggleIsActive(userName);

        if (isPerformed) return new ResponseEntity<>(HttpStatus.valueOf(200));

        throw new ResourceCreationException("could not update Trainer Profile");
    }
}
