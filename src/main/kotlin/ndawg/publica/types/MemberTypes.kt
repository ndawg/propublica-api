package ndawg.publica.types

import com.google.gson.Gson
import ndawg.publica.raw.RawMember
import ndawg.util.from
import ndawg.util.get

import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.time.LocalDate

/**
 * A member of Congress. This represents current as well as previous members of congress, which can
 * be differentiated based on the [inOffice] field. This class is derived from the 'Get a Specific Member'
 * query of ProPublica.
 */
data class CongressMember(
	/** Congressional ID. */
	val id: String,
	/** First name of the member. Use [name] for full name. */
	val firstName: String,
	/** Middle name of the member. Use [name] for full name. */
	val middleName: String?,
	/** Last name of the member. Use [name] for full name. */
	val lastName: String,
	/** Optional suffix for the member. */
	val suffix: String?,
	/** Date of birth. */
	val dob: LocalDate,
	/** The gender of the member, either "F" or "M". */
	val gender: String,
	/** The link to the member's congressional website. */
	val website: String?,
	/** The member's IDs on various third parties. */
	val ids: MemberIDs,
	/** The member's various social media handles. */
	val accounts: MemberSocialMedia,
	/** Particular NYT data about the member. */
	val nyt: MemberNYTData,
	/** Whether or not the member is currently serving. */
	val inOffice: Boolean,
	/** The roles this member has had, which is basically every tenure. */
	val roles: List<MemberRole>
) {
	
	/** The easy to use name of this member, including middle name. */
	val name: String = listOf(firstName, middleName, lastName, suffix).filterNotNull().joinToString(" ")
	
	/** The current role of this member, if they are still in office. */
	val currentRole: MemberRole? get() = if (inOffice) roles.first() else null
	
	companion object : KoinComponent {
		fun from(full: RawMember): CongressMember {
			val gson = get<Gson>()
			return CongressMember(
				full.member_id,
				full.first_name,
				full.middle_name,
				full.last_name,
				full.suffix,
				gson.from(full.date_of_birth),
				full.gender,
				full.url,
				MemberIDs(
					full.govtrack_id,
					full.cspan_id,
					full.votesmart_id,
					full.icpsr_id,
					full.crp_id,
					full.google_entity_id
				),
				MemberSocialMedia(
					full.twitter_account,
					full.facebook_account,
					full.youtube_account
				),
				MemberNYTData(
					full.times_topics_url,
					full.times_tag
				),
				full.in_office,
				full.roles.map {
					MemberRole(
						it.congress,
						it.chamber,
						it.state,
						it.party,
						it.district,
						it.title,
						it.short_title,
						it.leadership_role,
						it.seniority.toLong(),
						it.senate_class?.toInt(),
						it.state_rank,
						gson.from(it.start_date),
						gson.from(it.end_date),
						MemberRoleIDs(
							it.fec_candidate_id,
							it.lis_id,
							it.ocd_id
						),
						MemberRoleContact(
							it.office,
							it.phone,
							it.fax,
							it.contact_form
						),
						MemberRoleStatistics(
							it.bills_sponsored,
							it.bills_cosponsored,
							it.missed_votes_pct,
							it.votes_with_party_pct
						),
						it.committees.map { c ->
							MemberRoleCommittee(
								c.name,
								c.code,
								c.side,
								c.title,
								c.rank_in_party,
								gson.get(c.begin_date),
								gson.from(c.end_date),
								null
							) to it.subcommittees.filter { it.parent_committee_id == c.code }.map { s ->
								MemberRoleCommittee(
									s.name,
									s.code,
									s.side,
									s.title,
									s.rank_in_party,
									gson.get(s.begin_date),
									gson.from(s.end_date),
									s.parent_committee_id!!
								)
							}
						}.toMap()
					)
				}
			)
		}
	}
}

data class MemberNYTData(
	/** A URL to NYT times topics featuring the member. */
	val topics: String,
	/** The tag of the member on the NYT. */
	val tag: String
)

/**
 * The member's IDs on various third parties.
 */
data class MemberIDs(
	val govtrack: String,
	val cspan: String,
	val votesmart: String,
	val icpsr: String,
	val crp: String,
	val google: String
)

/**
 * The member's various social media handles.
 */
// TODO probably make this into a Map.
data class MemberSocialMedia(
	val twitter: String?,
	val facebook: String?,
	val youtube: String?
)

/**
 * Encapsulates a single tenure or session of a member's position, for example each House or Senate tenure.
 */
data class MemberRole(
	/** The congress served in. */
	val congress: Long,
	/** The chamber this role was in. */
	val chamber: Chamber,
	/** The state being served during this role. */
	val state: State,
	/** The party alignment during this role. */
	val party: Party,
	/** For house members, the district they represent. This could either be a number or 'At-Large' */
	val district: String?,
	/** A fancy position title, like 'Senator, 1st Class' */
	val title: String,
	/** A simpler position title, like 'Sen.' */
	val shortTitle: String,
	/** A leadership role held by the member, like 'Speaker of the House' */
	val role: String?,
	/** The seniority rank of this member within their own party. */
	val seniority: Long,
	/** For senators, this class represents the cycle from which elections are decided. */
	val senateClass: Int?,
	/** For senators, the rank relative to the other member's seniority, either junior or senior. */
	val stateRank: String?,
	/** The day this role started. */
	val start: LocalDate,
	/** The day this role ended. */
	val end: LocalDate,
	/** Various IDs related to the campaign for the role. */
	val ids: MemberRoleIDs,
	/** Various ways to contact/find the representative. */
	val contact: MemberRoleContact,
	/** Various statistics on bills and voting. */
	val stats: MemberRoleStatistics,
	/** A list of all the committees this member served on, paired with any subcommittees. */
	val committees: Map<MemberRoleCommittee, List<MemberRoleCommittee>>
)

/**
 * Various IDs related to the campaign for the role.
 */
data class MemberRoleIDs(
	/** Committee ID. */
	val fec: String?,
	/** ??? */
	val lis: String?,
	/** Open Civic Data ID. */
	val ocd: String?
)

/**
 * Various official ways to contact the representative.
 */
data class MemberRoleContact(
	/** The member's office location. */
	val office: String?,
	/** Normal phone number. */
	val phone: String?,
	/** Fax number. */
	val fax: String?,
	/** URL that can be used to contact. */
	val contact: String?
)

data class MemberRoleStatistics(
	/** The number of bills sponsored. */
	val billsSponsored: Long?,
	/** The number of bills cosposnored. */
	val billsCosposnored: Long?,
	/** The percentage of votes missed, eg 12.24 */
	val missedVotes: Float?,
	/** The percentage of votes aligned with other members of the party. */
	val votesWithParty: Float?
)

data class MemberRoleCommittee(
	/** The name of the committee. */
	val name: String,
	/** The code of the committee, eg 'SSEG' */
	val code: String,
	/** Which side the member is on, either minority or majority. */
	val side: String?,
	/** The title of the member in relation to the committee, like 'Member' */
	val title: String?,
	/** How senior this member is in party hierarchy. */
	val partyRank: Long?,
	/** When this committee position began. */
	// TODO why is this sometimes null
	val start: LocalDate?,
	/** When this committee position ended. */
	val end: LocalDate,
	/** The ID of the parent committee, if this is a subcommittee. */
	// maybe link this straight to the parent committee?
	val parent: String?
)