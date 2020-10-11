package ndawg.publica

import ndawg.publica.raw.RawPublicaAPI
import ndawg.util.from
import com.google.gson.Gson
import io.kotlintest.shouldThrow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Test
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import org.koin.test.AutoCloseKoinTest
import kotlin.reflect.full.memberProperties

abstract class PublicaAPITest : AutoCloseKoinTest() {
	
	@Before
	open fun before() {
		PublicaAPI.start()
	}
	
	/**
	 * Returns a convenience object containing the ProPublica API Key, an instance of the API, and an instance
	 * of Gson.
	 */
	fun raw() = Raw(get("publica-api-key"), get(), get())
	
	data class Raw(val key: String, val api: RawPublicaAPI, val gson: Gson)
	
	/**
	 * Recursively verifies the properties of this object, ensuring that all properties not marked as nullable are in fact not null.
	 * Because of the way GSON works, it is possible to deserialize null values into non-null fields.
	 */
	@JvmName("vetIndirect")
	fun Any?.vet() {
		requireNotNull(this) {
			"Object to vet is null"
		}
		
		val c = this::class
		if (c.isData) {
			c.memberProperties.forEach { prop ->
				val v = prop.getter.call(this)
				if (!prop.returnType.isMarkedNullable && v == null) {
					throw IllegalStateException("Field '${prop.name}' is not marked as null (type is ${prop.returnType}), but is null in object of type $c ($this)")
				}
				v?.vet()
			}
		}
	}
	
	/**
	 * Calls [vet] for each item in this collection.
	 */
	fun Collection<Any>.vetAll() {
		this.forEach { it.vet() }
	}
	
	/**
	 * Continues making the given call until the returned list is either empty or of a size less than
	 * the standard ProPublica page size. The function is a page index.
	 */
	internal fun <T> asFlow(call: suspend (Int) -> List<T>) = flow {
		var n = 0
		while (true) {
			val res = call(n)
			if (res.isEmpty()) break
			res.forEach { emit(it) }
			if (res.size < 20) break
			n++
		}
	}
	
}

class VetTests : PublicaAPITest() {
	
	data class Sample(val data: Nested)
	data class Nested(val name: String)
	
	data class Simple(val name: String)
	
	@Test
	fun invalidData() {
		val gson = get<Gson>()
		val sample = gson.from<Simple>("""{"name": null}""")
		shouldThrow<IllegalStateException> {
			sample.vet()
		}
	}
	
	@Test
	fun nestedInvalidData() {
		val gson = get<Gson>()
		val sample = gson.from<Sample>("""{"data": {"name": null}}""")
		shouldThrow<IllegalStateException> {
			sample.vet()
		}
	}
	
	@Test
	fun validData() {
		val gson = get<Gson>()
		val sample = gson.from<Simple>("""{"name": "hi"}""")
		sample.vet()
	}
	
}

/**
 * Exhausts a certain end-point, continuing to query it until the returned data is empty.
 */
suspend fun <T> exhaust(call: suspend (Int) -> T, counter: (T) -> Int, receiver: (T) -> Unit) {
	var i = 0
	while (true) {
		val res = call(i)
		if (counter(res) == 0) break
		receiver(res)
		i += 20
	}
}

fun <T> exhaust(call: suspend (Int) -> T, counter: (T) -> Int): Flow<T> = flow {
	var i = 0
	while (true) {
		val res = call(i)
		if (counter(res) == 0) break
		emit(res)
		i += 20
	}
}

/**
 * Retrieves the text from a testing file.
 *
 * @param name The name of the file.
 */
fun Any.testFile(name: String) = this::class.java.getResource("/$name")!!.readText()

inline fun <reified T : Any> KoinComponent.testData(name: String) = CompletableDeferred(get<Gson>().from<T>(testFile(name)))