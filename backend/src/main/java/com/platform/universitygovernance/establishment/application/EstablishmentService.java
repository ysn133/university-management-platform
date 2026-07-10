package com.platform.universitygovernance.establishment.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.establishment.presentation.dto.CreateEstablishmentRequest;
import com.platform.universitygovernance.establishment.presentation.dto.EstablishmentResponse;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EstablishmentService {

    private final EstablishmentRepository establishmentRepository;
    private final UniversityRepository universityRepository;
    
    public EstablishmentService(EstablishmentRepository establishmentRepository , UniversityRepository universityRepository){
        this.establishmentRepository=establishmentRepository;
        this.universityRepository=universityRepository;
    }

    @Transactional(readOnly=true)
    public EstablishmentResponse getEstablishment(UUID id){
        Establishment establishment = establishmentRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Establishment not found"));

        return new EstablishmentResponse(
            establishment.getId(),
            establishment.getUniversity().getId(),
            establishment.getName(),
            establishment.getEstablishmentType(),
            establishment.getEstablishmentStatus(),
            establishment.getCreatedAt(),
            establishment.getUpdatedAt()



        );

    }



    @Transactional(readOnly=true)
    public List<EstablishmentResponse> getEstablishments(UUID id){
        List <Establishment> establishments = establishmentRepository.findByUniversityId(id); 
        List<EstablishmentResponse> responses = new ArrayList<>();
       for (Establishment establishment : establishments) {

        responses.add(
            new EstablishmentResponse(
                establishment.getId(),
                establishment.getUniversity().getId(),
                establishment.getName(),
                establishment.getEstablishmentType(),
                establishment.getEstablishmentStatus(),
                establishment.getCreatedAt(),
                establishment.getUpdatedAt()
            )
        );
    }

    return responses;
    }

 @Transactional
public EstablishmentResponse createEstablishment(CreateEstablishmentRequest request) {

    University university = universityRepository
            .findById(request.universityId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "University not found"));

    Establishment establishment = new Establishment();

    establishment.setName(request.name().trim());
    establishment.setUniversity(university);
    establishment.setEstablishmentType(request.type());
    establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);

    Establishment savedEstablishment = establishmentRepository.save(establishment);

    return new EstablishmentResponse(
            savedEstablishment.getId(),
            savedEstablishment.getUniversity().getId(),
            savedEstablishment.getName(),
            savedEstablishment.getEstablishmentType(),
            savedEstablishment.getEstablishmentStatus(),
            savedEstablishment.getCreatedAt(),
            savedEstablishment.getUpdatedAt()
    );
}

@Transactional
public void activateEstablishment(UUID establishmentId){
    Establishment establishment= establishmentRepository.findById(establishmentId)
    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND , "Establishment Not Found"));
    establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
    establishmentRepository.save(establishment);
}

@Transactional
public void deactivateEstablishment(UUID etsablishmentId){
    Establishment establishment = establishmentRepository.findById(etsablishmentId)
    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND , "Establishment Not Found"));
    establishment.setEstablishmentStatus(EstablishmentStatus.INACTIVE);
    establishmentRepository.save(establishment);

}


}
