package ndawg.publica.types

import ndawg.publica.raw.RawStatement
import ndawg.publica.raw.RawStatementSubject
import org.koin.standalone.KoinComponent
import java.time.LocalDate

/**
 * A single statement on a legislative topic.
 */
data class Statement(
	/** The URL to view the full statement. */
	val url: String,
	/** The title of the statement, which serves as a brief summary. */
	val title: String,
	/** The type of statement made, usually `Press Release`. */
	val type: String,
	/** The date the statement was made on. */
	val date: LocalDate,
	/** The member making the statement. */
	val member: ICongressMember,
	/** All relevant subjects, determined by ProPublica. */
	val subjects: List<StatementSubject>
) {
	companion object : KoinComponent {
		fun from(raw: RawStatement): Statement {
			return Statement(
				raw.url,
				raw.title,
				raw.statement_type,
				raw.date,
				SimpleCongressMember(
					raw.member_id, raw.name, raw.party, raw.state
				),
				raw.subjects.map {
					StatementSubject(it.name, it.slug)
				}
			)
		}
	}
}

/**
 * A category, decided by ProPublica, used for legislative topics.
 */
data class StatementSubject(
	/** The name of the subject, eg `Affordable Care Act Repeal` */
	val name: String,
	/** The slug assigned, eg `affordable-care-act-repeal` */
	val slug: String
) {
	companion object {
		fun from(raw: RawStatementSubject): StatementSubject {
			return StatementSubject(raw.name, raw.slug)
		}
	}
}