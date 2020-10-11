package ndawg.publica.raw

import ndawg.publica.types.Chamber
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalTime

data class RawBillDetails(
	val bill_id: String,
	val bill_slug: String? = bill_id.split("-").first(),
	val congress: String,
	val bill: String?,
	val bill_type: String,
	val number: String,
	val bill_uri: String,
	val title: String,
	val short_title: String?,
	val sponsor_title: String,
	@SerializedName("sponsor", alternate = ["sponsor_name"])
	val sponsor: String,
	val sponsor_id: String,
	val sponsor_uri: String,
	val sponsor_party: String,
	val sponsor_state: String,
	val gpo_pdf_uri: String? = null,
	val congressdotgov_url: String,
	val govtrack_url: String,
	val introduced_date: LocalDate,
	val active: Boolean,
	val last_vote: LocalDate?,
	val house_passage: LocalDate? = null,
	val senate_passage: LocalDate? = null,
	val enacted: LocalDate? = null,
	val vetoed: LocalDate? = null,
	val cosponsors: Long,
	val cosponsors_by_party: RawCosponsorsByParty,
	val withdrawn_cosponsors: Long,
	val primary_subject: String,
	val committees: String,
	val committee_codes: List<String>?,
	val subcomittee_codes: List<String>?,
	val latest_major_action_date: LocalDate,
	val latest_major_action: String,
	val house_passage_vote: LocalDate? = null,
	val senate_passage_vote: LocalDate? = null,
	val summary: String,
	val summary_short: String,
	val versions: List<RawVersion>?,
	val actions: List<RawAction>?,
	val votes: List<RawVoteData>?,
	val subjects: List<RawBillSubject>?,
	val related_bills: List<RawBillDetails>?
) {
	
	data class RawAction(
		val id: Long,
		val chamber: Chamber,
		val action_type: String,
		val datetime: LocalDate,
		val description: String
	)
	
	data class RawVersion(
		val status: String?,
		val title: String?,
		val congressdotgov_url: String?
	)
	
	data class RawCosponsorsByParty(
		val R: Long,
		val D: Long,
		val I: Long
	)
	
	data class RawVoteData(
		val chamber: Chamber,
		val date: LocalDate,
		val time: LocalTime,
		val roll_call: Long,
		val question: String,
		val result: String,
		val total_yes: Long,
		val total_no: Long,
		val total_not_voting: Long
	)
	
	data class RawBillSubject(
		val name: String,
		val url_name: String
	)
}

data class RawRecentMemberBills(
	val id: String,
	val member_uri: String,
	val name: String,
	val num_results: Long,
	val offset: Long,
	val bills: List<RawBillDetails>
)

data class RawBillWithCosponsors(
	val bill_id: String,
	val bill_slug: String? = bill_id.split("-").first(),
	val congress: String,
	val bill: String?,
	val bill_type: String,
	val number: String,
	val bill_uri: String,
	val title: String,
	val short_title: String?,
	val sponsor_title: String,
	@SerializedName("sponsor", alternate = ["sponsor_name"])
	val sponsor: String,
	val sponsor_id: String,
	val sponsor_uri: String,
	val sponsor_party: String,
	val sponsor_state: String,
	val gpo_pdf_uri: String? = null,
	val congressdotgov_url: String,
	val govtrack_url: String,
	val introduced_date: LocalDate,
	val active: Boolean,
	val last_vote: LocalDate?,
	val house_passage: LocalDate? = null,
	val senate_passage: LocalDate? = null,
	val enacted: LocalDate? = null,
	val vetoed: LocalDate? = null,
	val cosponsors_by_party: List<RawCosponsorByParty>,
	val cosponsors: List<RawCosponsor>
) {
	data class RawCosponsorByParty(
		val id: String,
		val sponsors: Long
	)
	
	data class RawCosponsor(
		val cosponsor_id: String,
		val name: String,
		val cosponsor_title: String,
		val cosponsor_state: String,
		val cosponsor_party: String,
		val cosponsor_uri: String,
		val date: LocalDate
	)
}

data class RawBillSubjectSearchResults(
	val query: String,
	val num_results: Long,
	val offset: Int,
	val subjects: List<RawBillDetails.RawBillSubject>
)