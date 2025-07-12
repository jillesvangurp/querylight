import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.cli.default
import kotlinx.cli.multiple
import org.yaml.snakeyaml.Yaml
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import search.*

fun main(args: Array<String>) {
    val parser = ArgParser("querylight-indexer")
    val dirs by parser.option(ArgType.String, shortName = "d", description = "Directories to scan").multiple().required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file").required()
    val datePattern by parser.option(ArgType.String, shortName = "p", description = "Date pattern in file name").default("yyyy-MM-dd'T'HH:mm'Z'")
    parser.parse(args)

    val formatter = DateTimeFormatter.ofPattern(datePattern)

    val index = DocumentIndex(
        mutableMapOf(
            "content" to TextFieldIndex(),
            "title" to TextFieldIndex(),
            "tags" to TextFieldIndex(),
            "createdAt" to TextFieldIndex()
        )
    )

    val yaml = Yaml()

    dirs.forEach { dir ->
        File(dir).walk().filter { it.isFile && it.extension == "md" }.forEach { file ->
            val text = file.readText()
            val (meta, content) = parseFrontMatter(text, yaml)
            val title = meta["title"]?.toString() ?: file.nameWithoutExtension
            val tags = (meta["tags"] as? Iterable<*>)?.map { it.toString() } ?: emptyList()
            val dateString = extractDate(file.nameWithoutExtension, formatter)
            val fields = mutableMapOf(
                "title" to listOf(title),
                "content" to listOf(content)
            )
            if (tags.isNotEmpty()) fields["tags"] = tags
            dateString?.let { fields["createdAt"] = listOf(it) }
            val doc = Document(file.path, fields)
            index.index(doc)
        }
    }

    val json = Json.encodeToString(DocumentIndexState.serializer(), index.indexState)
    File(output).writeText(json)
}

private fun parseFrontMatter(text: String, yaml: Yaml): Pair<Map<String, Any>, String> {
    val re = Regex("^---\\s*([\\s\\S]*?)\\s*---\\s*(.*)", RegexOption.DOT_MATCHES_ALL)
    val m = re.find(text)
    return if (m != null) {
        val meta = yaml.load<Map<String, Any>>(m.groupValues[1]) ?: emptyMap()
        meta to m.groupValues[2].trim()
    } else {
        emptyMap<String, Any>() to text
    }
}

private fun extractDate(name: String, formatter: DateTimeFormatter): String? {
    val length = LocalDateTime.now().format(formatter).length
    for (i in 0..name.length - length) {
        val part = name.substring(i, i + length)
        try {
            val dt = LocalDateTime.parse(part, formatter)
            return dt.toString()
        } catch (_: Exception) { }
    }
    return null
}
