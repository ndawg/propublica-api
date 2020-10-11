package ndawg.publica.types

import ndawg.publica.raw.RawBillAmendment
import ndawg.util.from
import com.google.gson.Gson
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.time.LocalDate

data class BillAmendment(
	/** The 'number' used to refer to this amendment, eg "H.AMDT.105". */
	val number: String,
	/** The technical identifier for this amendment, eg "hamdt105". */
	val slug: String,
	/** The congressional member who sponsored this amendment. */
	val sponsor: SponsorMeta,
	/** The day this amendment was introduced to congress. */
	val introduced: LocalDate,
	/** A "title" for the amendment, which is usually more of a verbose description. */
	val title: String,
	/** The congress.gov link for this amendment's text. */
	val link: String,
	/** The date of the last major action related to this amendment. */
	val lastActionDate: LocalDate,
	/** The description for the last major action related to this amendment. */
	val lastAction: String
) {
	companion object : KoinComponent {
		fun from(raw: RawBillAmendment): BillAmendment {
			val gson = get<Gson>()
			return BillAmendment(
				raw.amendment_number,
				raw.slug,
				SponsorMeta(
					raw.sponsor_id,
					raw.sponsor,
					gson.from(raw.sponsor_party),
					gson.from(raw.sponsor_state),
					raw.sponsor_title
				),
				raw.introduced_date,
				raw.title,
				raw.congressdotgov_url,
				raw.latest_major_action_date,
				raw.latest_major_action
			)
		}
	}
}