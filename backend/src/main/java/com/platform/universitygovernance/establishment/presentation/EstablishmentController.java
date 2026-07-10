package com.platform.universitygovernance.establishment.presentation;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.establishment.application.EstablishmentService;
import com.platform.universitygovernance.establishment.presentation.dto.CreateEstablishmentRequest;
import com.platform.universitygovernance.establishment.presentation.dto.EstablishmentResponse;



@RestController
@RequestMapping("/api/v1")

public class EstablishmentController {
    private final EstablishmentService establishmentService;

    public EstablishmentController(EstablishmentService establishmentService){

        this.establishmentService = establishmentService;
    }
    @PreAuthorize("hasRole('ROOT_SUPER_ADMIN')")
    @GetMapping("/establishments/{id}")
    public EstablishmentResponse getEstablishmentById(@PathVariable("id") UUID establishmentId){
       return this.establishmentService.getEstablishment(establishmentId);
    }
    @PreAuthorize("hasRole('ROOT_SUPER_ADMIN')")
    @GetMapping("/university/{universityId}/establishments")
    public List<EstablishmentResponse> getEstablishments(@PathVariable("universityId") UUID universityId){
        return this.establishmentService.getEstablishments(universityId);
    }

    @PreAuthorize("hasRole('ROOT_SUPER_ADMIN')")
    @PostMapping("/establishments")
    public EstablishmentResponse createEstablishment(@Valid @RequestBody CreateEstablishmentRequest request) {
        return this.establishmentService.createEstablishment(request);
    }

    @PreAuthorize("hasRole('ROOT_SUPER_ADMIN')")
    @PostMapping("/establishments/{id}/activate")
    public ResponseEntity<ActionResponse> activateEstablishment(@PathVariable("id") UUID establishmentId ){
        establishmentService.activateEstablishment(establishmentId);
        return ResponseEntity.ok(new ActionResponse(true, "Establishment activated"));
    }

    
    @PreAuthorize("hasRole('ROOT_SUPER_ADMIN')")
    @PostMapping("/establishments/{id}/deactivate")
    public ResponseEntity<ActionResponse> deactivateEstablishment(@PathVariable("id") UUID establishmentId){

        establishmentService.deactivateEstablishment(establishmentId);
        return ResponseEntity.ok(new ActionResponse(true, "Establishment deactivated!"));
    }



    
}
