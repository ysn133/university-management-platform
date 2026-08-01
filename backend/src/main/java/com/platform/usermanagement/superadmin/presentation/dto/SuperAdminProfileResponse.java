package com.platform.usermanagement.superadmin.presentation.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.shared.domain.Sex;

public record SuperAdminProfileResponse (
    UUID id,
    UUID accountId,
    UUID establishmentId,
    String email , 
    AccountRoleType role , 
    AccountStatus status,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String cin,
    Sex sex,
    String phoneNumber
   
    
    
){
    
}
