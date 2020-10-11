package ndawg.publica.types

import com.google.gson.Gson
import ndawg.publica.raw.RawVote
import ndawg.publica.raw.RawVotePosition
import ndawg.util.from
import ndawg.util.get
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.time.LocalDate
import java.time.LocalTime

/**
 * Finds a Congressional member from the given vote's positions list, which comes in two formats:
 * 1. "LastName (ST)" - which includes the state when a name conflict exists
 * 2. "LastName" - which only includes the last name
 *
 * If no member can be found, a simple [INamedMember] is returned to represent the name.
 */
fun findOrName(vote: RawVote, string: String): INamedMember? {
	if (string == "Not Voting")
		return null
	
	requireNotNull(vote.positions) {
		"Attempted to find member $string in null positions list in $vote"
	}
	
	// string can either: "Ryan" or "Ryan (WI)" for example
	val pos = if (string.contains(Regex("\\(\\w\\w\\)"))) {
		val name = string.replace(Regex("(.*) \\((\\w\\w)\\)"), "$1")
		val state = string.replace(Regex("(.*) \\((\\w\\w)\\)"), "$2")
		vote.positions.find {
			it.name.split(" ").last() == name && it.state == state
		} ?: return SimpleNamedMember(string)
	} else {
		vote.positions.find {
			it.name.split(" ").last() == string
		} ?: return SimpleNamedMember(string)
	}
	
	return VotingMember.from(pos)
}

/**
 * Finds a Congressional member from the given vote's positions list, which comes in two formats:
 * 1. "LastName (ST)" - which includes the state when a name conflict exists
 * 2. "LastName" - which only includes the last name
 */
fun findMember(vote: RawVote, string: String): ICongressMember? {
	if (string == "Not Voting")
		return null
	
	requireNotNull(vote.positions) {
		"Attempted to find member $string in null positions list in $vote"
	}
	
	// string can either: "Ryan" or "Ryan (WI)" for example
	val pos = if (string.contains(Regex("\\(\\w\\w\\)"))) {
		val name = string.replace(Regex("(.*) \\((\\w\\w)\\)"), "$1")
		val state = string.replace(Regex("(.*) \\((\\w\\w)\\)"), "$2")
		vote.positions.find {
			it.name.split(" ").last() == name && it.state == state
		} ?: throw IllegalStateException("Unable to find member $string, name: $name, state: $state")
	} else {
		vote.positions.find {
			it.name.split(" ").last() == string
		} ?: throw IllegalStateException("Unable to find member $string")
	}
	
	return VotingMember.from(pos)
}

/**
 * Maps legislative vote positions from a [RawVote] to a Party based vote.
 */
private fun KoinComponent.mapLegislativeVotes(vote: RawVote): YeaNayVotes {
	val gson = get<Gson>()
	return YeaNayVotes(mapOf(
		Party.D to LegislativeVote.RecentVotePositions(
			mapOf(
				VotePosition.Yes to vote.demRaw.getValue("yes").toLong(),
				VotePosition.No to vote.demRaw.getValue("no").toLong(),
				VotePosition.Abstain to vote.demRaw.getValue("not_voting").toLong(),
				VotePosition.Present to vote.demRaw.getValue("present").toLong()
			),
			vote.demRaw["majority_position"]?.let { gson.from<MajorityPosition>(it) }
		),
		Party.R to LegislativeVote.RecentVotePositions(
			mapOf(
				VotePosition.Yes to vote.repRaw.getValue("yes").toLong(),
				VotePosition.No to vote.repRaw.getValue("no").toLong(),
				VotePosition.Abstain to vote.repRaw.getValue("not_voting").toLong(),
				VotePosition.Present to vote.repRaw.getValue("present").toLong()
			),
			vote.repRaw["majority_position"]?.let { gson.from<MajorityPosition>(it) }
		),
		Party.I to LegislativeVote.RecentVotePositions(
			mapOf(
				VotePosition.Yes to vote.indRaw.getValue("yes").toLong(),
				VotePosition.No to vote.indRaw.getValue("no").toLong(),
				VotePosition.Abstain to vote.indRaw.getValue("not_voting").toLong(),
				VotePosition.Present to vote.indRaw.getValue("present").toLong()
			),
			vote.indRaw["majority_position"]?.let { gson.from<MajorityPosition>(it) }
		),
		null to LegislativeVote.RecentVotePositions(
			mapOf(
				VotePosition.Yes to vote.totalRaw.getValue("yes").toLong(),
				VotePosition.No to vote.totalRaw.getValue("no").toLong(),
				VotePosition.Abstain to vote.totalRaw.getValue("not_voting").toLong(),
				VotePosition.Present to vote.totalRaw.getValue("present").toLong()
			),
			vote.totalRaw["majority_position"]?.let { gson.from<MajorityPosition>(it) }
		)
	), VoteType.from(vote.vote_type))
}

/**
 * A data container for a yes/no vote, used in legislative and motion votes.
 */
data class YeaNayVotes(
	val votes: Map<Party?, LegislativeVote.RecentVotePositions>,
	val type: VoteType
) : Map<Party?, LegislativeVote.RecentVotePositions> by votes {
	
	/**
	 * Retrieves the count of votes in the given party of the given position.
	 */
	operator fun get(party: Party?, position: VotePosition): Long {
		return votes.getValue(party)[position] ?: 0L
	}
	
	/**
	 * The total number of votes cast.
	 */
	val totalVotes: Long by lazy {
		votes.getValue(null)[VotePosition.Yes]!! +
			votes.getValue(null)[VotePosition.No]!! +
			votes.getValue(null)[VotePosition.Abstain]!!
	}
	
	/**
	 * Whether or not this vote passed, as in exceeded the threshold required.
	 */
	val passed: Boolean by lazy {
		votes.getValue(null)[VotePosition.Yes]!! >= totalVotes * type.required
	}
	
}

/**
 * A vote on a piece of legislation or nomination (pretty much every congressional vote except a vote for the speaker).
 * This class includes a map of a per-party breakdown in [votes]. A full list of positions is loaded via
 * [PublicaAPI.getPositions]. For this type of vote, a [LegislativeVotePositions] instance is returned.
 */
data class LegislativeVote(
	/** Versioning and metadata on this vote. */
	override val meta: VoteMeta,
	/** The bill meta this vote was taken on, if it was a vote on a bill (or amendment). */
	override val bill: RecentBillVote?,
	/** The nomination this vote was taken on, if it was a vote on a nomination. */
	override val nomination: RecentNominationVote?,
	/** The amendment this vote was taken on, if it was a vote on an amendment to a bill. */
	override val amendment: RecentAmendmentVote?,
	/** The question being asked, eg: "On Passage" */
	override val question: String,
	/** Usually indicates the subject that is being voted on, eg: "For the People Act" */
	override val description: String,
	/** The type of vote taken, which determines the margin required to pass. */
	override val type: VoteType,
	/** The day this vote took place. */
	override val date: LocalDate,
	/** The time this vote took place. */
	override val time: LocalTime,
	/** A string that represents the result of this vote, eg: "Passed", "Agreed to", etc. */
	val result: String,
	/** Used only in Senate votes, this is present when the vote was broken with a tie break vote. */
	val tieBreak: TieBreak?,
	/** A per-party breakdown of votes, including totals, which uses `null`. */
	val votes: YeaNayVotes
) : IVote {
	
	/**
	 * The total number of votes cast.
	 */
	val totalVotes: Long by lazy {
		votes.getValue(null)[VotePosition.Yes]!! +
			votes.getValue(null)[VotePosition.No]!! +
			votes.getValue(null)[VotePosition.Abstain]!!
	}
	
	/**
	 * Whether or not this vote passed, as in exceeded the threshold required.
	 */
	val passed: Boolean by lazy {
		votes.getValue(null)[VotePosition.Yes]!! >= totalVotes * type.required
	}
	
	// TODO maybe make a custom `votes` type for this
	operator fun get(party: Party?, position: VotePosition): Long {
		return votes.getValue(party)[position] ?: 0L
	}
	
	companion object : KoinComponent {
		fun from(vote: RawVote): LegislativeVote {
			val gson: Gson = get()
			return LegislativeVote(
				VoteMeta(
					vote.congress,
					vote.chamber,
					vote.session,
					vote.roll_call,
					vote.source
				),
				kotlin.run {
					if (vote.bill?.bill_id != null) {
						// The bill information is given as 'hres208-116' for example
						val re = Regex("([a-z]+)(\\d+)-(\\d+)")
						RecentBillVote(
							BillMeta(
								vote.bill.bill_id.replace(re, "$1$2"),
								vote.bill.bill_id.replace(re, "$3").toLong(),
								vote.bill.number!!,
								vote.bill.bill_id.replace(re, "$1"),
								vote.bill.bill_id
							),
							vote.bill.title?.ifBlank { null }
						)
					} else null
				},
				kotlin.run {
					if (vote.nomination?.nomination_id != null) {
						RecentNominationVote(
							vote.nomination.nomination_id,
							vote.nomination.number,
							vote.nomination.name,
							vote.nomination.agency
						)
					} else null
				},
				kotlin.run {
					if (vote.amendment?.number != null) {
						RecentAmendmentVote(
							vote.amendment.number,
							SponsorMeta(
								vote.amendment.sponsor_id!!,
								vote.amendment.sponsor!!,
								gson.from(vote.amendment.sponsor_party!!),
								requireNotNull(gson.from(vote.amendment.sponsor_state!!)) {
									"Unknown sponsor state: ${vote.amendment.sponsor_state}"
								},
								if (vote.chamber == Chamber.House) "Rep." else "Sen."
							)
						)
					} else null
				},
				vote.question,
				vote.description,
				VoteType.from(vote.vote_type),
				vote.date,
				vote.time,
				vote.result,
				// TODO test tiebreak
				vote.tie_breaker?.ifBlank { null }?.let {
					TieBreak(vote.tie_breaker, gson.from(vote.tie_breaker_vote!!))
				},
				mapLegislativeVotes(vote)
			)
		}
	}
	
	/**
	 * The data used to represent legislative vote positions.
	 */
	data class RecentVotePositions(
		val counts: Map<VotePosition, Long>,
		val majority: MajorityPosition?
	) {
		operator fun get(position: VotePosition) = counts[position]
	}
}

/**
 * A vote on the nomination of the Speaker of the House, which is a special type of congressional vote.
 * This class includes a map of a per-party breakdown in [votes]. A full list of positions is loaded via
 * [PublicaAPI.getPositions]. For this type of vote, a [SpeakerVotePositions] instance is returned.
 */
data class SpeakerVote(
	/** Versioning and metadata on this vote. */
	override val meta: VoteMeta,
	/** The bill meta this vote was taken on, if it was a vote on a bill (or amendment). */
	override val bill: RecentBillVote?,
	/** The nomination this vote was taken on, if it was a vote on a nomination. */
	override val nomination: RecentNominationVote?,
	/** The amendment this vote was taken on, if it was a vote on an amendment to a bill. */
	override val amendment: RecentAmendmentVote?,
	/** The question being asked, eg: "On Passage" */
	override val question: String,
	/** Usually indicates the subject that is being voted on, eg: "For the People Act" */
	override val description: String,
	/** The type of vote taken, which determines the margin required to pass. */
	override val type: VoteType,
	/** The day this vote took place. */
	override val date: LocalDate,
	/** The time this vote took place. */
	override val time: LocalTime,
	/** The name of the member that won the nomination. Use [SpeakerVotePositions] for a parsed type. */
	val result: String,
	/** A per-party breakdown of votes, including totals, which uses `null`. */
	val votes: Map<Party?, Map<String, Long>>
) : IVote {
	
	companion object : KoinComponent {
		fun from(vote: RawVote): SpeakerVote {
			val gson: Gson = get()
			
			return SpeakerVote(
				VoteMeta(
					vote.congress,
					vote.chamber,
					vote.session,
					vote.roll_call,
					vote.source
				),
				kotlin.run {
					if (vote.bill?.bill_id != null) {
						// The bill information is given as 'hres208-116' for example
						val re = Regex("([a-z]+)(\\d+)-(\\d+)")
						RecentBillVote(
							BillMeta(
								vote.bill.bill_id.replace(re, "$1$2"),
								vote.bill.bill_id.replace(re, "$3").toLong(),
								vote.bill.number!!,
								vote.bill.bill_id.replace(re, "$1"),
								vote.bill.bill_id
							),
							vote.bill.title?.ifBlank { null }
						)
					} else null
				},
				kotlin.run {
					if (vote.nomination?.nomination_id != null) {
						RecentNominationVote(
							vote.nomination.nomination_id,
							vote.nomination.number,
							vote.nomination.name,
							vote.nomination.agency
						)
					} else null
				},
				kotlin.run {
					if (vote.amendment?.number != null) {
						RecentAmendmentVote(
							vote.amendment.number,
							SponsorMeta(
								vote.amendment.sponsor_id!!,
								vote.amendment.sponsor!!,
								gson.from(vote.amendment.sponsor_party!!),
								gson.from(vote.amendment.sponsor_state!!),
								if (vote.chamber == Chamber.House) "Rep." else "Sen."
							)
						)
					} else null
				},
				vote.question,
				vote.description,
				VoteType.from(vote.vote_type),
				vote.date,
				vote.time,
				vote.result,
				mapOf(
					Party.D to vote.demRaw.mapValues { it.value.toLong() },
					Party.R to vote.repRaw.mapValues { it.value.toLong() },
					Party.I to vote.indRaw.mapValues { it.value.toLong() },
					null to vote.totalRaw.mapValues { it.value.toLong() }
				)
			)
		}
	}
}

/**
 * The House occasionally votes on simpler motions, like adjourning, and the journal. Although similar to a
 * legislative vote, there is no bill to represent this information.
 */
data class MotionVote(
	override val meta: VoteMeta,
	override val bill: RecentBillVote? = null,
	override val nomination: RecentNominationVote? = null,
	override val amendment: RecentAmendmentVote? = null,
	override val question: String,
	override val description: String,
	override val type: VoteType,
	override val date: LocalDate,
	override val time: LocalTime,
	val motion: Type,
	/** A string that represents the result of this vote, eg: "Passed", "Agreed to", etc. */
	val result: String,
	/** A per-party breakdown of votes, including totals, which uses `null`. */
	val votes: YeaNayVotes
) : IVote {
	companion object : KoinComponent {
		
		fun isMotion(vote: RawVote): Boolean {
			if (vote.bill == null) return false
			if (vote.bill.number == null) return false
			return Type.values().any { vote.bill.number.toLowerCase().startsWith(it.name.toLowerCase()) }
		}
		
		fun from(vote: RawVote): MotionVote {
			requireNotNull(vote.bill) {
				"Motion votes must have a bill field attached."
			}
			requireNotNull(vote.bill.number) {
				"Motion votes must have a bill number field attached."
			}
			return MotionVote(
				VoteMeta(
					vote.congress,
					vote.chamber,
					vote.session,
					vote.roll_call,
					vote.source
				),
				null,
				null,
				null,
				vote.question,
				"House Journal", // TODO is it always a journal vote?
				VoteType.from(vote.vote_type),
				vote.date,
				vote.time,
				when (vote.bill.number.toLowerCase()) {
					"adjourn" -> Type.ADJOURN
					"journal" -> Type.JOURNAL
					"quorum" -> Type.QUORUM
					"motion" -> Type.MOTION
					"treatydoc" -> Type.TREATY
					else -> {
						if (vote.bill.number.startsWith("TreatyDoc") || vote.bill.number.startsWith("Treaty.Doc"))
							Type.TREATY
						else
							throw IllegalStateException("Unknown motion type: ${vote.bill.number}")
					}
				},
				vote.result,
				mapLegislativeVotes(vote)
			)
		}
	}
	
	enum class Type {
		ADJOURN,
		JOURNAL,
		QUORUM,
		MOTION,
		TREATY
	}
}

/**
 * Abstraction root for vote breakdowns.
 */
// TODO rename to IVoteWithPositions for more clarity?
interface IVotePositions<V : IVote> {
	val topic: V
}

/**
 * Contains the data for a [LegislativeVote], which is a map of each possible vote position to
 * every member of congress who voted for it. All legislative votes use a simple Yay/Nay system,
 * though members occasionally cast symbolic "Present" votes as well, or abstain for other reasons.
 */
data class LegislativeVotePositions(
	override val topic: LegislativeVote,
	val positions: Map<VotePosition, List<VotingMember>>
) : IVotePositions<LegislativeVote> {
	
	val byMember: Map<VotingMember, VotePosition>
		get() {
			val map = mutableMapOf<VotingMember, VotePosition>()
			positions.forEach { t, u ->
				u.forEach { pos ->
					map[pos] = t
				}
			}
			return map
		}
	
	companion object : KoinComponent {
		fun from(full: RawVote): LegislativeVotePositions {
			requireNotNull(full.positions) {
				"No positions were passed from the full vote."
			}
			val gson = get<Gson>()
			return LegislativeVotePositions(
				LegislativeVote.from(full),
				VotePosition.values().associate { pos ->
					pos to full.positions.filter {
						val p = gson.get<VotePosition?>(it.vote_position!!.replace(" ", "_"))
						p != null && p == pos
					}.map {
						requireNotNull(it.party)
						VotingMember(
							it.member_id,
							it.name,
							it.party,
							gson.from(it.state),
							it.district,
							it.dw_nominate
						)
					}
				}
			)
		}
	}
}

/**
 * Contains the data for a [SpeakerVote], which is a map of each possible speaker nominee
 * to every member of congress who voted for them.
 */
data class SpeakerVotePositions(
	override val topic: SpeakerVote,
	/** The winner of the nomation. */
	val winner: ICongressMember,
	/** A map of each nominated member, and the list of members who voted for them. */
	val positions: Map<INamedMember, List<ICongressMember>>
) : IVotePositions<SpeakerVote> {
	companion object : KoinComponent {
		fun from(full: RawVote): SpeakerVotePositions {
			// Collect all members who were voted on.
			val votedOn = full.totalRaw.keys.mapNotNull { findOrName(full, it) }
			
			return SpeakerVotePositions(
				SpeakerVote.from(full),
				findMember(full, full.result)!!,
				// Collect all members who voted on each nominee.
				votedOn.associate { nominee ->
					nominee to full.positions!!.filter { findOrName(full, it.vote_position!!) != null }.filter { pos ->
						findOrName(full, pos.vote_position!!)!!.name == nominee.name
					}.map { VotingMember.from(it) }
				}
			)
		}
	}
}

/**
 * Used to represent a member who has cast a vote.
 */
data class VotingMember(
	override val id: String,
	override val name: String,
	override val party: Party,
	override val state: State,
	val district: Long?,
	val dwNominate: Double?
) : ICongressMember {
	companion object : KoinComponent {
		fun from(position: RawVotePosition): VotingMember {
			return VotingMember(
				position.member_id,
				position.name,
				position.party!!,
				get<Gson>().from(position.state),
				position.district,
				position.dw_nominate
			)
		}
	}
}
