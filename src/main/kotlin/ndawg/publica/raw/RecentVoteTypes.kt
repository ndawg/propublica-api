package ndawg.publica.raw

import com.google.gson.annotations.SerializedName
import ndawg.publica.types.Chamber
import ndawg.publica.types.MajorityPosition

import java.time.LocalDate
import java.time.LocalTime

data class RawRecentVotes(
	val chamber: String,
	val offset: Long,
	val num_results: Long,
	val votes: List<RawVote>
)

data class RawVote(
	val congress: Long,
	val chamber: Chamber,
	val session: Long,
	val roll_call: Long,
	val source: String,
	val url: String,
	val vote_uri: String,
	val bill: RawBill? = null,
	val amendment: RawAmendment? = null,
	val nomination: RawNomination? = null,
	val question: String,
	val description: String,
	val vote_type: String,
	val date: LocalDate,
	val time: LocalTime,
	val result: String,
	val tie_breaker: String?,
	val tie_breaker_vote: String?,
//	val dem: PartyVote,
//	val rep: PartyVote,
//	val ind: PartyVote,
//	val tot: PartyVote,
	@SerializedName("democratic")
	val demRaw: Map<String, String>,
	@SerializedName("republican")
	val repRaw: Map<String, String>,
	@SerializedName("independent")
	val indRaw: Map<String, String>,
	@SerializedName("total")
	val totalRaw: Map<String, String>,
	val positions: List<RawVotePosition>?
)

data class RawBill(
	val bill_id: String? = null,
	val number: String? = null,
	val sponsor_id: String? = null,
	val api_uri: String? = null,
	val title: String? = null,
	val latest_action: String? = null
)

data class RawAmendment(
	val number: String? = null,
	val api_uri: String? = null,
	val sponsor_id: String? = null,
	val sponsor: String? = null,
	val sponsor_uri: String? = null,
	val sponsor_party: String? = null,
	val sponsor_state: String? = null
)

data class RawNomination(
	val nomination_id: String,
	val number: String,
	val name: String,
	val agency: String
)

data class PartyVote(
	val yes: Long,
	val no: Long,
	val present: Long,
	val not_voting: Long,
	val majority_position: MajorityPosition? = null
)
