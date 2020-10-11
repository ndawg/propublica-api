package ndawg.publica.raw

import ndawg.publica.raw.RawBillDetails

class RecentBillResults(
	val num_results: Int,
	val offset: Int,
	val bills: List<RawBillDetails>
)