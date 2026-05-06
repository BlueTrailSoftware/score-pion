package org.example.notifier.infrastructure.persistence

object DynamoKeyPatterns {

    // Position GSI1 patterns for listing all positions
    const val ALL_POSITIONS_GSI1_PK = "POSITIONS"

    // User patterns
    fun userPk(userId: String) = "USER#$userId"
    fun userSk() = "PROFILE"
    fun userPkPrefix() = "USER#"

    // User role index patterns (for querying users by role)
    fun userRoleIndexPk(role: String) = "ROLE#$role"
    fun userRoleIndexSk(userId: String) = "USER#$userId"
    const val ALL_RECRUITERS_GSI1_PK = "ROLE#RECRUITER"
    const val ALL_ADMINS_GSI1_PK = "ROLE#ADMIN"
    const val ALL_RECRUITER_INVITATIONS_GSI2_PK = "RECRUITER_INVITATIONS"

    // Email GSI1 patterns (shared by User, Invitation, RecruiterInvitation)
    fun emailGsi1Pk(email: String) = "EMAIL#$email"
    fun userGsi1Sk() = "USER"
    fun invitationGsi1Sk() = "INVITATION"
    fun recruiterInvGsi1Sk() = "RECRUITER_INV"

    // GoogleId GSI2 patterns
    fun googleIdGsi2Pk(googleId: String) = "GOOGLEID#$googleId"
    fun userGsi2Sk() = "USER"

    // Position patterns
    fun positionPk(positionId: String) = "POSITION#$positionId"
    fun positionSk() = "METADATA"
    fun positionPkPrefix() = "POSITION#"

    // Position Assessment patterns
    fun assessmentSk(assessmentId: String) = "ASSESSMENT#$assessmentId"
    fun assessmentSkPrefix() = "ASSESSMENT#"

    // Position Recruiter Access patterns
    fun recruiterSk(recruiterId: String) = "RECRUITER#$recruiterId"
    fun recruiterSkPrefix() = "RECRUITER#"
    fun recruiterGsi1Pk(recruiterId: String) = "RECRUITER#$recruiterId"

    // Invitation patterns
    fun invitationPk(invitationId: String) = "INVITATION#$invitationId"
    fun invitationSk() = "METADATA"
    fun invitationPkPrefix() = "INVITATION#"
    fun candidateAssessmentGsi1Pk(candidateEmail: String, assessmentId: String) =
        "CANDIDATE#$candidateEmail#ASSESSMENT#$assessmentId"
    const val ALL_INVITATIONS_GSI2_PK = "ALL_INVITATIONS"

    // Recruiter Invitation patterns
    fun recruiterInvPk(invitationId: String) = "RECRUITER_INV#$invitationId"
    fun recruiterInvSk() = "METADATA"
    fun recruiterInvPkPrefix() = "RECRUITER_INV#"
    fun recruiterInvGsi2Pk() = ALL_RECRUITER_INVITATIONS_GSI2_PK
    fun recruiterInvGsi2Sk(invitationId: String) = invitationId

    // Global Recipients patterns
    fun globalRecipientsPk(): String = "GLOBAL_RECIPIENTS"
    fun globalRecipientsSk(): String = "METADATA"

    // Applicant patterns
    fun applicantPk(applicantId: String) = "APPLICANT#$applicantId"
    fun applicantSk() = "METADATA"
    fun applicantPkPrefix() = "APPLICANT#"
    fun applicantPositionGsi1Pk(positionId: String) = "POSITION#$positionId"
    fun applicantPositionGsi1Sk(applicantId: String) = "APPLICANT#$applicantId"
    fun applicantGsi2Pk() = "ALL_APPLICANTS"
    fun applicantGsi2Sk(email: String) = email.lowercase()
}
