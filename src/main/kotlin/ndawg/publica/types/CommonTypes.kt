package ndawg.publica.types

import com.google.gson.annotations.SerializedName
import ndawg.publica.PublicaAPI

import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.time.ZoneId

/**
 * A political party. More specifically, the two main parties and independents for everyone else.
 */
enum class Party {
	/** The democratic party. */
	D,
	/** The republican party. */
	R,
	/** The independent party, used for anyone who doesn't fit in D or R. */
	@SerializedName("I", alternate = ["ID"])
	I;
	
	/**
	 * An unofficial, informal abbreviation, like `Dem`.
	 */
	val abbrev: String
		get() = when (this) {
			D -> "Dem"
			I -> "Ind"
			R -> "Rep"
		}
	
	/**
	 * The full name of the party.
	 */
	val full: String
		get() = when (this) {
			D -> "Democrat"
			I -> "Independent"
			R -> "Republican"
		}
}

/**
 * A vote position in a simple vote scenario, ie yes/no.
 */
enum class VotePosition(val fancyShort: String, val fancy: String = fancyShort) {
	Yes("Yea"),
	No("Nay"),
	@SerializedName("Not_Voting")
	Abstain("Abs", "Abstain"),
	Present("Present"),
	None("None");
	
	/**
	 * A grouped version of this VotePosition, which essentially is an attempt to simplify
	 * unnecessarily specific positions that have the same impact. For example, `Present` is
	 * considered to be in the group of an `Abstain` vote because it is categorically equal.
	 */
	val group: VotePosition
		get() = when (this) {
			Present -> Abstain
			None -> Abstain
			else -> this
		}
}

/**
 * A named member just represents someone's name. This type exists for cases when a member
 * might not actually be in congress, but still given a vote (for example, a protest vote for speaker
 * of the house might name someone who is not actually in the house at the time).
 */
interface INamedMember {
	/** The full name of the member. */
	val name: String
}

/**
 * Abstract root for a member of congress.
 */
interface ICongressMember : INamedMember {
	/**
	 * The member's unique congressional ID, assigned by the Biographical Directory of
	 * the United States Congress.
	 */
	val id: String
	/** The party the member represents. */
	val party: Party
	/** The state the member represents. */
	val state: State
	
	/**
	 * Performs a full lookup of this member.
	 */
	suspend fun KoinComponent.lookup() = get<PublicaAPI>().getMember(id)
}

/**
 * A simple implementation of [INamedMember].
 */
data class SimpleNamedMember(override val name: String) : INamedMember

/**
 * A simple data representation of a Congress Member, for situations where no additional
 * data is present.
 */
data class SimpleCongressMember(
	override val id: String,
	override val name: String,
	override val party: Party,
	override val state: State
) : ICongressMember

/**
 * The timezone that all federal government actions occur in.
 */
val GOV_TZ = ZoneId.of("America/New_York")

/**
 * Used to express simple pass/fail scenarios.
 */
// TODO use a boolean instead?
enum class VoteResult { PASS, FAIL }

/**
 * A United States' state or territory that needs to be represented.
 */
enum class State constructor(val display: String, val abbrev: String) {
	@SerializedName("Alabama", alternate = ["AL", "Ala."])
	ALABAMA("Alabama", "AL"),
	@SerializedName("Alaska", alternate = ["AK"])
	ALASKA("Alaska", "AK"),
	@SerializedName("AS")
	AMERICAN_SAMOA("American Samoa", "AS"),
	@SerializedName("Arizona", alternate = ["AZ", "Ariz."])
	ARIZONA("Arizona", "AZ"),
	@SerializedName("Arkansas", alternate = ["AR", "Ark."])
	ARKANSAS("Arkansas", "AR"),
	@SerializedName("California", alternate = ["CA", "Calif."])
	CALIFORNIA("California", "CA"),
	@SerializedName("Colorado", alternate = ["CO", "Colo."])
	COLORADO("Colorado", "CO"),
	@SerializedName("Connecticut", alternate = ["CT", "Conn."])
	CONNECTICUT("Connecticut", "CT"),
	@SerializedName("Delaware", alternate = ["DE", "Del."])
	DELAWARE("Delaware", "DE"),
	@SerializedName("District of Columbia", alternate = ["DC", "D.C."])
	DISTRICT_OF_COLUMBIA("District of Columbia", "DC"),
	@SerializedName("FM")
	FEDERATED_STATES_OF_MICRONESIA("Federated States of Micronesia", "FM"),
	@SerializedName("Florida", alternate = ["FL", "Fla."])
	FLORIDA("Florida", "FL"),
	@SerializedName("Georgia", alternate = ["GA", "Ga."])
	GEORGIA("Georgia", "GA"),
	@SerializedName("Guam", alternate = ["GU"])
	GUAM("Guam", "GU"),
	@SerializedName("Hawaii", alternate = ["HI"])
	HAWAII("Hawaii", "HI"),
	@SerializedName("Idaho", alternate = ["ID"])
	IDAHO("Idaho", "ID"),
	@SerializedName("Illinois", alternate = ["IL", "Ill."])
	ILLINOIS("Illinois", "IL"),
	@SerializedName("Indiana", alternate = ["IN", "Ind."])
	INDIANA("Indiana", "IN"),
	@SerializedName("Iowa", alternate = ["IA"])
	IOWA("Iowa", "IA"),
	@SerializedName("Kansas", alternate = ["KS", "Kan."])
	KANSAS("Kansas", "KS"),
	@SerializedName("Kentucky", alternate = ["KY", "Ky."])
	KENTUCKY("Kentucky", "KY"),
	@SerializedName("Louisiana", alternate = ["LA", "La."])
	LOUISIANA("Louisiana", "LA"),
	@SerializedName("Maine", alternate = ["ME", "M.E.", "Me."])
	MAINE("Maine", "ME"),
	@SerializedName("Maryland", alternate = ["MD", "Md."])
	MARYLAND("Maryland", "MD"),
	@SerializedName("MH")
	MARSHALL_ISLANDS("Marshall Islands", "MH"),
	@SerializedName("Massachusetts", alternate = ["MA", "Mass."])
	MASSACHUSETTS("Massachusetts", "MA"),
	@SerializedName("Michigan", alternate = ["MI", "Mich."])
	MICHIGAN("Michigan", "MI"),
	@SerializedName("Minnesota", alternate = ["MN", "Minn."])
	MINNESOTA("Minnesota", "MN"),
	@SerializedName("Mississippi", alternate = ["MS", "Miss."])
	MISSISSIPPI("Mississippi", "MS"),
	@SerializedName("Missouri", alternate = ["MO", "Mo."])
	MISSOURI("Missouri", "MO"),
	@SerializedName("Montana", alternate = ["MT", "Mont."])
	MONTANA("Montana", "MT"),
	@SerializedName("Nebraska", alternate = ["NE", "Neb."])
	NEBRASKA("Nebraska", "NE"),
	@SerializedName("Nevada", alternate = ["NV", "Nev."])
	NEVADA("Nevada", "NV"),
	@SerializedName("New Hampshire", alternate = ["NH", "N.H."])
	NEW_HAMPSHIRE("New Hampshire", "NH"),
	@SerializedName("New Jersey", alternate = ["NJ", "N.J."])
	NEW_JERSEY("New Jersey", "NJ"),
	@SerializedName("New Mexico", alternate = ["NM", "N.M."])
	NEW_MEXICO("New Mexico", "NM"),
	@SerializedName("New York", alternate = ["NY", "N.Y."])
	NEW_YORK("New York", "NY"),
	@SerializedName("North Carolina", alternate = ["NC", "N.C."])
	NORTH_CAROLINA("North Carolina", "NC"),
	@SerializedName("North Dakota", alternate = ["ND", "N.D."])
	NORTH_DAKOTA("North Dakota", "ND"),
	@SerializedName("MP")
	NORTHERN_MARIANA_ISLANDS("Northern Mariana Islands", "MP"),
	@SerializedName("Ohio", alternate = ["OH"])
	OHIO("Ohio", "OH"),
	@SerializedName("Oklahoma", alternate = ["OK", "Okla."])
	OKLAHOMA("Oklahoma", "OK"),
	@SerializedName("Oregon", alternate = ["OR", "Ore."])
	OREGON("Oregon", "OR"),
	@SerializedName("PW")
	PALAU("Palau", "PW"),
	@SerializedName("Pennsylvania", alternate = ["PA", "Pa."])
	PENNSYLVANIA("Pennsylvania", "PA"),
	@SerializedName("Puerto Rico", alternate = ["PR"])
	PUERTO_RICO("Puerto Rico", "PR"),
	@SerializedName("Rhode Island", alternate = ["RI", "R.I."])
	RHODE_ISLAND("Rhode Island", "RI"),
	@SerializedName("South Carolina", alternate = ["SC", "S.C."])
	SOUTH_CAROLINA("South Carolina", "SC"),
	@SerializedName("South Dakota", alternate = ["SD", "S.D."])
	SOUTH_DAKOTA("South Dakota", "SD"),
	@SerializedName("Tennessee", alternate = ["TN", "Tenn."])
	TENNESSEE("Tennessee", "TN"),
	@SerializedName("TX", alternate = ["TX", "Tex."])
	TEXAS("Texas", "TX"),
	@SerializedName("Utah", alternate = ["UT"])
	UTAH("Utah", "UT"),
	@SerializedName("Vermont", alternate = ["VT", "Vt."])
	VERMONT("Vermont", "VT"),
	@SerializedName("VI")
	VIRGIN_ISLANDS("Virgin Islands", "VI"),
	@SerializedName("Virginia", alternate = ["VA", "Va."])
	VIRGINIA("Virginia", "VA"),
	@SerializedName("Washington", alternate = ["WA", "Wash."])
	WASHINGTON("Washington", "WA"),
	@SerializedName("West Virginia", alternate = ["WV", "W.Va."])
	WEST_VIRGINIA("West Virginia", "WV"),
	@SerializedName("Wisconsin", alternate = ["WI", "Wis."])
	WISCONSIN("Wisconsin", "WI"),
	@SerializedName("Wyoming", alternate = ["WY", "Wyo."])
	WYOMING("Wyoming", "WY");
}

enum class Chamber {
	@SerializedName("House", alternate = ["house"])
	House,
	@SerializedName("Senate", alternate = ["senate"])
	Senate
}