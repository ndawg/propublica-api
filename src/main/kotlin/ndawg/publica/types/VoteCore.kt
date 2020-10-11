package ndawg.publica.types

import com.google.gson.annotations.SerializedName
import ndawg.publica.PublicaAPI
import ndawg.publica.getPositions
import ndawg.util.CitizenUtil
import ndawg.util.VoteGoal

import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Abstract root for a vote. This class contains no information about the result
 * or actual voting positions.
 */
// TODO split implementations into bill/nomination/amendment?
interface IVote {
	/** Versioning and metadata on this vote. */
	val meta: VoteMeta
	/** The bill meta this vote was taken on, if it was a vote on a bill (or amendment). */
	val bill: RecentBillVote?
	/** The nomination this vote was taken on, if it was a vote on a nomination. */
	val nomination: RecentNominationVote?
	/** The amendment this vote was taken on, if it was a vote on an amendment to a bill. */
	val amendment: RecentAmendmentVote?
	/** The question being asked, eg: "On Passage" */
	val question: String
	/** Usually indicates the subject that is being voted on, eg: "For the People Act" */
	val description: String
	/** The type of vote taken, which determines the margin required to pass. */
	val type: VoteType
	/** The day this vote took place. */
	val date: LocalDate
	/** The time this vote took place. */
	val time: LocalTime
	/** The categorized goal of this vote. */
	val goal: VoteGoal? get() = CitizenUtil.categorize(this.question, this.description)
	/** Convenience date/time combination. */
	val at: LocalDateTime get() = LocalDateTime.of(date, time)
	
	/** Loads the positions of this vote. */
	suspend fun positions() = meta.positions()
}

/**
 * Metadata about a specific roll call vote.
 */
data class VoteMeta(
	val congress: Long,
	val chamber: Chamber,
	val session: Long,
	val rollCall: Long,
	val source: String
) : KoinComponent {
	suspend fun positions() = get<PublicaAPI>().getPositions(this)
}

/**
 * The data attached to a vote when a bill is the subject of the vote.
 */
data class RecentBillVote(
	/** The metadata of the bill being voted on. */
	val meta: BillMeta,
	/** The string of the vote, which is not necessarily the same. */
	val title: String?
) {
	suspend fun details() = meta.details()
}

/**
 * The data attached to a vote when a nomination is the subject of the vote.
 */
data class RecentNominationVote(
	/** The unique identifier, which includes the congressional session. */
	val id: String,
	/** The 'number' which is an identifier within the congressional session. */
	val number: String,
	/** The name of the nominee. */
	val name: String,
	/** The agency the person is being nominated for. */
	val agency: String
)

/**
 * The data attached to a vote when a bill amendment is the subject of the vote.
 */
data class RecentAmendmentVote(
	/** The amendment identifier, eg H.AMDT.95 */
	val number: String,
	/** The sponsor of the amendment. */
	val sponsor: SponsorMeta
)

/**
 * The data attached to a vote when a tie break occurred.
 */
data class TieBreak(
	val breaker: String,
	val vote: VotePosition
)

/**
 * The categorization for the type of vote, which determines the majority required to pass.
 */
enum class VoteType(val required: Float) {
	@SerializedName("1/2")
	ONE_HALF(1F / 2),
	@SerializedName("YEA-AND-NAY")
	YEA_AND_NAY(1F / 2),
	@SerializedName("RECORDED VOTE")
	RECORDED(1F / 2),
	@SerializedName("2/3")
	TWO_THIRDS(2F / 3),
	@SerializedName("2/3 YAY-AND-NAY")
	TWO_THIRDS_YEA_AND_NAY(2F / 3),
	@SerializedName("2/3 RECORDED VOTE")
	TWO_THIRDS_RECORDED(2F / 3),
	@SerializedName("3/5")
	THREE_FIFTHS(3F / 5),
	// Special type of vote
	QUORUM(1F / 2);
	
	companion object {
		/**
		 * For some reason SerializedName doesn't like the slashes and such
		 */
		fun from(string: String): VoteType {
			return when (string.trim()) {
				"1/2" -> ONE_HALF
				"YEA-AND-NAY" -> YEA_AND_NAY
				"RECORDED VOTE" -> RECORDED
				"2/3" -> TWO_THIRDS
				"2/3 YEA-AND-NAY" -> TWO_THIRDS_YEA_AND_NAY
				"2/3 RECORDED VOTE" -> TWO_THIRDS_RECORDED
				"3/5" -> THREE_FIFTHS
				"QUORUM" -> QUORUM
				else -> throw IllegalArgumentException("Unknown VoteType $string given")
			}
		}
	}
}

typealias MajorityPosition = VotePosition
