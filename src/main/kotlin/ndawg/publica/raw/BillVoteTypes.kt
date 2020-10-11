package ndawg.publica.raw

import ndawg.publica.types.Party

data class RawVoteResults(val votes: RawVotes)

data class RawVotes(
	val vote: RawVote,
	val vacant_seats: List<RawVotePosition>
)

data class RawVotePosition(
	val member_id: String,
	val name: String,
	val party: Party?,
	val state: String,
	val vote_position: String?,
	val dw_nominate: Double? = null,
	val district: Long? = null
)
