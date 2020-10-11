package ndawg.publica.raw

import ndawg.publica.types.Chamber
import ndawg.publica.types.Party
import ndawg.publica.types.State
import java.time.LocalDate

data class ListMemberResult(
	val congress: Long,
	val chamber: String,
	val num_results: Int,
	val offset: Int,
	val members: List<RawMemberWithRole>
)

data class RawMember(
	val member_id: String,
	val first_name: String,
	val middle_name: String? = null,
	val last_name: String,
	val suffix: String? = null,
	val date_of_birth: String,
	val gender: String,
	val url: String,
	val times_topics_url: String,
	val times_tag: String,
	val govtrack_id: String,
	val cspan_id: String,
	val votesmart_id: String,
	val icpsr_id: String,
	val twitter_account: String?,
	val facebook_account: String?,
	val youtube_account: String?,
	val crp_id: String,
	val google_entity_id: String,
	val rss_url: String,
	val in_office: Boolean,
	val current_party: Party,
	val most_recent_vote: String?,
	val last_updated: String,
	val roles: List<RawMemberRole>
)

data class RawMemberRole(
	val congress: Long,
	val chamber: Chamber,
	val title: String,
	val short_title: String,
	val state: State,
	val party: Party,
	val leadership_role: String? = null,
	val fec_candidate_id: String? = null,
	val seniority: String,
	val senate_class: String? = null,
	val state_rank: String? = null,
	val lis_id: String? = null,
	val ocd_id: String,
	val start_date: String,
	val end_date: String,
	val office: String? = null,
	val phone: String? = null,
	val fax: String? = null,
	val contact_form: String? = null,
	val bills_sponsored: Long? = null,
	val bills_cosponsored: Long? = null,
	val missed_votes_pct: Float?,
	val votes_with_party_pct: Float?,
	val committees: List<RawCommitteeRole>,
	val subcommittees: List<RawCommitteeRole>,
	val district: String? = null,
	val at_large: Boolean? = null
)

data class RawMemberWithRole(
	val id: String,
	val first_name: String,
	val middle_name: String? = null,
	val last_name: String,
	val suffix: String? = null,
	val date_of_birth: String,
	val gender: String,
	val url: String,
	val times_topics_url: String,
	val times_tag: String,
	val govtrack_id: String,
	val cspan_id: String,
	val votesmart_id: String,
	val icpsr_id: String,
	val twitter_account: String?,
	val facebook_account: String?,
	val youtube_account: String?,
	val crp_id: String,
	val google_entity_id: String,
	val rss_url: String,
	val in_office: Boolean,
	val current_party: Party,
	val most_recent_vote: String?,
	val last_updated: String,
	val chamber: String,
	val title: String,
	val short_title: String,
	val state: State,
	val party: Party,
	val leadership_role: String? = null,
	val fec_candidate_id: String? = null,
	val seniority: String,
	val senate_class: String? = null,
	val state_rank: String? = null,
	val lis_id: String? = null,
	val ocd_id: String,
	val start_date: String,
	val end_date: String,
	val office: String? = null,
	val phone: String? = null,
	val fax: String? = null,
	val contact_form: String? = null,
	val missed_votes_pct: Float?,
	val votes_with_party_pct: Float?,
	val committees: List<RawCommitteeRole>,
	val subcommittees: List<RawCommitteeRole>,
	val district: String? = null,
	val at_large: Boolean? = null
)

data class RawBarebonesMember(
	val id: String,
	val api_uri: String,
	val first_name: String,
	val middle_name: String?,
	val last_name: String,
	val suffix: String?,
	val party: String,
	val state: String,
	val district: String,
	val begin_date: LocalDate,
	val end_date: LocalDate
)

data class RawCommitteeRole(
	val name: String,
	val code: String,
	val api_url: String,
	val side: String? = null,
	val title: String? = null,
	val rank_in_party: Long? = null,
	val begin_date: String,
	val end_date: String,
	val parent_committee_id: String? = null
)