ALTER TABLE academic_rule_profile
    ADD COLUMN minimum_individually_validated_modules_per_semester integer NOT NULL DEFAULT 0;

ALTER TABLE academic_rule_profile
    ADD COLUMN maximum_non_validated_modules_per_semester integer NOT NULL DEFAULT 999;

ALTER TABLE academic_rule_profile
    ADD COLUMN allow_inter_semester_compensation boolean NOT NULL DEFAULT false;

ALTER TABLE academic_rule_profile
    ADD COLUMN minimum_individually_validated_modules_per_academic_level integer NOT NULL DEFAULT 0;

ALTER TABLE academic_rule_profile
    ADD COLUMN rule_definition json;

ALTER TABLE academic_rule_profile
    ADD CONSTRAINT ck_rule_min_validated_semester_non_negative
        CHECK (minimum_individually_validated_modules_per_semester >= 0);

ALTER TABLE academic_rule_profile
    ADD CONSTRAINT ck_rule_max_non_validated_semester_non_negative
        CHECK (maximum_non_validated_modules_per_semester >= 0);

ALTER TABLE academic_rule_profile
    ADD CONSTRAINT ck_rule_min_validated_level_non_negative
        CHECK (minimum_individually_validated_modules_per_academic_level >= 0);

ALTER TABLE academic_rule_profile
    ADD CONSTRAINT ck_rule_inter_semester_compensation
        CHECK (
            allow_inter_semester_compensation
            OR minimum_individually_validated_modules_per_academic_level = 0
        );
