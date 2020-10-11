package ndawg.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

interface Serialization {
	fun serialize(obj: Any): String
	fun <T> deserialize(input: String, type: Type): T
}

class GsonSerialization(val gson: Gson): Serialization {
	
	override fun serialize(obj: Any): String {
		return gson.toJson(obj)
	}
	
	override fun <T> deserialize(input: String, type: Type): T {
		return gson.fromJson<T>(input, type)
	}
	
}

inline fun <reified T> Serialization.deserialize(input: String)
	= this.deserialize<T>(input, object: TypeToken<T>() {}.type)

inline fun <reified T> Serialization.get(input: String?): T? {
	if (input == null) return null
	return this.deserialize(input)
}
