package dev.kuro9.domain.member.auth.enumurate

abstract class MemberAuthority {
    abstract val serviceName: String
    abstract val authorityName: String
   
    override fun toString(): String = "AUTHORITY_${serviceName.uppercase()}_${authorityName.uppercase()}"
}