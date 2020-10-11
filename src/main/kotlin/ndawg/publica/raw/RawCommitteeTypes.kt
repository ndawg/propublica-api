package ndawg.publica.raw

import java.time.LocalDate

data class RawCommitteeResponse(
	val congress: String,
	val chamber: String,
	val num_results: Int,
	val committees: List<RawCommittee>
)

data class RawCommittee(
	val id: String,
	val name: String,
	val chamber: String,
	val url: String,
	val chair: String?,
	val chair_id: String?,
	val chair_party: String?,
	val chair_url: String?,
	val ranking_member_id: String?,
	val subcommittees: List<RawSubcommitteeId>,
	val current_members: List<RawCommitteeMember>?,
	val former_members: List<RawCommitteeMember>?
)

data class RawSubcommitteeId(
	val id: String,
	val name: String
)

data class RawSubcommittee(
	val id: String,
	val name: String,
	val chamber: String,
	val chair: String?,
	val chair_id: String?,
	val chair_party: String?,
	val chair_url: String?,
	val ranking_member_id: String?,
	val current_members: List<RawCommitteeMember>?,
	val committee_id: String,
	val committee_name: String,
	val committee_url: String
)

data class RawCommitteeMember(
	val id: String,
	val name: String,
	val party: String,
	val chamber: String,
	val side: String,
	val state: String,
	val begin_date: LocalDate?,
	val note: String?,
	val end_date: LocalDate?
)