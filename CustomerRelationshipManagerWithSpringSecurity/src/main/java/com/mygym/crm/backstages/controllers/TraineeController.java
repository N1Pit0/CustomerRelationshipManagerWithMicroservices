package com.mygym.crm.backstages.controllers;

import com.mygym.crm.backstages.core.dtos.request.ChangePasswordDto;
import com.mygym.crm.backstages.core.dtos.request.common.CombineUserDtoWithSecurityDto;
import com.mygym.crm.backstages.core.dtos.request.traineedto.TraineeDto;
import com.mygym.crm.backstages.core.dtos.response.traineedto.select.SelectTraineeDto;
import com.mygym.crm.backstages.core.dtos.response.traineedto.select.SelectTraineeTrainingsDtoSet;
import com.mygym.crm.backstages.core.dtos.response.traineedto.select.SelectTrainerNotAssignedDtoSet;
import com.mygym.crm.backstages.core.dtos.response.traineedto.select.TraineeCredentialsDto;
import com.mygym.crm.backstages.core.dtos.response.traineedto.update.UpdateTraineeDto;
import com.mygym.crm.backstages.core.services.UserService;
import com.mygym.crm.backstages.domain.models.Trainee;
import com.mygym.crm.backstages.domain.models.Trainer;
import com.mygym.crm.backstages.domain.models.Training;
import com.mygym.crm.backstages.exceptions.custom.NoResourceException;
import com.mygym.crm.backstages.exceptions.custom.NoTraineeException;
import com.mygym.crm.backstages.exceptions.custom.ResourceCreationException;
import com.mygym.crm.backstages.exceptions.custom.ResourceUpdateException;
import com.mygym.crm.backstages.interfaces.services.TraineeServiceCommon;
import com.mygym.crm.backstages.core.services.mapper.TraineeMapper;
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
@RequestMapping(value = "/users/trainees")
public class TraineeController {
    private TraineeServiceCommon traineeService;
    private UserService userService;
    private TraineeMapper mapper;

    @Autowired
    public void setTraineeService(TraineeServiceCommon traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setMapper(TraineeMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/{userName:.+}", produces = "application/json")
    @Operation(summary = "Get a Trainee profile", description = "Gets a single Trainee profile and all its dependencies with specific userName")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the single Trainee profile"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainee resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<SelectTraineeDto> getTraineeProfile(@PathVariable("userName") String userName) throws NoTraineeException {

        Optional<Trainee> optionalTrainee = traineeService.getByUserName(userName);

        return optionalTrainee.map(mapper::traineeToSelectTraineeDto)
                .map((trainee) -> new ResponseEntity<>(trainee, HttpStatus.OK))
                .orElseThrow(() -> new NoResourceException("No resource found for " + userName));
    }

    @GetMapping(value = "/{userName:.+}/list-trainee-trainings", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Get Trainings of Trainees", description = "Gets the list of Trainings that are of specific Trainee on given filters as query params")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the list of Trainings"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainee resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<SelectTraineeTrainingsDtoSet> getTraineeTrainings(@PathVariable("userName") String userName,
                                                                            @RequestParam(name = "periodFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
                                                                            @RequestParam(name = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
                                                                            @RequestParam(name = "trainerName", required = false) String trainerName,
                                                                            @RequestParam(name = "trainingTypename", required = false) String trainingTypename) {


        Optional<Set<Training>> optionalTrainings = traineeService.getTraineeTrainings(
                userName,
                periodFrom,
                periodTo,
                trainerName,
                trainingTypename
        );

        return optionalTrainings
                .map(mapper::trainingToSelectTraineeTrainingDtoSet)
                .map(trainings -> new ResponseEntity<>(trainings, HttpStatus.OK))
                .orElseThrow(() -> new NoResourceException("No resource found for " + userName));
    }

    @GetMapping(value = "/{userName:.+}/not-assigned-trainers")
    @Operation(summary = "Get Trainers not for Trainees", description = "Gets the list of Trainers that are not teaching specific Trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the list of Trainers"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainee resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<SelectTrainerNotAssignedDtoSet> getTrainersNotTrainingTraineesWithUserName(
            @PathVariable("userName") String UserName) {

        Optional<Set<Trainer>> optionalTrainings = traineeService.getTrainersNotTrainingTraineesWithUserName(
                UserName
        );

        return optionalTrainings
                .map(mapper::trainerNotAssignedToSelectTrainerDtoSet)
                .map(trainers -> new ResponseEntity<>(trainers, HttpStatus.OK))
                .orElseThrow(() -> new NoResourceException("No resource found for " + UserName));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(summary = "Create a new Trainee user", description = "Create a Trainee object and save it in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new Trainee"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainee resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<TraineeCredentialsDto> registerTrainee(@RequestBody TraineeDto traineeDto) {
        userService.validateDto(traineeDto);

        Optional<Trainee> optionalTrainee = traineeService.create(traineeDto);

        Trainee trainee = optionalTrainee.orElseThrow(() -> new ResourceCreationException("could not create Trainer"));

        return new ResponseEntity<>(new TraineeCredentialsDto(trainee.getUserName(), userService.getPassword()),
                HttpStatus.CREATED);
    }

    @PutMapping(value = {"/{userName:.+}"}, consumes = "application/json", produces = "application/json")
    @Operation(summary = "Update Trainee user", description = "Updates an existing Trainee Object and save it in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated an existing Trainee object"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainee resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<UpdateTraineeDto> updateTraineeProfile(@PathVariable("userName") String userName,
                                                                 @RequestBody CombineUserDtoWithSecurityDto<TraineeDto> updateTraineeDtoWithSecurityDto) {

        userService.validateDto(updateTraineeDtoWithSecurityDto);
        userService.validateDto(updateTraineeDtoWithSecurityDto.getUserDto());

        Optional<Trainee> optionalTrainee = traineeService.updateByUserName(
                userName,
                updateTraineeDtoWithSecurityDto.getUserDto());

        return optionalTrainee
                .map(mapper::traineeToUpdateTraineeDto)
                .map((trainee) -> new ResponseEntity<>(trainee, HttpStatus.OK))
                .orElseThrow(() -> new ResourceCreationException("could not update Trainee Profile"));
    }

    @PutMapping(value = "/{userName:.+}/change-login", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Change password of Trainee user", description = "Changes password of an existing Trainee Object and save it in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed the password of user"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainee resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> changeLogin(@PathVariable("userName") String userName,
                                            @RequestBody ChangePasswordDto changePasswordDto) {

        userService.validateDto(changePasswordDto);

        boolean isPassed = traineeService.changePassword(
                userName,
                changePasswordDto.getNewPassword()
        );

        if (isPassed) return ResponseEntity.ok().build();

        throw new ResourceUpdateException("could not update Trainee Profile");
    }

    @DeleteMapping(value = "/{userName:.+}", produces = "application/json")
    @Operation(summary = "Delete Trainee user", description = "Deletes an existing Trainee Object from the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted an existing Trainee"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainee resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable("userName") String userName) {

        Optional<Trainee> optionalTrainee = traineeService.deleteWithUserName(userName);

        if (optionalTrainee.isPresent()) {

            return new ResponseEntity<>(HttpStatus.valueOf(204));
        }

        throw new ResourceCreationException("could not delete Trainee Profile");
    }

    @PatchMapping(value = "/{userName:.+}/toggleActive")
    @Operation(summary = "Update Trainee user", description = "Performs an action that lets user turn on and off their active status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully toggled isActive button"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "409", description = "The Trainee resource is not there OR could not perform the action"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> toggleIsActive(@PathVariable("userName") String userName) {

        boolean isPerformed = traineeService.toggleIsActive(userName);

        if (isPerformed) {
            return new ResponseEntity<>(HttpStatus.valueOf(200));
        }

        throw new ResourceCreationException("could not update Trainee Profile");
    }

}
