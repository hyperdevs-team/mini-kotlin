package masmini

import kotlin.reflect.KClass

interface DiffAdapter<T : Any> {

    companion object {
        const val GENERATED_CLASS_SUFFIX = "DiffAdapter"
    }

    /**
     * Produce a json like diff of an object
     */
    fun diff(a: T, b: T, indent: String = "  "): String?
}

class ListDiffAdapter<T : List<*>> : DiffAdapter<T> {
    companion object {
        val INSTANCE = ListDiffAdapter<List<*>>()
    }

    override fun diff(a: T, b: T, indent: String): String? {
        val sb = StringBuilder()
        val aSize = a.size
        val bSize = b.size
        val range = 0..(kotlin.math.max(aSize, bSize))
        range.forEach { index ->
            val ai = a.getOrNull(index)
            val bi = b.getOrNull(index)
            val diff = diffObjects(ai, bi, indent)
            if (diff != null) {
                @Suppress("UNCHECKED_CAST")
                sb.append("\n$index: $diff")
            }
        }
        return "[${sb.toString().prependIndent(indent)}\n]"
    }
}

@Suppress("UNCHECKED_CAST")
class MapDiffAdapter<T : Map<*, *>> : DiffAdapter<T> {

    companion object {
        val INSTANCE = MapDiffAdapter<Map<*, *>>()
    }

    override fun diff(a: T, b: T, indent: String): String? {
        val sb = StringBuilder()
        b.forEach { item ->
            val newValue = item.value
            val oldValue = a[item.key]
            if (oldValue != null) {
                val itemDiff = diffObjects(oldValue, newValue, indent)
                if (itemDiff != null) {
                    sb.append("\n~ ${item.key}: ${item.value}")
                }
            } else {
                sb.append("\n+ ${item.key}: ${item.value}")
            }
        }
        //Check for removed keys
        a.entries.forEach { entry ->
            if (!b.containsKey(entry.key)) {
                sb.append("\n- ${entry.key}: ${entry.value}")
            }
        }
        return "{${sb.toString().prependIndent(indent)}\n}"
    }
}

class DirectDiffAdapter<T : Any> : DiffAdapter<T> {
    override fun diff(a: T, b: T, indent: String): String? {
        return "$a -> $b"
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> diffObjects(a: T?, b: T?, indent: String = "  "): String? {
    if (a === b) return null //Same instance
    if (a == b) return null //Equal
    //One is null, other is not
    if (a != null && b == null) return "${a.toQuotedString()} -> null"
    if (a == null && b != null) return "null -> ${b.toQuotedString()}"
    a as Any
    b as Any
    return when (a) {
        is String, is Number -> "${a.toQuotedString()} -> ${b.toQuotedString()}"
        is List<*> -> ListDiffAdapter.INSTANCE.diff(a, b as List<*>, indent)
        is Map<*, *> -> MapDiffAdapter.INSTANCE.diff(a, b as Map<*, *>, indent)
        else -> {
            @Suppress("USELESS_CAST")
            val adapter: DiffAdapter<Any> = findDiffAdapter((a as Any)::class) as DiffAdapter<Any>
            adapter.diff(a, b as Any, indent)
        }
    }
}

private val adapterCache = HashMap<String, DiffAdapter<*>>()
private fun <T : Any> findDiffAdapter(kclass: KClass<T>): DiffAdapter<T> {
    val adapterClassName = kclass.qualifiedName + DiffAdapter.GENERATED_CLASS_SUFFIX
    @Suppress("UNCHECKED_CAST")
    return adapterCache.getOrPut(adapterClassName) {
        try {
            val adapter = Class.forName(adapterClassName)
            adapter.getDeclaredConstructor().newInstance() as DiffAdapter<*>
        } catch (e: Throwable) {
            DirectDiffAdapter<T>()
        }
    } as DiffAdapter<T>
}