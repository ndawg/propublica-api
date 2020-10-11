package ndawg.publica.types

import ndawg.publica.PublicaAPI
import ndawg.publica.getDetails
import ndawg.publica.raw.RawBillDetails
import ndawg.publica.raw.RawBillWithCosponsors
import ndawg.publica.raw.RawUpcomingBill
import ndawg.util.CitizenUtil
import ndawg.util.VoteGoal
import ndawg.util.from
import com.google.gson.Gson
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.time.LocalDate
import java.time.LocalTime

/**
 * In-depth details about a bill. This is more detail than the "recent" overview of a bill.
 */
data class BillDetails(
	/** Versioning and metadata about the bill. */
	val meta: BillMeta,
	/** The "title" of the bill, which is usually more of a paragraph of information. */
	val title: String,
	/** The "short title" of the bill, which is essentially a nickname. */
	val shortTitle: String,
	/** The sponsor of this bill, who is responsible for introduction. */
	val sponsor: SponsorMeta,
	/** Various informative links related to this bill */
	val links: BillLinks,
	/** Contains various dates related to the bill, like introduction. */
	val dates: BillDates,
	/** Whether the bill is still within congress. */
	val active: Boolean,
	/** The number of cosponsors by party. */
	val cosponsors: Map<Party, Long>,
	/** Number of cosponsors who have withdrawn support. */
	val withdrawnCosponors: Long,
	/** The primary subject of this bill, like 'Government Operations and Politics' */
	val subject: String,
	/** The name of the primary (?) committee for this bill. */
	val committee: String,
	/** A set of all committee codes that this bill has been referred to. */
	val committees: List<String>,
	/** A set of all subcommittee codes that this bill has been referred to. */
	val subcommittees: List<String>,
	/** The full summary of this bill, sometimes provided. */
	val summary: String?,
	/** The short summary of this bill, which is usually best to display. */
	val shortSummary: String?,
	/** Various versions of this bill throughout the congressional process. */
	val versions: List<RawBillDetails.RawVersion>,
	/** Various actions taken on this bill, like votes, amendments, etc. */
	val actions: List<RawBillDetails.RawAction>,
	/** A list of all the votes taken on this bill that had roll calls. Voice votes are not included. */
	val votes: List<BillVoteOverview>
) {
	val totalCosponors = cosponsors.values.sum()
	
	/**
	 * Creates a list of [BillDecision] instances in an attempt to encapsulate each step of this
	 * bill's history through the government. It uses [CitizenUtil.toDecision].
	 */
	val history: List<BillDecision>
		get() = actions.mapNotNull { CitizenUtil.toDecision(this, it) }
	
	/**
	 * Retrieves an action by ID, which is relative to this bill (ie 0 is the first action).
	 */
	fun action(id: Long) = actions.find { it.id == id }
	
	companion object : KoinComponent {
		/**
		 * Converts the raw result from the ProPublica API to the more human friendly result.
		 */
		fun from(full: RawBillDetails): BillDetails {
			val gson: Gson = get()
			return BillDetails(
				BillMeta(
					full.bill_slug ?: full.bill_id.split("-").first(),
					// TODO temporary patch for the getRecentBills endpoint https://github.com/propublica/congress-api-docs/issues/241
					@Suppress("USELESS_ELVIS")
					(full.congress ?: full.bill_id.split("-").last()).toLongOrNull() ?: -1,
					full.bill ?: full.number,
					full.bill_type,
					full.bill_id
				),
				full.title,
				full.short_title ?: full.title,
				SponsorMeta(
					full.sponsor_id,
					full.sponsor,
					gson.from(full.sponsor_party),
					gson.from(full.sponsor_state),
					full.sponsor_title
				),
				BillLinks(full.gpo_pdf_uri, full.congressdotgov_url, full.govtrack_url),
				BillDates(
					full.introduced_date,
					full.last_vote,
					full.house_passage,
					full.senate_passage,
					full.enacted,
					full.vetoed
				),
				full.active,
				mapOf(
					Party.D to full.cosponsors_by_party.D,
					Party.R to full.cosponsors_by_party.R,
					Party.I to full.cosponsors_by_party.I
				),
				full.withdrawn_cosponsors,
				full.primary_subject,
				full.committees,
				full.committee_codes ?: emptyList(),
				full.subcomittee_codes ?: emptyList(),
				full.summary,
				full.summary_short,
				full.versions ?: emptyList(),
				full.actions ?: emptyList(),
				full.votes?.map {
					BillVoteOverview(
						it.chamber,
						it.date,
						it.time,
						it.roll_call,
						it.question,
						it.result,
						mapOf(
							VotePosition.Yes to it.total_yes,
							VotePosition.No to it.total_no,
							VotePosition.Abstain to it.total_not_voting
						)
					)
				} ?: emptyList()
			)
		}
	}
}

/**
 * Convenient metadata about a bill, including the type, number, congressional session, etc.
 */
data class BillMeta(
	/** eg: s756 */
	val slug: String,
	/** eg: 115 */
	val congress: Long,
	/** eg: S.756 - NOT the name of the Bill */
	val name: String,
	/** eg: s, h, hr, sr, journal etc. */
	val type: String, // TODO enum?
	/** eg: s756-115 */
	val id: String
) : KoinComponent {
	suspend fun details() = get<PublicaAPI>().getDetails(this)
}

data class BillLinks(
	/** The full bill text. */
	val gpo: String?,
	/** The congress.gov link. */
	val congress: String?,
	/** The govtrack.us link. */
	val govtrack: String?
)

data class BillDates(
	/** The day this bill was introduced to congress. */
	val introduced: LocalDate,
	/** The day this bill was last voted on. */
	val lastVote: LocalDate?,
	/** The day this bill passed the house. */
	val housePassage: LocalDate?,
	/** The day this bill passed the senate. */
	val senatePassage: LocalDate?,
	/** The day this bill was enacted into law. */
	val enacted: LocalDate?,
	/** The day this bill was vetoed. */
	val vetoed: LocalDate?
)

data class SponsorMeta(
	/** Congressional ID */
	override val id: String,
	/** Full name of the member */
	override val name: String,
	/** Political party */
	override val party: Party,
	/** State the member represents */
	override val state: State,
	/** either Sen. or Rep. */
	val title: String
) : ICongressMember {
	
	companion object: KoinComponent {
		fun from(raw: RawBillWithCosponsors.RawCosponsor): SponsorMeta {
			val gson = get<Gson>()
			return SponsorMeta(
				raw.cosponsor_id,
				raw.name,
				gson.from(raw.cosponsor_party),
				gson.from(raw.cosponsor_state),
				raw.cosponsor_title
			)
		}
	}
	
}

data class BillVoteOverview(
	/** The chamber that the vote took place in. */
	val chamber: Chamber,
	/** The date the vote took place. */
	val date: LocalDate,
	/** The time the vote took place. */
	val time: LocalTime,
	/** The roll call ID, which can be used to lookup more details. */
	val rollCall: Long,
	/** The question put forth, eg: "On Passage", "On Agreeing to the Amendment" etc. */
	val question: String,
	/** The result of the vote, eg: "Agreed to", "Passed", "Failed", etc. */
	val result: String,
	/** An overview of which positions had certain votes. */
	val votes: Map<VotePosition, Long>
) {
	/** The categorized goal of the vote. */
	val goal: VoteGoal? = CitizenUtil.categorize(this.question)
}

/**
 * Represents an upcoming bill that is scheduled to be voted on.
 */
// TODO determine values for "range" and add a convenience for start/end windows
data class UpcomingBill(
	/** Metadata of the upcoming bill. */
	val meta: BillMeta,
	/** The chamber the vote will be in. */
	val chamber: Chamber,
	/** The day given by the schedule. Usually this is within a week of when it will be voted on. */
	val day: LocalDate,
	/** Usually the best title available for the bill. */
	val description: String,
	/** The range of the date, usually "week" */
	val range: String
) {
	companion object : KoinComponent {
		fun from(bill: RawUpcomingBill): UpcomingBill {
			val gson = get<Gson>()
			return UpcomingBill(
				BillMeta(
					bill.bill_slug,
					bill.congress,
					bill.bill_number,
					bill.bill_type,
					bill.bill_id
				),
				gson.from(bill.chamber),
				bill.legislative_day,
				bill.description,
				bill.range
			)
		}
	}
}

/**
 * Represents a single decision taken on a bill by a chamber of congress. This type is used when analyzing a bill's
 * history, where not every vote is queried.
 */
data class BillDecision(
	val chamber: Chamber,
	val date: LocalDate,
	val description: String,
	val type: String,
	val vote: BillVote
)

/**
 * A simple overview of a vote on a bill. This type is used when analyzing a bill's history, where not
 * every vote is queried.
 */
interface BillVote {
	val voice: Boolean
	val result: VoteResult
	val overview: BillVoteOverview?
}

/**
 * A classification for the status that a bill has reached in congress. This is used by some calls in the
 * ProPublica API, such as getRecentBills.
 */
enum class BillStatus {
	/** The bill has been introduced into congress, but no other actions have been taken. */
	INTRODUCED,
	/** The bill has been introduced and may have been modified since introduction, but it is not up for consideration. */
	UPDATED,
	/** The bill has seen action beyond introduction and committee referral (like discussion on the floor). */
	ACTIVE,
	/** The bill has successfully passed both chambers of Congress. */
	PASSED,
	/** The bill has been enacted successfully. */
	ENACTED,
	/** The bill was passed but vetoed by the President. */
	VETOED;
}

/**
 * A subject for a bill, as determined by congress. More information:
 * - Finding Bills by Subject: https://www.congress.gov/help/faq/find-bills-by-subject
 * - Different Values: https://www.congress.gov/help/field-values/legislative-subject-terms
 */
data class BillSubject(
	/** The human-friendly name to describe this subject. */
	val name: String,
	/** The slug used for searching this subject. */
	val slug: String
) {
	companion object {
		fun from(raw: RawBillDetails.RawBillSubject) = BillSubject(raw.name, raw.url_name)
	}
}