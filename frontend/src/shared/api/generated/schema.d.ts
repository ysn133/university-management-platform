export interface paths {
    "/api/v1/teaching-groups/{teachingGroupId}/members/{semesterRegistrationId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["moveMember"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/super-admins/{superAdminId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSuperAdmin"];
        put: operations["updateSuperAdmin"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/subject-modules/{subjectModuleId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSubjectModule"];
        put: operations["updateSubjectModule"];
        post?: never;
        delete: operations["deleteSubjectModule"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/subject-modules/{subjectModuleId}/teaching-components": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getModuleTeachingComponents"];
        put: operations["replaceModuleTeachingComponents"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/students/{studentId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getStudent"];
        put: operations["updateStudent"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semesters/{semesterId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSemester"];
        put: operations["updateSemester"];
        post?: never;
        delete: operations["deleteSemester"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/schedule-entries/{scheduleEntryId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getScheduleEntry"];
        put: operations["updateScheduleEntry"];
        post?: never;
        delete: operations["deleteScheduleEntry"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/rooms/{roomId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getRoom"];
        put: operations["updateRoom"];
        post?: never;
        delete: operations["deactivateRoom"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/professors/{professorId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getProfessor"];
        put: operations["update"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/professors/{professorId}/expertise": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getProfessorExpertise"];
        put: operations["replaceProfessorExpertise"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-exams/{moduleExamId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getModuleExam"];
        put: operations["updateModuleExam"];
        post?: never;
        delete: operations["deleteModuleExam"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-exams/{moduleExamId}/grade-sheet": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getGradeSheet"];
        put: operations["saveDraftGradeSheet"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/exam-schedules/{examScheduleId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getExamSchedule"];
        put: operations["updateExamSchedule"];
        post?: never;
        delete: operations["deleteExamSchedule"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getEstablishmentById"];
        put: operations["updateEstablishment"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/teaching-assignment-rank-preferences/{componentType}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["replace"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/class-groups/{classGroupId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getClassGroup"];
        put: operations["updateClassGroup"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/blocks/{blockId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getBlock"];
        put: operations["updateBlock"];
        post?: never;
        delete: operations["deactivateBlock"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAdmin"];
        put: operations["updateAdmin"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}/permission-grants": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAdminGrants"];
        put: operations["replaceAdminGrants"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-years/{academicYearId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicYear"];
        put: operations["updateAcademicYear"];
        post?: never;
        delete: operations["deleteAcademicYear"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-rule-profiles/{academicRuleProfileId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicRuleProfile"];
        put: operations["updateAcademicRuleProfile"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-registrations/{registrationId}/semesters/{semesterId}/class-assignment": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getStudentClassAssignment"];
        put: operations["assignStudentClass"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-registrations/{academicRegistrationId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicRegistration"];
        put: operations["updateAcademicRegistration"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-ranks/{rankId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["update_1"];
        post?: never;
        delete: operations["delete"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicLevel"];
        put: operations["updateAcademicLevel"];
        post?: never;
        delete: operations["deleteAcademicLevel"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}/teaching-group-policies": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getPolicies"];
        put: operations["replacePolicies"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}/class-groups/rebalance": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["rebalanceClassGroups"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}/class-groups/assignments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["bulkAssignStudentClasses"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-domains/{academicDomainId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicDomain"];
        put: operations["updateAcademicDomain"];
        post?: never;
        delete: operations["deleteAcademicDomain"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/absences/{absenceId}/justification": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["updateJustification"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/teaching-assignments/{teachingAssignmentId}/absences": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getTeachingAssignmentAbsences"];
        put?: never;
        post: operations["createAbsence"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/super-admins/{id}/unlock": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["unlockAccount"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/super-admins/{id}/restore": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["restoreAccount"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/super-admins/{id}/password-reset": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["resetPassword"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/super-admins/{id}/lock": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["lockAccount"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/super-admins/{id}/deactivate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["deactivateAccount"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/super-admins/{id}/archive": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["archiveAccount"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/super-admins/{id}/activate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["activateAccount"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/students/{studentId}/unlock": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["unlock"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/students/{studentId}/password-reset": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["resetPassword_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/students/{studentId}/lock": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["lock"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/students/{studentId}/deactivate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["deactivate"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/students/{studentId}/archive": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["archive"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semesters/{semesterId}/teaching-requirements/generate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["generate"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semesters/{semesterId}/teaching-groups/generate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["generate_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semesters/{semesterId}/teaching-assignments/generate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["generateTeachingAssignments"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semesters/{semesterId}/subject-modules": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSubjectModules"];
        put?: never;
        post: operations["createSubjectModule"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semester-schedules/{scheduleId}/publish": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["publishSemesterSchedule"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semester-schedules/{scheduleId}/entries": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getScheduleEntries"];
        put?: never;
        post: operations["createScheduleEntry"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/program-filieres/{programFiliereId}/academic-levels": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicLevels"];
        put?: never;
        post: operations["createAcademicLevel"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/professors/{professorId}/unlock": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["unlock_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/professors/{professorId}/password-reset": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["resetPassword_2"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/professors/{professorId}/lock": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["lock_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/professors/{professorId}/deactivate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["deactivate_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/professors/{professorId}/archive": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["archive_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-exams/{moduleExamId}/grade-sheet/submit": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["submitGradeSheet"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-exams/{moduleExamId}/grade-sheet/review": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["reviewGradeSheet"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-exams/{moduleExamId}/grade-sheet/publish": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["publishGradeSheet"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-exams/{moduleExamId}/grade-sheet/approve": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["approveGradeSheet"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-exams/{moduleExamId}/candidates/generate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["generateCandidates"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/exam-schedules/{examScheduleId}/publish": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["publishExamSchedule"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/exam-schedules/{examScheduleId}/module-exams": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getModuleExams"];
        put?: never;
        post: operations["createModuleExam"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["createEstablishment"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{id}/super-admins": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["createSuperAdmin"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{id}/deactivate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["deactivateEstablishment"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{id}/admins": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAdmins"];
        put?: never;
        post: operations["createAdmin"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{id}/activate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["activateEstablishment"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/teaching-assignments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getTeachingAssignments"];
        put?: never;
        post: operations["createTeachingAssignment"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/students": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getStudents"];
        put?: never;
        post: operations["createStudent"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/semester-schedules": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSemesterSchedules"];
        put?: never;
        post: operations["createSemesterSchedule"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/rooms": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getRooms"];
        put?: never;
        post: operations["createRoom"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/program-paths": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getProgramPaths"];
        put?: never;
        post: operations["createProgramPath"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/professors": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getProfessors"];
        put?: never;
        post: operations["createProfessor"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/module-class-responsibilities": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getResponsibilities"];
        put?: never;
        post: operations["createResponsibility"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/exam-schedules": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getExamSchedules"];
        put?: never;
        post: operations["createExamSchedule"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/departments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getDepartments"];
        put?: never;
        post: operations["createDepartment"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/degree-cycles": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getDegreeCycles"];
        put?: never;
        post: operations["createDegreeCycle"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/blocks": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getBlocks"];
        put?: never;
        post: operations["createBlock"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/academic-years": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicYears"];
        put?: never;
        post: operations["createAcademicYear"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/academic-rule-profiles": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicRuleProfiles"];
        put?: never;
        post: operations["createAcademicRuleProfile"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/academic-registrations": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicRegistrations"];
        put?: never;
        post: operations["createAcademicRegistration"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/academic-ranks": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list"];
        put?: never;
        post: operations["create"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/academic-domains": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAcademicDomains"];
        put?: never;
        post: operations["createAcademicDomain"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/departments/{departmentId}/program-filieres": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getProgramFilieres"];
        put?: never;
        post: operations["createProgramFiliere"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/refresh": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["refresh"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/logout": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["logout"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/login": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["login"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/change-password": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["changePassword"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}/unlock": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["unlockAccount_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}/restore": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["restoreAccount_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}/password-reset": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["resetPassword_3"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}/lock": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["lockAccount_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}/deactivate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["deactivateAccount_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}/archive": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["archiveAccount_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admins/{id}/activate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["activateAccount_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}/semesters": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSemesters"];
        put?: never;
        post: operations["createSemester"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}/rule-assignments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAssignments"];
        put?: never;
        post: operations["createAssignment"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}/class-groups": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getClassGroups"];
        put?: never;
        post: operations["createClassGroup"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}/class-groups/generate": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["generateClassGroups"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/program-paths/{programPathId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getProgramPath"];
        put?: never;
        post?: never;
        delete: operations["deleteProgramPath"];
        options?: never;
        head?: never;
        patch: operations["updateProgramPath"];
        trace?: never;
    };
    "/api/v1/program-filieres/{programFiliereId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getProgramFiliere"];
        put?: never;
        post?: never;
        delete: operations["deleteProgramFiliere"];
        options?: never;
        head?: never;
        patch: operations["updateProgramFiliere"];
        trace?: never;
    };
    "/api/v1/departments/{departmentId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getDepartment"];
        put?: never;
        post?: never;
        delete: operations["deleteDepartment"];
        options?: never;
        head?: never;
        patch: operations["updateDepartment"];
        trace?: never;
    };
    "/api/v1/degree-cycles/{degreeCycleId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getDegreeCycle"];
        put?: never;
        post?: never;
        delete: operations["deleteDegreeCycle"];
        options?: never;
        head?: never;
        patch: operations["updateDegreeCycle"];
        trace?: never;
    };
    "/api/v1/university": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getUniversity"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/university/{universityId}/establishments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getEstablishments"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/university/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getUniversityById"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/teaching-assignments/{teachingAssignmentId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getTeachingAssignment"];
        put?: never;
        post?: never;
        delete: operations["unassignProfessor"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/teaching-assignments/{teachingAssignmentId}/students": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getTeachingAssignmentStudents"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/students/{studentId}/grades": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getStudentGrades"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/students/{studentId}/academic-registrations": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getStudentAcademicRegistrations"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semesters/{semesterId}/teaching-requirements": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getForSemester"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semesters/{semesterId}/teaching-groups": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getRoster"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semester-schedules/{scheduleId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSemesterSchedule"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semester-registrations/{semesterRegistrationId}/result": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSemesterResult"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semester-registrations/{semesterRegistrationId}/module-registrations": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getModuleRegistrations"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/permissions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getPermissionCatalog"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-exams/{moduleExamId}/candidates": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getCandidates"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/module-class-responsibilities/{responsibilityId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getResponsibility"];
        put?: never;
        post?: never;
        delete: operations["removeResponsibility"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/me/teaching-assignments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getMyTeachingAssignments"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/me/module-class-responsibilities": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getMyResponsibilities"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/me/grades": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getMyGrades"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/me/exam-invitations": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getMyInvitations"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/me/absences": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getMyAbsences"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/teaching-assignment-rank-preferences": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/super-admins": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSuperAdmins"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/establishments/{establishmentId}/absences": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getEstablishmentAbsences"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/me": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["me"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-registrations/{academicRegistrationId}/semester-registrations": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getSemesterRegistrations"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-registrations/{academicRegistrationId}/progression-decision": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getProgressionDecision"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-levels/{academicLevelId}/class-groups/roster": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getClassGroupRoster"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/academic-level-rule-assignments/{assignmentId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getAssignment"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/semesters/{semesterId}/teaching-assignments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete: operations["clearTeachingAssignments"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
}
export type webhooks = Record<string, never>;
export interface components {
    schemas: {
        TeachingGroupMemberResponse: {
            /** Format: uuid */
            semesterRegistrationId?: string;
            /** Format: uuid */
            studentId?: string;
            apogeeCode?: string;
            firstName?: string;
            lastName?: string;
            secondInscription?: boolean;
        };
        TeachingGroupResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            semesterId?: string;
            /** Format: uuid */
            sourceClassGroupId?: string;
            sourceClassGroupName?: string;
            name?: string;
            /** @enum {string} */
            groupType?: "TD" | "TP";
            members?: components["schemas"]["TeachingGroupMemberResponse"][];
        };
        TeachingGroupRosterResponse: {
            /** Format: uuid */
            semesterId?: string;
            groups?: components["schemas"]["TeachingGroupResponse"][];
        };
        UpdateSuperAdminRequest: {
            /** Format: email */
            universityEmail: string;
            firstName: string;
            lastName: string;
            /** Format: date */
            birth_date: string;
            cin?: string;
            /** @enum {string} */
            sex: "MALE" | "FEMALE";
            phone_number?: string;
        };
        SuperAdminProfileResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            accountId?: string;
            /** Format: uuid */
            establishmentId?: string;
            email?: string;
            /** @enum {string} */
            role?: "ROOT_SUPER_ADMIN" | "SUPER_ADMIN" | "ADMIN" | "PROFESSOR" | "STUDENT";
            /** @enum {string} */
            status?: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
            firstName?: string;
            lastName?: string;
            /** Format: date */
            birthDate?: string;
            cin?: string;
            /** @enum {string} */
            sex?: "MALE" | "FEMALE";
            phoneNumber?: string;
        };
        UpdateSubjectModuleRequest: {
            code: string;
            title: string;
            academicDomainIds: string[];
        };
        SubjectModuleResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            semesterId?: string;
            code?: string;
            title?: string;
            academicDomainIds?: string[];
        };
        ModuleTeachingComponentItemRequest: {
            /** @enum {string} */
            componentType: "COURSE" | "TD" | "TP";
            /** Format: int32 */
            sessionsPerWeek?: number;
            /** Format: int32 */
            sessionDurationMinutes?: number;
            /** @enum {string} */
            audienceMode: "WHOLE_COHORT" | "CLASS_GROUP" | "SUBGROUP";
            /** @enum {string} */
            requiredRoomType: "LECTURE_HALL" | "CLASSROOM" | "COMPUTER_LAB";
        };
        ReplaceModuleTeachingComponentsRequest: {
            components: components["schemas"]["ModuleTeachingComponentItemRequest"][];
        };
        ModuleTeachingComponentResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** @enum {string} */
            componentType?: "COURSE" | "TD" | "TP";
            /** Format: int32 */
            sessionsPerWeek?: number;
            /** Format: int32 */
            sessionDurationMinutes?: number;
            /** @enum {string} */
            audienceMode?: "WHOLE_COHORT" | "CLASS_GROUP" | "SUBGROUP";
            /** @enum {string} */
            requiredRoomType?: "LECTURE_HALL" | "CLASSROOM" | "COMPUTER_LAB";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateStudentRequest: {
            apogeeCode: string;
            nationalStudentCode?: string;
            cin?: string;
            /** Format: date */
            initialEnrollmentDate: string;
            /** Format: email */
            universityEmail: string;
            firstName: string;
            lastName: string;
            /** Format: date */
            birth_date: string;
            placeOfBirth: string;
            nationality: string;
            /** @enum {string} */
            sex: "MALE" | "FEMALE";
            phone_number?: string;
        };
        StudentProfileResponse: {
            /** Format: uuid */
            studentId?: string;
            /** Format: uuid */
            userAccountId?: string;
            /** Format: uuid */
            establishmentId?: string;
            apogeeCode?: string;
            nationalStudentCode?: string;
            /** Format: date */
            initialEnrollmentDate?: string;
            universityEmail?: string;
            /** @enum {string} */
            roleType?: "ROOT_SUPER_ADMIN" | "SUPER_ADMIN" | "ADMIN" | "PROFESSOR" | "STUDENT";
            /** @enum {string} */
            accountStatus?: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
            firstName?: string;
            lastName?: string;
            /** Format: date */
            birthDate?: string;
            placeOfBirth?: string;
            nationality?: string;
            cin?: string;
            /** @enum {string} */
            sex?: "MALE" | "FEMALE";
            phoneNumber?: string;
            profilePicturePath?: string;
        };
        UpdateSemesterRequest: {
            name: string;
            /** Format: int32 */
            semesterOrder?: number;
            /** @enum {string} */
            termType: "AUTUMN" | "SPRING";
        };
        SemesterResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: uuid */
            establishmentId?: string;
            name?: string;
            /** Format: int32 */
            semesterOrder?: number;
            /** @enum {string} */
            termType?: "AUTUMN" | "SPRING";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateScheduleEntryRequest: {
            /** Format: uuid */
            teachingAssignmentId: string;
            /** @enum {string} */
            dayOfWeek: "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";
            startTime: string;
            endTime: string;
            /** Format: uuid */
            roomId: string;
        };
        ScheduleEntryResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            semesterScheduleId?: string;
            /** Format: uuid */
            teachingAssignmentId?: string;
            /** Format: uuid */
            professorId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** Format: uuid */
            teachingGroupId?: string;
            teachingGroupName?: string;
            /** @enum {string} */
            dayOfWeek?: "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";
            startTime?: string;
            endTime?: string;
            /** Format: uuid */
            roomId?: string;
            roomCode?: string;
            roomName?: string;
            /** Format: uuid */
            blockId?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateRoomRequest: {
            /** Format: uuid */
            blockId?: string;
            code: string;
            name: string;
            /** @enum {string} */
            roomType: "LECTURE_HALL" | "CLASSROOM" | "COMPUTER_LAB";
            /** Format: int32 */
            capacity?: number;
            /** @enum {string} */
            status: "ACTIVE" | "INACTIVE";
        };
        RoomResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** Format: uuid */
            blockId?: string;
            blockCode?: string;
            code?: string;
            name?: string;
            /** @enum {string} */
            roomType?: "LECTURE_HALL" | "CLASSROOM" | "COMPUTER_LAB";
            /** Format: int32 */
            capacity?: number;
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateProfessorRequest: {
            employeeNumber: string;
            /** Format: uuid */
            academicRankId?: string;
            academicRank?: string;
            /** Format: date */
            hireDate?: string;
            /** Format: int32 */
            maximumWeeklyTeachingMinutes: number;
            cin?: string;
            /** Format: email */
            universityEmail: string;
            firstName: string;
            lastName: string;
            /** Format: date */
            birth_date: string;
            placeOfBirth: string;
            nationality: string;
            /** @enum {string} */
            sex: "MALE" | "FEMALE";
            phone_number?: string;
        };
        ProfessorProfileResponse: {
            /** Format: uuid */
            professorId?: string;
            /** Format: uuid */
            userAccountId?: string;
            /** Format: uuid */
            establishmentId?: string;
            employeeNumber?: string;
            /** Format: uuid */
            academicRankId?: string;
            academicRank?: string;
            /** Format: date */
            hireDate?: string;
            /** Format: int32 */
            maximumWeeklyTeachingMinutes?: number;
            universityEmail?: string;
            /** @enum {string} */
            roleType?: "ROOT_SUPER_ADMIN" | "SUPER_ADMIN" | "ADMIN" | "PROFESSOR" | "STUDENT";
            /** @enum {string} */
            accountStatus?: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
            firstName?: string;
            lastName?: string;
            /** Format: date */
            birthDate?: string;
            placeOfBirth?: string;
            nationality?: string;
            cin?: string;
            /** @enum {string} */
            sex?: "MALE" | "FEMALE";
            phoneNumber?: string;
            profilePicturePath?: string;
        };
        ReplaceProfessorExpertiseRequest: {
            academicDomainIds: string[];
        };
        ProfessorExpertiseItemResponse: {
            /** Format: uuid */
            academicDomainId?: string;
            code?: string;
            name?: string;
        };
        ProfessorExpertiseResponse: {
            /** Format: uuid */
            professorId?: string;
            academicDomains?: components["schemas"]["ProfessorExpertiseItemResponse"][];
        };
        UpdateModuleExamRequest: {
            /** Format: uuid */
            subjectModuleId: string;
            /** Format: uuid */
            classGroupId: string;
            /** Format: date */
            examDate: string;
            startTime: string;
            endTime?: string;
            location?: string;
        };
        ModuleExamResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            examScheduleId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** Format: uuid */
            classGroupId?: string;
            /** Format: date */
            examDate?: string;
            startTime?: string;
            endTime?: string;
            location?: string;
            /** Format: date-time */
            candidateListGeneratedAt?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        GradeItemRequest: {
            /** Format: uuid */
            moduleRegistrationId: string;
            gradeValue: number;
            /** @enum {string} */
            zeroGradeReason?: "ABSENT" | "EARNED_ZERO";
        };
        SaveGradeSheetRequest: {
            grades: components["schemas"]["GradeItemRequest"][];
        };
        GradeItemResponse: {
            /** Format: uuid */
            gradeRecordId?: string;
            /** Format: uuid */
            moduleRegistrationId?: string;
            /** Format: uuid */
            studentId?: string;
            /** Format: int32 */
            inscriptionNumber?: number;
            gradeValue?: number;
            /** @enum {string} */
            zeroGradeReason?: "ABSENT" | "EARNED_ZERO";
            /** @enum {string} */
            workflowStatus?: "DRAFT" | "SUBMITTED" | "REVIEWED" | "APPROVED" | "PUBLISHED";
            /** Format: date-time */
            publishedAt?: string;
        };
        GradeSheetResponse: {
            /** Format: uuid */
            moduleExamId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** Format: uuid */
            classGroupId?: string;
            /** @enum {string} */
            workflowStatus?: "DRAFT" | "SUBMITTED" | "REVIEWED" | "APPROVED" | "PUBLISHED";
            grades?: components["schemas"]["GradeItemResponse"][];
        };
        UpdateExamScheduleRequest: {
            /** Format: uuid */
            academicYearId: string;
            /** Format: uuid */
            semesterId: string;
            /** @enum {string} */
            sessionType: "NORMAL" | "RATTRAPAGE";
        };
        ExamScheduleResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: uuid */
            semesterId?: string;
            /** @enum {string} */
            sessionType?: "NORMAL" | "RATTRAPAGE";
            /** @enum {string} */
            publicationStatus?: "PUBLISHED" | "DRAFT";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateEstablishmentRequest: {
            name: string;
            /** @enum {string} */
            type: "SCHOOL" | "FACULTY" | "INSTITUTE";
        };
        EstablishmentResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            universityId?: string;
            name?: string;
            /** @enum {string} */
            type?: "SCHOOL" | "FACULTY" | "INSTITUTE";
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE" | "ARCHIVED";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        ReplaceRankPreferencesRequest: {
            academicRankIds: string[];
        };
        TeachingAssignmentRankPreferenceResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** @enum {string} */
            componentType?: "COURSE" | "TD" | "TP";
            /** Format: uuid */
            academicRankId?: string;
            academicRankCode?: string;
            academicRankName?: string;
            /** Format: int32 */
            priority?: number;
        };
        UpdateClassGroupRequest: {
            name: string;
            /** @enum {string} */
            status: "ACTIVE" | "INACTIVE" | "ARCHIVED";
        };
        ClassGroupResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: uuid */
            programFiliereId?: string;
            /** Format: uuid */
            establishmentId?: string;
            name?: string;
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE" | "ARCHIVED";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateBlockRequest: {
            code: string;
            name: string;
            /** @enum {string} */
            status: "ACTIVE" | "INACTIVE";
        };
        BlockResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            code?: string;
            name?: string;
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateAdminRequest: {
            /** Format: email */
            universityEmail: string;
            firstName: string;
            lastName: string;
            /** Format: date */
            birth_date: string;
            cin?: string;
            /** @enum {string} */
            sex: "MALE" | "FEMALE";
            phone_number?: string;
        };
        AdminProfileResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            accountId?: string;
            /** Format: uuid */
            establishmentId?: string;
            email?: string;
            /** @enum {string} */
            role?: "ROOT_SUPER_ADMIN" | "SUPER_ADMIN" | "ADMIN" | "PROFESSOR" | "STUDENT";
            /** @enum {string} */
            status?: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
            firstName?: string;
            lastName?: string;
            /** Format: date */
            birthDate?: string;
            cin?: string;
            /** @enum {string} */
            sex?: "MALE" | "FEMALE";
            phoneNumber?: string;
        };
        ReplaceAdminPermissionGrantsRequest: {
            permissions: ("DEPARTMENT_VIEW" | "DEPARTMENT_CREATE" | "DEPARTMENT_UPDATE" | "DEPARTMENT_DELETE" | "PROGRAM_FILIERE_VIEW" | "PROGRAM_FILIERE_CREATE" | "PROGRAM_FILIERE_UPDATE" | "PROGRAM_FILIERE_DELETE" | "DEGREE_CYCLE_VIEW" | "DEGREE_CYCLE_CREATE" | "DEGREE_CYCLE_UPDATE" | "DEGREE_CYCLE_DELETE" | "PROGRAM_PATH_VIEW" | "PROGRAM_PATH_CREATE" | "PROGRAM_PATH_UPDATE" | "PROGRAM_PATH_DELETE" | "ACADEMIC_LEVEL_VIEW" | "ACADEMIC_LEVEL_CREATE" | "ACADEMIC_LEVEL_UPDATE" | "ACADEMIC_LEVEL_DELETE" | "ACADEMIC_RULE_PROFILE_VIEW" | "ACADEMIC_RULE_PROFILE_CREATE" | "ACADEMIC_RULE_PROFILE_UPDATE" | "ACADEMIC_RULE_ASSIGNMENT_VIEW" | "ACADEMIC_RULE_ASSIGNMENT_CREATE" | "ACADEMIC_YEAR_VIEW" | "ACADEMIC_YEAR_CREATE" | "ACADEMIC_YEAR_UPDATE" | "ACADEMIC_YEAR_DELETE" | "SEMESTER_VIEW" | "SEMESTER_CREATE" | "SEMESTER_UPDATE" | "SEMESTER_DELETE" | "SUBJECT_MODULE_VIEW" | "SUBJECT_MODULE_CREATE" | "SUBJECT_MODULE_UPDATE" | "SUBJECT_MODULE_DELETE" | "CLASS_GROUP_VIEW" | "CLASS_GROUP_CREATE" | "CLASS_GROUP_UPDATE" | "ADMIN_CREATE" | "STUDENT_VIEW" | "STUDENT_CREATE" | "STUDENT_UPDATE" | "STUDENT_ACCOUNT_MANAGE" | "PROFESSOR_VIEW" | "PROFESSOR_CREATE" | "PROFESSOR_UPDATE" | "PROFESSOR_ACCOUNT_MANAGE" | "ACADEMIC_DOMAIN_VIEW" | "ACADEMIC_DOMAIN_CREATE" | "ACADEMIC_DOMAIN_UPDATE" | "ACADEMIC_DOMAIN_DELETE" | "PROFESSOR_EXPERTISE_VIEW" | "PROFESSOR_EXPERTISE_UPDATE" | "MODULE_TEACHING_COMPONENT_VIEW" | "MODULE_TEACHING_COMPONENT_UPDATE" | "TEACHING_GROUP_POLICY_VIEW" | "TEACHING_GROUP_POLICY_UPDATE" | "TEACHING_GROUP_VIEW" | "TEACHING_GROUP_GENERATE" | "TEACHING_GROUP_UPDATE" | "ACADEMIC_REGISTRATION_VIEW" | "ACADEMIC_REGISTRATION_CREATE" | "ACADEMIC_REGISTRATION_UPDATE" | "MODULE_CLASS_RESPONSIBILITY_VIEW" | "MODULE_CLASS_RESPONSIBILITY_CREATE" | "MODULE_CLASS_RESPONSIBILITY_DELETE" | "TEACHING_REQUIREMENT_VIEW" | "TEACHING_REQUIREMENT_GENERATE" | "TEACHING_ASSIGNMENT_VIEW" | "TEACHING_ASSIGNMENT_CREATE" | "TEACHING_ASSIGNMENT_DELETE" | "ABSENCE_VIEW" | "BLOCK_VIEW" | "BLOCK_CREATE" | "BLOCK_UPDATE" | "BLOCK_DELETE" | "ROOM_VIEW" | "ROOM_CREATE" | "ROOM_UPDATE" | "ROOM_DELETE" | "SEMESTER_SCHEDULE_VIEW" | "SEMESTER_SCHEDULE_CREATE" | "SEMESTER_SCHEDULE_UPDATE" | "SEMESTER_SCHEDULE_PUBLISH" | "EXAM_SCHEDULE_VIEW" | "EXAM_SCHEDULE_CREATE" | "EXAM_SCHEDULE_UPDATE" | "EXAM_SCHEDULE_DELETE" | "EXAM_SCHEDULE_PUBLISH" | "GRADE_VIEW" | "GRADE_REVIEW" | "GRADE_APPROVE" | "GRADE_PUBLISH")[];
        };
        AdminPermissionGrantsResponse: {
            /** Format: uuid */
            adminId?: string;
            /** Format: uuid */
            establishmentId?: string;
            permissions?: ("DEPARTMENT_VIEW" | "DEPARTMENT_CREATE" | "DEPARTMENT_UPDATE" | "DEPARTMENT_DELETE" | "PROGRAM_FILIERE_VIEW" | "PROGRAM_FILIERE_CREATE" | "PROGRAM_FILIERE_UPDATE" | "PROGRAM_FILIERE_DELETE" | "DEGREE_CYCLE_VIEW" | "DEGREE_CYCLE_CREATE" | "DEGREE_CYCLE_UPDATE" | "DEGREE_CYCLE_DELETE" | "PROGRAM_PATH_VIEW" | "PROGRAM_PATH_CREATE" | "PROGRAM_PATH_UPDATE" | "PROGRAM_PATH_DELETE" | "ACADEMIC_LEVEL_VIEW" | "ACADEMIC_LEVEL_CREATE" | "ACADEMIC_LEVEL_UPDATE" | "ACADEMIC_LEVEL_DELETE" | "ACADEMIC_RULE_PROFILE_VIEW" | "ACADEMIC_RULE_PROFILE_CREATE" | "ACADEMIC_RULE_PROFILE_UPDATE" | "ACADEMIC_RULE_ASSIGNMENT_VIEW" | "ACADEMIC_RULE_ASSIGNMENT_CREATE" | "ACADEMIC_YEAR_VIEW" | "ACADEMIC_YEAR_CREATE" | "ACADEMIC_YEAR_UPDATE" | "ACADEMIC_YEAR_DELETE" | "SEMESTER_VIEW" | "SEMESTER_CREATE" | "SEMESTER_UPDATE" | "SEMESTER_DELETE" | "SUBJECT_MODULE_VIEW" | "SUBJECT_MODULE_CREATE" | "SUBJECT_MODULE_UPDATE" | "SUBJECT_MODULE_DELETE" | "CLASS_GROUP_VIEW" | "CLASS_GROUP_CREATE" | "CLASS_GROUP_UPDATE" | "ADMIN_CREATE" | "STUDENT_VIEW" | "STUDENT_CREATE" | "STUDENT_UPDATE" | "STUDENT_ACCOUNT_MANAGE" | "PROFESSOR_VIEW" | "PROFESSOR_CREATE" | "PROFESSOR_UPDATE" | "PROFESSOR_ACCOUNT_MANAGE" | "ACADEMIC_DOMAIN_VIEW" | "ACADEMIC_DOMAIN_CREATE" | "ACADEMIC_DOMAIN_UPDATE" | "ACADEMIC_DOMAIN_DELETE" | "PROFESSOR_EXPERTISE_VIEW" | "PROFESSOR_EXPERTISE_UPDATE" | "MODULE_TEACHING_COMPONENT_VIEW" | "MODULE_TEACHING_COMPONENT_UPDATE" | "TEACHING_GROUP_POLICY_VIEW" | "TEACHING_GROUP_POLICY_UPDATE" | "TEACHING_GROUP_VIEW" | "TEACHING_GROUP_GENERATE" | "TEACHING_GROUP_UPDATE" | "ACADEMIC_REGISTRATION_VIEW" | "ACADEMIC_REGISTRATION_CREATE" | "ACADEMIC_REGISTRATION_UPDATE" | "MODULE_CLASS_RESPONSIBILITY_VIEW" | "MODULE_CLASS_RESPONSIBILITY_CREATE" | "MODULE_CLASS_RESPONSIBILITY_DELETE" | "TEACHING_REQUIREMENT_VIEW" | "TEACHING_REQUIREMENT_GENERATE" | "TEACHING_ASSIGNMENT_VIEW" | "TEACHING_ASSIGNMENT_CREATE" | "TEACHING_ASSIGNMENT_DELETE" | "ABSENCE_VIEW" | "BLOCK_VIEW" | "BLOCK_CREATE" | "BLOCK_UPDATE" | "BLOCK_DELETE" | "ROOM_VIEW" | "ROOM_CREATE" | "ROOM_UPDATE" | "ROOM_DELETE" | "SEMESTER_SCHEDULE_VIEW" | "SEMESTER_SCHEDULE_CREATE" | "SEMESTER_SCHEDULE_UPDATE" | "SEMESTER_SCHEDULE_PUBLISH" | "EXAM_SCHEDULE_VIEW" | "EXAM_SCHEDULE_CREATE" | "EXAM_SCHEDULE_UPDATE" | "EXAM_SCHEDULE_DELETE" | "EXAM_SCHEDULE_PUBLISH" | "GRADE_VIEW" | "GRADE_REVIEW" | "GRADE_APPROVE" | "GRADE_PUBLISH")[];
        };
        UpdateAcademicYearRequest: {
            label: string;
            /** @enum {string} */
            status: "PLANNED" | "ACTIVE" | "CLOSED";
        };
        AcademicYearResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            label?: string;
            /** Format: int32 */
            startYear?: number;
            /** Format: int32 */
            endYear?: number;
            /** @enum {string} */
            status?: "PLANNED" | "ACTIVE" | "CLOSED";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateAcademicRuleProfileRequest: {
            name: string;
            moduleValidationThreshold: number;
            compensationMinimumThreshold: number;
            semesterValidationAverage: number;
            annualValidationAverage?: number;
            /** Format: int32 */
            maximumModuleInscriptions: number;
            /** @enum {string} */
            sessionGradePolicy: "BEST_GRADE" | "RATTRAPAGE_REPLACES_NORMAL" | "RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD";
            allowProgressionWithDebt: boolean;
            /** Format: int32 */
            maximumCarriedModules: number;
            /** Format: int32 */
            maximumUnjustifiedAbsences: number;
            /** @enum {string} */
            absenceExclusionPolicy: "NORMAL_ONLY" | "NORMAL_AND_RATTRAPAGE";
            /** @enum {string} */
            status: "ACTIVE" | "INACTIVE";
        };
        AcademicRuleProfileResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            name?: string;
            /** Format: int32 */
            version?: number;
            moduleValidationThreshold?: number;
            compensationMinimumThreshold?: number;
            semesterValidationAverage?: number;
            annualValidationAverage?: number;
            /** Format: int32 */
            maximumModuleInscriptions?: number;
            /** @enum {string} */
            sessionGradePolicy?: "BEST_GRADE" | "RATTRAPAGE_REPLACES_NORMAL" | "RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD";
            allowProgressionWithDebt?: boolean;
            /** Format: int32 */
            maximumCarriedModules?: number;
            /** Format: int32 */
            maximumUnjustifiedAbsences?: number;
            /** @enum {string} */
            absenceExclusionPolicy?: "NORMAL_ONLY" | "NORMAL_AND_RATTRAPAGE";
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        AssignStudentClassRequest: {
            /** Format: uuid */
            classGroupId: string;
        };
        StudentClassAssignmentResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            academicRegistrationId?: string;
            /** Format: uuid */
            semesterRegistrationId?: string;
            /** Format: uuid */
            semesterId?: string;
            /** Format: uuid */
            classGroupId?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateAcademicRegistrationRequest: {
            /** @enum {string} */
            status: "ACTIVE" | "COMPLETED" | "CANCELLED" | "SUSPENDED";
        };
        AcademicRegistrationResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            studentId?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** Format: uuid */
            programFiliereId?: string;
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** @enum {string} */
            status?: "ACTIVE" | "COMPLETED" | "CANCELLED" | "SUSPENDED";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        AcademicRankRequest: {
            code: string;
            name: string;
            /** Format: int32 */
            seniorityOrder: number;
            canHoldModuleResponsibility?: boolean;
            /** @enum {string} */
            status: "ACTIVE" | "INACTIVE";
        };
        AcademicRankResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            code?: string;
            name?: string;
            /** Format: int32 */
            seniorityOrder?: number;
            canHoldModuleResponsibility?: boolean;
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE";
        };
        UpdateAcademicLevelRequest: {
            name: string;
            /** Format: int32 */
            levelOrder?: number;
        };
        AcademicLevelResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            programFiliereId?: string;
            /** Format: uuid */
            establishmentId?: string;
            name?: string;
            /** Format: int32 */
            levelOrder?: number;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        ReplaceTeachingGroupPoliciesRequest: {
            policies: components["schemas"]["TeachingGroupPolicyItemRequest"][];
        };
        TeachingGroupPolicyItemRequest: {
            /** @enum {string} */
            groupType: "TD" | "TP";
            /** Format: int32 */
            minimumGroupSize?: number;
            /** Format: int32 */
            maximumGroupSize?: number;
        };
        TeachingGroupPolicyResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** @enum {string} */
            groupType?: "TD" | "TP";
            /** Format: int32 */
            minimumGroupSize?: number;
            /** Format: int32 */
            maximumGroupSize?: number;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        GenerateClassGroupsRequest: {
            /** Format: int32 */
            minimumGroupSize?: number;
            /** Format: int32 */
            maximumGroupSize?: number;
        };
        ClassGroupRebalanceResponse: {
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: int32 */
            totalStudents?: number;
            /** Format: int32 */
            semesterAssignmentsChanged?: number;
            groups?: components["schemas"]["GeneratedClassGroupResponse"][];
        };
        GeneratedClassGroupResponse: {
            /** Format: uuid */
            classGroupId?: string;
            name?: string;
            /** Format: int32 */
            studentCount?: number;
        };
        BulkAssignStudentClassesRequest: {
            assignments: components["schemas"]["BulkClassAssignmentItemRequest"][];
        };
        BulkClassAssignmentItemRequest: {
            /** Format: uuid */
            academicRegistrationId: string;
            /** Format: uuid */
            classGroupId: string;
        };
        BulkClassAssignmentResponse: {
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: int32 */
            studentsProcessed?: number;
            /** Format: int32 */
            semesterAssignmentsCreated?: number;
        };
        UpdateAcademicDomainRequest: {
            code: string;
            name: string;
        };
        AcademicDomainResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            code?: string;
            name?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UpdateAbsenceJustificationRequest: {
            justified: boolean;
            justificationNote?: string;
        };
        AbsenceRecordResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            moduleRegistrationId?: string;
            /** Format: uuid */
            studentId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** Format: uuid */
            teachingAssignmentId?: string;
            /** Format: uuid */
            recordedByProfessorId?: string;
            /** Format: date */
            absenceDate?: string;
            justified?: boolean;
            justificationNote?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        CreateAbsenceRequest: {
            /** Format: uuid */
            moduleRegistrationId: string;
            /** Format: date */
            absenceDate: string;
        };
        ActionResponse: {
            success?: boolean;
            message?: string;
        };
        ResetPasswordRequest: {
            newPassword: string;
        };
        ResetManagedPasswordRequest: {
            newPassword: string;
        };
        TeachingRequirementResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** Format: uuid */
            moduleTeachingComponentId?: string;
            /** @enum {string} */
            componentType?: "COURSE" | "TD" | "TP";
            /** Format: uuid */
            teachingGroupId?: string;
            teachingGroupName?: string;
            /** @enum {string} */
            audienceType?: "WHOLE_COHORT" | "CLASS_GROUP" | "SUBGROUP";
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE";
        };
        ProfessorTeachingWorkloadResponse: {
            /** Format: uuid */
            professorId?: string;
            employeeNumber?: string;
            /** Format: int32 */
            assignedWeeklyMinutes?: number;
            /** Format: int32 */
            maximumWeeklyTeachingMinutes?: number;
        };
        TeachingAssignmentGenerationResponse: {
            /** Format: uuid */
            semesterId?: string;
            /** Format: int32 */
            preservedAssignmentCount?: number;
            createdAssignments?: components["schemas"]["TeachingAssignmentResponse"][];
            unresolvedRequirements?: components["schemas"]["UnresolvedTeachingRequirementResponse"][];
            professorWorkloads?: components["schemas"]["ProfessorTeachingWorkloadResponse"][];
        };
        TeachingAssignmentResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** Format: uuid */
            professorId?: string;
            /** Format: uuid */
            teachingRequirementId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** @enum {string} */
            componentType?: "COURSE" | "TD" | "TP";
            /** Format: uuid */
            teachingGroupId?: string;
            teachingGroupName?: string;
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE";
            /** @enum {string} */
            assignmentSource?: "MANUAL" | "AUTOMATIC";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        UnresolvedTeachingRequirementResponse: {
            /** Format: uuid */
            teachingRequirementId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** @enum {string} */
            componentType?: "COURSE" | "TD" | "TP";
            /** Format: uuid */
            teachingGroupId?: string;
            teachingGroupName?: string;
            /** @enum {string} */
            reason?: "NO_ACTIVE_PROFESSOR" | "MISSING_ACADEMIC_DOMAIN_CONFIGURATION" | "NO_MATCHING_EXPERTISE" | "NO_ELIGIBLE_ACADEMIC_RANK" | "WORKLOAD_CAPACITY_EXCEEDED";
            message?: string;
        };
        CreateSubjectModuleRequest: {
            code: string;
            title: string;
            academicDomainIds: string[];
        };
        SemesterScheduleResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: uuid */
            semesterId?: string;
            /** @enum {string} */
            publicationStatus?: "DRAFT" | "PUBLISHED";
            /** Format: date-time */
            publishedAt?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        CreateScheduleEntryRequest: {
            /** Format: uuid */
            teachingAssignmentId: string;
            /** @enum {string} */
            dayOfWeek: "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";
            startTime: string;
            endTime: string;
            /** Format: uuid */
            roomId: string;
        };
        CreateAcademicLevelRequest: {
            name: string;
            /** Format: int32 */
            levelOrder?: number;
            /** Format: uuid */
            initialAcademicYearId: string;
            /** Format: uuid */
            academicRuleProfileId: string;
        };
        ExamCandidateResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            moduleExamId?: string;
            /** Format: uuid */
            moduleRegistrationId?: string;
            /** Format: uuid */
            studentId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** @enum {string} */
            sessionType?: "NORMAL" | "RATTRAPAGE";
            /** Format: date */
            examDate?: string;
            startTime?: string;
            location?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        CreateModuleExamRequest: {
            /** Format: uuid */
            subjectModuleId: string;
            /** Format: uuid */
            classGroupId: string;
            /** Format: date */
            examDate: string;
            startTime: string;
            endTime?: string;
            location?: string;
        };
        CreateEstablishmentRequest: {
            /** Format: uuid */
            universityId: string;
            name: string;
            /** @enum {string} */
            type: "SCHOOL" | "FACULTY" | "INSTITUTE";
        };
        CreateSuperAdminRequest: {
            /** Format: email */
            universityEmail: string;
            password: string;
            firstName: string;
            lastName: string;
            /** Format: date */
            birth_date: string;
            cin?: string;
            /** @enum {string} */
            sex: "MALE" | "FEMALE";
            phone_number?: string;
        };
        CreateSuperAdminResponse: {
            /** Format: uuid */
            userAccountId?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** @enum {string} */
            roleType?: "ROOT_SUPER_ADMIN" | "SUPER_ADMIN" | "ADMIN" | "PROFESSOR" | "STUDENT";
        };
        CreateAdminRequest: {
            /** Format: email */
            universityEmail: string;
            password: string;
            firstName: string;
            lastName: string;
            /** Format: date */
            birth_date: string;
            /** @enum {string} */
            sex: "MALE" | "FEMALE";
            phone_number?: string;
        };
        CreateAdminResponse: {
            /** Format: uuid */
            adminId?: string;
            /** Format: uuid */
            userAccountId?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** @enum {string} */
            roleType?: "ROOT_SUPER_ADMIN" | "SUPER_ADMIN" | "ADMIN" | "PROFESSOR" | "STUDENT";
        };
        CreateTeachingAssignmentRequest: {
            /** Format: uuid */
            professorId: string;
            /** Format: uuid */
            teachingRequirementId: string;
        };
        CreateStudentRequest: {
            apogeeCode: string;
            nationalStudentCode?: string;
            cin?: string;
            /** Format: date */
            initialEnrollmentDate: string;
            /** Format: email */
            universityEmail: string;
            password: string;
            firstName: string;
            lastName: string;
            /** Format: date */
            birth_date: string;
            placeOfBirth: string;
            nationality: string;
            /** @enum {string} */
            sex: "MALE" | "FEMALE";
            phone_number?: string;
        };
        CreateStudentResponse: {
            /** Format: uuid */
            studentId?: string;
            /** Format: uuid */
            userAccountId?: string;
            /** Format: uuid */
            establishmentId?: string;
            apogeeCode?: string;
            /** @enum {string} */
            roleType?: "ROOT_SUPER_ADMIN" | "SUPER_ADMIN" | "ADMIN" | "PROFESSOR" | "STUDENT";
        };
        CreateSemesterScheduleRequest: {
            /** Format: uuid */
            academicYearId: string;
            /** Format: uuid */
            semesterId: string;
        };
        CreateRoomRequest: {
            /** Format: uuid */
            blockId?: string;
            code: string;
            name: string;
            /** @enum {string} */
            roomType: "LECTURE_HALL" | "CLASSROOM" | "COMPUTER_LAB";
            /** Format: int32 */
            capacity?: number;
        };
        CreateProgramPathRequest: {
            name: string;
        };
        ProgramPathResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            name?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        CreateProfessorRequest: {
            employeeNumber: string;
            /** Format: uuid */
            academicRankId?: string;
            academicRank?: string;
            /** Format: date */
            hireDate?: string;
            /** Format: int32 */
            maximumWeeklyTeachingMinutes: number;
            cin?: string;
            /** Format: email */
            universityEmail: string;
            password: string;
            firstName: string;
            lastName: string;
            /** Format: date */
            birth_date: string;
            placeOfBirth: string;
            nationality: string;
            /** @enum {string} */
            sex: "MALE" | "FEMALE";
            phone_number?: string;
        };
        CreateProfessorResponse: {
            /** Format: uuid */
            professorId?: string;
            /** Format: uuid */
            userAccountId?: string;
            /** Format: uuid */
            establishmentId?: string;
            employeeNumber?: string;
            /** @enum {string} */
            roleType?: "ROOT_SUPER_ADMIN" | "SUPER_ADMIN" | "ADMIN" | "PROFESSOR" | "STUDENT";
        };
        CreateModuleClassResponsibilityRequest: {
            /** Format: uuid */
            professorId: string;
            /** Format: uuid */
            subjectModuleId: string;
            /** Format: uuid */
            classGroupId: string;
            /** Format: uuid */
            academicYearId: string;
            /** Format: uuid */
            semesterId: string;
        };
        ModuleClassResponsibilityResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** Format: uuid */
            professorId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            /** Format: uuid */
            classGroupId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: uuid */
            semesterId?: string;
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        CreateExamSchedule: {
            /** Format: uuid */
            academicYearId: string;
            /** Format: uuid */
            semesterId: string;
            /** @enum {string} */
            sessionType: "NORMAL" | "RATTRAPAGE";
        };
        CreateDepartmentRequest: {
            name: string;
        };
        DepartmentResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            name?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        CreateDegreeCycleRequest: {
            name: string;
        };
        DegreeCycleResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            establishmentId?: string;
            name?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        CreateBlockRequest: {
            code: string;
            name: string;
        };
        CreateAcademicYearRequest: {
            label: string;
            /** @enum {string} */
            status: "PLANNED" | "ACTIVE" | "CLOSED";
        };
        CreateAcademicRuleProfileRequest: {
            name: string;
            moduleValidationThreshold: number;
            compensationMinimumThreshold: number;
            semesterValidationAverage: number;
            annualValidationAverage?: number;
            /** Format: int32 */
            maximumModuleInscriptions: number;
            /** @enum {string} */
            sessionGradePolicy: "BEST_GRADE" | "RATTRAPAGE_REPLACES_NORMAL" | "RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD";
            allowProgressionWithDebt: boolean;
            /** Format: int32 */
            maximumCarriedModules: number;
            /** Format: int32 */
            maximumUnjustifiedAbsences: number;
            /** @enum {string} */
            absenceExclusionPolicy: "NORMAL_ONLY" | "NORMAL_AND_RATTRAPAGE";
            /** @enum {string} */
            status: "ACTIVE" | "INACTIVE";
        };
        CreateAcademicRegistrationRequest: {
            /** Format: uuid */
            studentId: string;
            /** Format: uuid */
            programFiliereId: string;
            /** Format: uuid */
            academicLevelId: string;
            /** Format: uuid */
            academicYearId: string;
        };
        CreateAcademicDomainRequest: {
            code: string;
            name: string;
        };
        CreateProgramFiliereRequest: {
            code: string;
            name: string;
            /** Format: uuid */
            degreeCycleId: string;
            /** Format: uuid */
            programPathId: string;
        };
        ProgramFiliereResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            departmentId?: string;
            /** Format: uuid */
            establishmentId?: string;
            /** Format: uuid */
            degreeCycleId?: string;
            /** Format: uuid */
            programPathId?: string;
            code?: string;
            name?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        RefreshRequest: {
            refreshToken: string;
        };
        AuthResponse: {
            /** Format: uuid */
            userAccountId?: string;
            role?: string;
            /** Format: uuid */
            roleEntityId?: string;
            /** Format: uuid */
            establishmentId?: string;
            universityEmail?: string;
            firstName?: string;
            lastName?: string;
            accountStatus?: string;
            accessToken?: string;
            refreshToken?: string;
        };
        LoginRequest: {
            /** Format: email */
            universityEmail: string;
            password: string;
        };
        ChangePasswordRequest: {
            currentPassword: string;
            newPassword: string;
        };
        ResetAdminPasswordRequest: {
            newPassword: string;
        };
        CreateSemesterRequest: {
            name: string;
            /** Format: int32 */
            semesterOrder?: number;
            /** @enum {string} */
            termType: "AUTUMN" | "SPRING";
        };
        CreateAcademicLevelRuleAssignmentRequest: {
            /** Format: uuid */
            academicYearId: string;
            /** Format: uuid */
            academicRuleProfileId: string;
        };
        AcademicLevelRuleAssignmentResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: uuid */
            academicRuleProfileId?: string;
            /** @enum {string} */
            status?: "ACTIVE" | "INACTIVE";
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        CreateClassGroupRequest: {
            name: string;
            /** @enum {string} */
            status: "ACTIVE" | "INACTIVE" | "ARCHIVED";
        };
        ClassGroupGenerationResponse: {
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: int32 */
            totalStudents?: number;
            /** Format: int32 */
            semesterAssignmentsCreated?: number;
            groups?: components["schemas"]["GeneratedClassGroupResponse"][];
        };
        UpdateProgramPathRequest: {
            name: string;
        };
        UpdateProgramFiliereRequest: {
            code: string;
            name: string;
            /** Format: uuid */
            degreeCycleId: string;
            /** Format: uuid */
            programPathId: string;
        };
        UpdateDepartmentRequest: {
            name: string;
        };
        UpdateDegreeCycleRequest: {
            name: string;
        };
        UniversityResponse: {
            /** Format: uuid */
            universityId?: string;
            universityName?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        TeachingAssignmentStudentResponse: {
            /** Format: uuid */
            studentId?: string;
            apogeeCode?: string;
            nationalStudentCode?: string;
            universityEmail?: string;
            firstName?: string;
            lastName?: string;
        };
        StudentGradeResponse: {
            /** Format: uuid */
            gradeRecordId?: string;
            /** Format: uuid */
            moduleRegistrationId?: string;
            /** Format: uuid */
            moduleExamId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            subjectModuleCode?: string;
            subjectModuleTitle?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: uuid */
            semesterId?: string;
            /** @enum {string} */
            sessionType?: "NORMAL" | "RATTRAPAGE";
            /** Format: int32 */
            inscriptionNumber?: number;
            gradeValue?: number;
            /** @enum {string} */
            zeroGradeReason?: "ABSENT" | "EARNED_ZERO";
            /** Format: date-time */
            publishedAt?: string;
            /** Format: uuid */
            moduleResultId?: string;
            finalGradeValue?: number;
            /** @enum {string} */
            moduleResultStatus?: "V" | "AV" | "NV";
            /** Format: uuid */
            academicRuleProfileId?: string;
            /** Format: date-time */
            moduleResultCalculatedAt?: string;
        };
        SemesterResultResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            semesterRegistrationId?: string;
            /** Format: uuid */
            academicRuleProfileId?: string;
            semesterAverage?: number;
            /** @enum {string} */
            resultStatus?: "VALIDATED" | "NON_VALIDATED";
            /** Format: date-time */
            evaluatedAt?: string;
        };
        ModuleRegistrationResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            semesterRegistrationId?: string;
            /** Format: uuid */
            subjectModuleId?: string;
            subjectModuleCode?: string;
            subjectModuleTitle?: string;
            /** Format: uuid */
            originAcademicLevelId?: string;
            /** Format: int32 */
            inscriptionNumber?: number;
            /** @enum {string} */
            status?: "ACTIVE" | "COMPLETED" | "CANCELLED";
        };
        PermissionResponse: {
            /** Format: uuid */
            id?: string;
            /** @enum {string} */
            code?: "DEPARTMENT_VIEW" | "DEPARTMENT_CREATE" | "DEPARTMENT_UPDATE" | "DEPARTMENT_DELETE" | "PROGRAM_FILIERE_VIEW" | "PROGRAM_FILIERE_CREATE" | "PROGRAM_FILIERE_UPDATE" | "PROGRAM_FILIERE_DELETE" | "DEGREE_CYCLE_VIEW" | "DEGREE_CYCLE_CREATE" | "DEGREE_CYCLE_UPDATE" | "DEGREE_CYCLE_DELETE" | "PROGRAM_PATH_VIEW" | "PROGRAM_PATH_CREATE" | "PROGRAM_PATH_UPDATE" | "PROGRAM_PATH_DELETE" | "ACADEMIC_LEVEL_VIEW" | "ACADEMIC_LEVEL_CREATE" | "ACADEMIC_LEVEL_UPDATE" | "ACADEMIC_LEVEL_DELETE" | "ACADEMIC_RULE_PROFILE_VIEW" | "ACADEMIC_RULE_PROFILE_CREATE" | "ACADEMIC_RULE_PROFILE_UPDATE" | "ACADEMIC_RULE_ASSIGNMENT_VIEW" | "ACADEMIC_RULE_ASSIGNMENT_CREATE" | "ACADEMIC_YEAR_VIEW" | "ACADEMIC_YEAR_CREATE" | "ACADEMIC_YEAR_UPDATE" | "ACADEMIC_YEAR_DELETE" | "SEMESTER_VIEW" | "SEMESTER_CREATE" | "SEMESTER_UPDATE" | "SEMESTER_DELETE" | "SUBJECT_MODULE_VIEW" | "SUBJECT_MODULE_CREATE" | "SUBJECT_MODULE_UPDATE" | "SUBJECT_MODULE_DELETE" | "CLASS_GROUP_VIEW" | "CLASS_GROUP_CREATE" | "CLASS_GROUP_UPDATE" | "ADMIN_CREATE" | "STUDENT_VIEW" | "STUDENT_CREATE" | "STUDENT_UPDATE" | "STUDENT_ACCOUNT_MANAGE" | "PROFESSOR_VIEW" | "PROFESSOR_CREATE" | "PROFESSOR_UPDATE" | "PROFESSOR_ACCOUNT_MANAGE" | "ACADEMIC_DOMAIN_VIEW" | "ACADEMIC_DOMAIN_CREATE" | "ACADEMIC_DOMAIN_UPDATE" | "ACADEMIC_DOMAIN_DELETE" | "PROFESSOR_EXPERTISE_VIEW" | "PROFESSOR_EXPERTISE_UPDATE" | "MODULE_TEACHING_COMPONENT_VIEW" | "MODULE_TEACHING_COMPONENT_UPDATE" | "TEACHING_GROUP_POLICY_VIEW" | "TEACHING_GROUP_POLICY_UPDATE" | "TEACHING_GROUP_VIEW" | "TEACHING_GROUP_GENERATE" | "TEACHING_GROUP_UPDATE" | "ACADEMIC_REGISTRATION_VIEW" | "ACADEMIC_REGISTRATION_CREATE" | "ACADEMIC_REGISTRATION_UPDATE" | "MODULE_CLASS_RESPONSIBILITY_VIEW" | "MODULE_CLASS_RESPONSIBILITY_CREATE" | "MODULE_CLASS_RESPONSIBILITY_DELETE" | "TEACHING_REQUIREMENT_VIEW" | "TEACHING_REQUIREMENT_GENERATE" | "TEACHING_ASSIGNMENT_VIEW" | "TEACHING_ASSIGNMENT_CREATE" | "TEACHING_ASSIGNMENT_DELETE" | "ABSENCE_VIEW" | "BLOCK_VIEW" | "BLOCK_CREATE" | "BLOCK_UPDATE" | "BLOCK_DELETE" | "ROOM_VIEW" | "ROOM_CREATE" | "ROOM_UPDATE" | "ROOM_DELETE" | "SEMESTER_SCHEDULE_VIEW" | "SEMESTER_SCHEDULE_CREATE" | "SEMESTER_SCHEDULE_UPDATE" | "SEMESTER_SCHEDULE_PUBLISH" | "EXAM_SCHEDULE_VIEW" | "EXAM_SCHEDULE_CREATE" | "EXAM_SCHEDULE_UPDATE" | "EXAM_SCHEDULE_DELETE" | "EXAM_SCHEDULE_PUBLISH" | "GRADE_VIEW" | "GRADE_REVIEW" | "GRADE_APPROVE" | "GRADE_PUBLISH";
            name?: string;
        };
        CurrentUserResponse: {
            /** Format: uuid */
            userAccountId?: string;
            role?: string;
            /** Format: uuid */
            roleEntityId?: string;
            /** Format: uuid */
            establishmentId?: string;
            universityEmail?: string;
            firstName?: string;
            lastName?: string;
            accountStatus?: string;
        };
        SemesterRegistrationResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            academicRegistrationId?: string;
            /** Format: uuid */
            semesterId?: string;
            semesterName?: string;
            /** Format: int32 */
            semesterOrder?: number;
        };
        ProgressionDecisionResponse: {
            /** Format: uuid */
            id?: string;
            /** Format: uuid */
            academicRegistrationId?: string;
            /** Format: uuid */
            academicRuleProfileId?: string;
            /** @enum {string} */
            decisionStatus?: "PROMOTED" | "PROMOTED_WITH_DEBT" | "REPEAT" | "FAILED";
            annualAverage?: number;
            /** Format: int32 */
            outstandingModuleCount?: number;
            /** Format: date-time */
            decidedAt?: string;
        };
        ClassGroupRosterGroupResponse: {
            /** Format: uuid */
            classGroupId?: string;
            name?: string;
            academicRegistrationIds?: string[];
        };
        ClassGroupRosterResponse: {
            /** Format: uuid */
            academicLevelId?: string;
            /** Format: uuid */
            academicYearId?: string;
            /** Format: uuid */
            semesterId?: string;
            /** Format: int32 */
            totalStudents?: number;
            unassignedAcademicRegistrationIds?: string[];
            groups?: components["schemas"]["ClassGroupRosterGroupResponse"][];
        };
    };
    responses: never;
    parameters: never;
    requestBodies: never;
    headers: never;
    pathItems: never;
}
export type $defs = Record<string, never>;
export interface operations {
    moveMember: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                teachingGroupId: string;
                semesterRegistrationId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingGroupRosterResponse"];
                };
            };
        };
    };
    getSuperAdmin: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                superAdminId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SuperAdminProfileResponse"];
                };
            };
        };
    };
    updateSuperAdmin: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                superAdminId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateSuperAdminRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SuperAdminProfileResponse"];
                };
            };
        };
    };
    getSubjectModule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                subjectModuleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SubjectModuleResponse"];
                };
            };
        };
    };
    updateSubjectModule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                subjectModuleId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateSubjectModuleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SubjectModuleResponse"];
                };
            };
        };
    };
    deleteSubjectModule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                subjectModuleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getModuleTeachingComponents: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                subjectModuleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleTeachingComponentResponse"][];
                };
            };
        };
    };
    replaceModuleTeachingComponents: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                subjectModuleId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReplaceModuleTeachingComponentsRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleTeachingComponentResponse"][];
                };
            };
        };
    };
    getStudent: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["StudentProfileResponse"];
                };
            };
        };
    };
    updateStudent: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateStudentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["StudentProfileResponse"];
                };
            };
        };
    };
    getSemester: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterResponse"];
                };
            };
        };
    };
    updateSemester: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateSemesterRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterResponse"];
                };
            };
        };
    };
    deleteSemester: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getScheduleEntry: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                scheduleEntryId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ScheduleEntryResponse"];
                };
            };
        };
    };
    updateScheduleEntry: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                scheduleEntryId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateScheduleEntryRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ScheduleEntryResponse"];
                };
            };
        };
    };
    deleteScheduleEntry: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                scheduleEntryId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getRoom: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                roomId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["RoomResponse"];
                };
            };
        };
    };
    updateRoom: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                roomId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateRoomRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["RoomResponse"];
                };
            };
        };
    };
    deactivateRoom: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                roomId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getProfessor: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProfessorProfileResponse"];
                };
            };
        };
    };
    update: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateProfessorRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProfessorProfileResponse"];
                };
            };
        };
    };
    getProfessorExpertise: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProfessorExpertiseResponse"];
                };
            };
        };
    };
    replaceProfessorExpertise: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReplaceProfessorExpertiseRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProfessorExpertiseResponse"];
                };
            };
        };
    };
    getModuleExam: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleExamResponse"];
                };
            };
        };
    };
    updateModuleExam: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateModuleExamRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleExamResponse"];
                };
            };
        };
    };
    deleteModuleExam: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getGradeSheet: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["GradeSheetResponse"];
                };
            };
        };
    };
    saveDraftGradeSheet: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SaveGradeSheetRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["GradeSheetResponse"];
                };
            };
        };
    };
    getExamSchedule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                examScheduleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ExamScheduleResponse"];
                };
            };
        };
    };
    updateExamSchedule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                examScheduleId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateExamScheduleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ExamScheduleResponse"];
                };
            };
        };
    };
    deleteExamSchedule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                examScheduleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getEstablishmentById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["EstablishmentResponse"];
                };
            };
        };
    };
    updateEstablishment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateEstablishmentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["EstablishmentResponse"];
                };
            };
        };
    };
    replace: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
                componentType: "COURSE" | "TD" | "TP";
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReplaceRankPreferencesRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingAssignmentRankPreferenceResponse"][];
                };
            };
        };
    };
    getClassGroup: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                classGroupId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ClassGroupResponse"];
                };
            };
        };
    };
    updateClassGroup: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                classGroupId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateClassGroupRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ClassGroupResponse"];
                };
            };
        };
    };
    getBlock: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                blockId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["BlockResponse"];
                };
            };
        };
    };
    updateBlock: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                blockId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateBlockRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["BlockResponse"];
                };
            };
        };
    };
    deactivateBlock: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                blockId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getAdmin: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminProfileResponse"];
                };
            };
        };
    };
    updateAdmin: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateAdminRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminProfileResponse"];
                };
            };
        };
    };
    getAdminGrants: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminPermissionGrantsResponse"];
                };
            };
        };
    };
    replaceAdminGrants: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReplaceAdminPermissionGrantsRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminPermissionGrantsResponse"];
                };
            };
        };
    };
    getAcademicYear: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicYearId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicYearResponse"];
                };
            };
        };
    };
    updateAcademicYear: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicYearId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateAcademicYearRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicYearResponse"];
                };
            };
        };
    };
    deleteAcademicYear: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicYearId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getAcademicRuleProfile: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicRuleProfileId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRuleProfileResponse"];
                };
            };
        };
    };
    updateAcademicRuleProfile: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicRuleProfileId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateAcademicRuleProfileRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRuleProfileResponse"];
                };
            };
        };
    };
    getStudentClassAssignment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                registrationId: string;
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["StudentClassAssignmentResponse"];
                };
            };
        };
    };
    assignStudentClass: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                registrationId: string;
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AssignStudentClassRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["StudentClassAssignmentResponse"];
                };
            };
        };
    };
    getAcademicRegistration: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicRegistrationId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRegistrationResponse"];
                };
            };
        };
    };
    updateAcademicRegistration: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicRegistrationId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateAcademicRegistrationRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRegistrationResponse"];
                };
            };
        };
    };
    update_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                rankId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AcademicRankRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRankResponse"];
                };
            };
        };
    };
    delete: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                rankId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getAcademicLevel: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicLevelResponse"];
                };
            };
        };
    };
    updateAcademicLevel: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateAcademicLevelRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicLevelResponse"];
                };
            };
        };
    };
    deleteAcademicLevel: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getPolicies: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingGroupPolicyResponse"][];
                };
            };
        };
    };
    replacePolicies: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReplaceTeachingGroupPoliciesRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingGroupPolicyResponse"][];
                };
            };
        };
    };
    rebalanceClassGroups: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["GenerateClassGroupsRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ClassGroupRebalanceResponse"];
                };
            };
        };
    };
    bulkAssignStudentClasses: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["BulkAssignStudentClassesRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["BulkClassAssignmentResponse"];
                };
            };
        };
    };
    getAcademicDomain: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicDomainId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicDomainResponse"];
                };
            };
        };
    };
    updateAcademicDomain: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicDomainId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateAcademicDomainRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicDomainResponse"];
                };
            };
        };
    };
    deleteAcademicDomain: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicDomainId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    updateJustification: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                absenceId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateAbsenceJustificationRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AbsenceRecordResponse"];
                };
            };
        };
    };
    getTeachingAssignmentAbsences: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                teachingAssignmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AbsenceRecordResponse"][];
                };
            };
        };
    };
    createAbsence: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                teachingAssignmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateAbsenceRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AbsenceRecordResponse"];
                };
            };
        };
    };
    unlockAccount: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    restoreAccount: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    resetPassword: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ResetPasswordRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    lockAccount: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    deactivateAccount: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    archiveAccount: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    activateAccount: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    unlock: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    resetPassword_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ResetManagedPasswordRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    lock: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    deactivate: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    archive: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    generate: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingRequirementResponse"][];
                };
            };
        };
    };
    generate_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingGroupRosterResponse"];
                };
            };
        };
    };
    generateTeachingAssignments: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingAssignmentGenerationResponse"];
                };
            };
        };
    };
    getSubjectModules: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SubjectModuleResponse"][];
                };
            };
        };
    };
    createSubjectModule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateSubjectModuleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SubjectModuleResponse"];
                };
            };
        };
    };
    publishSemesterSchedule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                scheduleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterScheduleResponse"];
                };
            };
        };
    };
    getScheduleEntries: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                scheduleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ScheduleEntryResponse"][];
                };
            };
        };
    };
    createScheduleEntry: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                scheduleId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateScheduleEntryRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ScheduleEntryResponse"];
                };
            };
        };
    };
    getAcademicLevels: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                programFiliereId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicLevelResponse"][];
                };
            };
        };
    };
    createAcademicLevel: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                programFiliereId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateAcademicLevelRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicLevelResponse"];
                };
            };
        };
    };
    unlock_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    resetPassword_2: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ResetManagedPasswordRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    lock_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    deactivate_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    archive_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                professorId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    submitGradeSheet: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["GradeSheetResponse"];
                };
            };
        };
    };
    reviewGradeSheet: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["GradeSheetResponse"];
                };
            };
        };
    };
    publishGradeSheet: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["GradeSheetResponse"];
                };
            };
        };
    };
    approveGradeSheet: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["GradeSheetResponse"];
                };
            };
        };
    };
    generateCandidates: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ExamCandidateResponse"][];
                };
            };
        };
    };
    publishExamSchedule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                examScheduleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ExamScheduleResponse"];
                };
            };
        };
    };
    getModuleExams: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                examScheduleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleExamResponse"][];
                };
            };
        };
    };
    createModuleExam: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                examScheduleId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateModuleExamRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleExamResponse"];
                };
            };
        };
    };
    createEstablishment: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateEstablishmentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["EstablishmentResponse"];
                };
            };
        };
    };
    createSuperAdmin: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateSuperAdminRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CreateSuperAdminResponse"];
                };
            };
        };
    };
    deactivateEstablishment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getAdmins: {
        parameters: {
            query?: {
                query?: string;
                status?: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
                createdFrom?: string;
                createdTo?: string;
            };
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminProfileResponse"][];
                };
            };
        };
    };
    createAdmin: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateAdminRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CreateAdminResponse"];
                };
            };
        };
    };
    activateEstablishment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getTeachingAssignments: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingAssignmentResponse"][];
                };
            };
        };
    };
    createTeachingAssignment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateTeachingAssignmentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingAssignmentResponse"];
                };
            };
        };
    };
    getStudents: {
        parameters: {
            query?: {
                query?: string;
                status?: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
                enrolledFrom?: string;
                enrolledTo?: string;
            };
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["StudentProfileResponse"][];
                };
            };
        };
    };
    createStudent: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateStudentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CreateStudentResponse"];
                };
            };
        };
    };
    getSemesterSchedules: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterScheduleResponse"][];
                };
            };
        };
    };
    createSemesterSchedule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateSemesterScheduleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterScheduleResponse"];
                };
            };
        };
    };
    getRooms: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["RoomResponse"][];
                };
            };
        };
    };
    createRoom: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateRoomRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["RoomResponse"];
                };
            };
        };
    };
    getProgramPaths: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgramPathResponse"][];
                };
            };
        };
    };
    createProgramPath: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateProgramPathRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgramPathResponse"];
                };
            };
        };
    };
    getProfessors: {
        parameters: {
            query?: {
                query?: string;
                status?: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
                joinedFrom?: string;
                joinedTo?: string;
                academicDomainId?: string;
            };
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProfessorProfileResponse"][];
                };
            };
        };
    };
    createProfessor: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateProfessorRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CreateProfessorResponse"];
                };
            };
        };
    };
    getResponsibilities: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleClassResponsibilityResponse"][];
                };
            };
        };
    };
    createResponsibility: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateModuleClassResponsibilityRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleClassResponsibilityResponse"];
                };
            };
        };
    };
    getExamSchedules: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ExamScheduleResponse"][];
                };
            };
        };
    };
    createExamSchedule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateExamSchedule"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ExamScheduleResponse"];
                };
            };
        };
    };
    getDepartments: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DepartmentResponse"][];
                };
            };
        };
    };
    createDepartment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateDepartmentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DepartmentResponse"];
                };
            };
        };
    };
    getDegreeCycles: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DegreeCycleResponse"][];
                };
            };
        };
    };
    createDegreeCycle: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateDegreeCycleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DegreeCycleResponse"];
                };
            };
        };
    };
    getBlocks: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["BlockResponse"][];
                };
            };
        };
    };
    createBlock: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateBlockRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["BlockResponse"];
                };
            };
        };
    };
    getAcademicYears: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicYearResponse"][];
                };
            };
        };
    };
    createAcademicYear: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateAcademicYearRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicYearResponse"];
                };
            };
        };
    };
    getAcademicRuleProfiles: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRuleProfileResponse"][];
                };
            };
        };
    };
    createAcademicRuleProfile: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateAcademicRuleProfileRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRuleProfileResponse"];
                };
            };
        };
    };
    getAcademicRegistrations: {
        parameters: {
            query?: {
                academicYearId?: string;
                programFiliereId?: string;
                academicLevelId?: string;
                semesterId?: string;
                classGroupId?: string;
                status?: "ACTIVE" | "COMPLETED" | "CANCELLED" | "SUSPENDED";
                query?: string;
            };
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRegistrationResponse"][];
                };
            };
        };
    };
    createAcademicRegistration: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateAcademicRegistrationRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRegistrationResponse"];
                };
            };
        };
    };
    list: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRankResponse"][];
                };
            };
        };
    };
    create: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AcademicRankRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRankResponse"];
                };
            };
        };
    };
    getAcademicDomains: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicDomainResponse"][];
                };
            };
        };
    };
    createAcademicDomain: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateAcademicDomainRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicDomainResponse"];
                };
            };
        };
    };
    getProgramFilieres: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                departmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgramFiliereResponse"][];
                };
            };
        };
    };
    createProgramFiliere: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                departmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateProgramFiliereRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgramFiliereResponse"];
                };
            };
        };
    };
    refresh: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["RefreshRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AuthResponse"];
                };
            };
        };
    };
    logout: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["RefreshRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    login: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["LoginRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AuthResponse"];
                };
            };
        };
    };
    changePassword: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ChangePasswordRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    unlockAccount_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    restoreAccount_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    resetPassword_3: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ResetAdminPasswordRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    lockAccount_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    deactivateAccount_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    archiveAccount_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    activateAccount_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getSemesters: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterResponse"][];
                };
            };
        };
    };
    createSemester: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateSemesterRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterResponse"];
                };
            };
        };
    };
    getAssignments: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicLevelRuleAssignmentResponse"][];
                };
            };
        };
    };
    createAssignment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateAcademicLevelRuleAssignmentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicLevelRuleAssignmentResponse"];
                };
            };
        };
    };
    getClassGroups: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ClassGroupResponse"][];
                };
            };
        };
    };
    createClassGroup: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateClassGroupRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ClassGroupResponse"];
                };
            };
        };
    };
    generateClassGroups: {
        parameters: {
            query: {
                academicYearId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["GenerateClassGroupsRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ClassGroupGenerationResponse"];
                };
            };
        };
    };
    getProgramPath: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                programPathId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgramPathResponse"];
                };
            };
        };
    };
    deleteProgramPath: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                programPathId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    updateProgramPath: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                programPathId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateProgramPathRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgramPathResponse"];
                };
            };
        };
    };
    getProgramFiliere: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                programFiliereId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgramFiliereResponse"];
                };
            };
        };
    };
    deleteProgramFiliere: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                programFiliereId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    updateProgramFiliere: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                programFiliereId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateProgramFiliereRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgramFiliereResponse"];
                };
            };
        };
    };
    getDepartment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                departmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DepartmentResponse"];
                };
            };
        };
    };
    deleteDepartment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                departmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    updateDepartment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                departmentId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateDepartmentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DepartmentResponse"];
                };
            };
        };
    };
    getDegreeCycle: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                degreeCycleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DegreeCycleResponse"];
                };
            };
        };
    };
    deleteDegreeCycle: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                degreeCycleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    updateDegreeCycle: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                degreeCycleId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateDegreeCycleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DegreeCycleResponse"];
                };
            };
        };
    };
    getUniversity: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["UniversityResponse"];
                };
            };
        };
    };
    getEstablishments: {
        parameters: {
            query?: {
                query?: string;
                type?: "SCHOOL" | "FACULTY" | "INSTITUTE";
                status?: "ACTIVE" | "INACTIVE" | "ARCHIVED";
            };
            header?: never;
            path: {
                universityId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["EstablishmentResponse"][];
                };
            };
        };
    };
    getUniversityById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["UniversityResponse"];
                };
            };
        };
    };
    getTeachingAssignment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                teachingAssignmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingAssignmentResponse"];
                };
            };
        };
    };
    unassignProfessor: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                teachingAssignmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getTeachingAssignmentStudents: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                teachingAssignmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingAssignmentStudentResponse"][];
                };
            };
        };
    };
    getStudentGrades: {
        parameters: {
            query?: {
                academicYearId?: string;
                academicLevelId?: string;
                semesterId?: string;
            };
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["StudentGradeResponse"][];
                };
            };
        };
    };
    getStudentAcademicRegistrations: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                studentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicRegistrationResponse"][];
                };
            };
        };
    };
    getForSemester: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingRequirementResponse"][];
                };
            };
        };
    };
    getRoster: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingGroupRosterResponse"];
                };
            };
        };
    };
    getSemesterSchedule: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                scheduleId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterScheduleResponse"];
                };
            };
        };
    };
    getSemesterResult: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterRegistrationId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterResultResponse"];
                };
            };
        };
    };
    getModuleRegistrations: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterRegistrationId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleRegistrationResponse"][];
                };
            };
        };
    };
    getPermissionCatalog: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PermissionResponse"][];
                };
            };
        };
    };
    getCandidates: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                moduleExamId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ExamCandidateResponse"][];
                };
            };
        };
    };
    getResponsibility: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                responsibilityId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleClassResponsibilityResponse"];
                };
            };
        };
    };
    removeResponsibility: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                responsibilityId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
    getMyTeachingAssignments: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingAssignmentResponse"][];
                };
            };
        };
    };
    getMyResponsibilities: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ModuleClassResponsibilityResponse"][];
                };
            };
        };
    };
    getMyGrades: {
        parameters: {
            query?: {
                academicYearId?: string;
                academicLevelId?: string;
                semesterId?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["StudentGradeResponse"][];
                };
            };
        };
    };
    getMyInvitations: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ExamCandidateResponse"][];
                };
            };
        };
    };
    getMyAbsences: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AbsenceRecordResponse"][];
                };
            };
        };
    };
    list_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TeachingAssignmentRankPreferenceResponse"][];
                };
            };
        };
    };
    getSuperAdmins: {
        parameters: {
            query?: {
                query?: string;
                status?: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
            };
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SuperAdminProfileResponse"][];
                };
            };
        };
    };
    getEstablishmentAbsences: {
        parameters: {
            query?: {
                studentId?: string;
                academicYearId?: string;
                semesterId?: string;
                subjectModuleId?: string;
                justified?: boolean;
            };
            header?: never;
            path: {
                establishmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AbsenceRecordResponse"][];
                };
            };
        };
    };
    me: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CurrentUserResponse"];
                };
            };
        };
    };
    getSemesterRegistrations: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicRegistrationId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SemesterRegistrationResponse"][];
                };
            };
        };
    };
    getProgressionDecision: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                academicRegistrationId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProgressionDecisionResponse"];
                };
            };
        };
    };
    getClassGroupRoster: {
        parameters: {
            query: {
                academicYearId: string;
                semesterId: string;
            };
            header?: never;
            path: {
                academicLevelId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ClassGroupRosterResponse"];
                };
            };
        };
    };
    getAssignment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                assignmentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AcademicLevelRuleAssignmentResponse"];
                };
            };
        };
    };
    clearTeachingAssignments: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                semesterId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ActionResponse"];
                };
            };
        };
    };
}
