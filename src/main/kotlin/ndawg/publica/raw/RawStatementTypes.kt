package ndawg.publica.raw

import ndawg.publica.types.Chamber
import ndawg.publica.types.Party
import ndawg.publica.types.State
import java.time.LocalDate

data class RawStatement(
	val url: String,
	val date: LocalDate,
	val title: String,
	val statement_type: String,
	val member_id: String,
	val congress: Long,
	val name: String,
	val chamber: Chamber,
	val state: State,
	val party: Party,
	val subjects: List<RawStatementSubject>
)

data class RawStatementSubject(
	val api_uri: String?,
	val name: String,
	val slug: String
)
