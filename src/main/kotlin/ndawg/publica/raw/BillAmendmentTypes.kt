package ndawg.publica.raw

import java.time.LocalDate

data class RawBillAmendmentsResult(
	val congress: String,
	val bill_id: String,
	val num_results: Int,
	val offset: Int,
	val amendments: List<RawBillAmendment>
)

data class RawBillAmendment(
	val amendment_number: String,
	val slug: String,
	val sponsor_title: String,
	val sponsor: String,
	val sponsor_id: String,
	val sponsor_uri: String,
	val sponsor_party: String,
	val sponsor_state: String,
	val introduced_date: LocalDate,
	val title: String,
	val congressdotgov_url: String,
	val latest_major_action_date: LocalDate,
	val latest_major_action: String
)