package ndawg.publica.raw

import java.time.LocalDate
import java.time.ZonedDateTime

data class RawUpcomingBillResult(
	val date: String,
	val bills: List<RawUpcomingBill>
)

data class RawUpcomingBill(
	val congress: Long,
	val chamber: String,
	val bill_id: String,
	val bill_slug: String,
	val bill_type: String,
	val bill_number: String,
	val api_uri: String,
	val legislative_day: LocalDate,
	val scheduled_at: ZonedDateTime,
	val range: String,
	val context: String? = null,
	val description: String,
	val bill_url: String,
	val consideration: String,
	val source_type: String,
	val url: String
)
