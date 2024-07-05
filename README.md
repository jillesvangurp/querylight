# Querylight

Querylight is an in memory, kotlin multi platform text indexing library that implements tfidf and a minimal query language. Great for client side search in web apps, android or other Kotlin apps.

This is my attempt at building a small kotlin library for implementing offline search in e.g. a website or inside a
mobile phone app (android, IOS, etc.). This is currently quite early stage and the API can and will change. For documentation, refer to the tests.

# General design

Users of Elasticsearch, will recognize a thing or two. I loosely follow their DSL. At the core is a simple in memory reverse index that implements TF/IDF for ranking. I've implemented a simple Analyzer structure similar to what Elasticsearch has and there's MatchQuery and BoolQuery that are usable for simple queries.

Don't expect amazing performance or search quality. This is not intended to scale massively and only appropriate for small amounts of data. That being said, it does work fine for small data sets in web applications, which is what I use this for.

Key motivation for building this is that I wanted offline search without a server and with some control over things like ranking. Judging from hacker news, there are countless others building their own search libraries. So yes, this is somewhat redundant.

## Kotlin Multiplatform

Since this is a kotlin multiplatform library without dependencies, it should be possible to get this running pretty much anywhere Kotlin runs. I use it mainly in a kotlin-js based web application currently but it should do fine on IOS/Android with compose multiplatform, spring servers, or any other places you use Kotlin.

## Get it 

We publish jars for this library to our own maven repository. Add it like this:

```kotlin
repositories {
    mavenCentral()
    maven("https://maven.tryformation.com/releases")
}
```

and then add the dependency:

```kotlin
implementation("com.github.jillesvangurp:querylight:1.0.0")
```

For older versions of gradle you may have to specify a postfix `-jvm` or `-js`. Supposedly recent versions are smarter about figuring out multiplatform.

Let me know if you have issues accessing the jars.

## Usage

Create an index by mapping your data classes to Documents. A Document has a unique id and a map of fields with zero or more values (all fields can be multivalued).

```kotlin
data class SampleObject(
    val title: String,
    val description: String,
    val id: String = Random.nextULong().toString()
) {
    fun toDoc() = Document(
        id, mapOf(
            "title" to listOf(title),
            "description" to listOf(description)
        )
    )
}

fun quotesIndex(): DocumentIndex {
    val documentIndex = DocumentIndex(
        mutableMapOf(
            "title" to TextFieldIndex(),
            "description" to TextFieldIndex()
        )
    )
    listOf(
        SampleObject("George Orwell, 1984", "War is peace. Freedom is slavery. Ignorance is strength."),
        SampleObject("Jane Austen, Pride and Prejudice", "It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife."),
        SampleObject("F. Scott Fitzgerald, The Great Gatsby", "So we beat on, boats against the current, borne back ceaselessly into the past."),
        SampleObject("William Shakespeare, Hamlet", "To be, or not to be: that is the question."),
        SampleObject("Douglas Adams, The Hitchhiker's Guide to the Galaxy", "The ships hung in the sky in much the same way that bricks don't."),
        SampleObject("Douglas Adams, The Hitchhiker's Guide to the Galaxy", "Don't Panic."),
        SampleObject("Douglas Adams, The Restaurant at the End of the Universe", "Time is an illusion. Lunchtime doubly so."),
        SampleObject("Douglas Adams, Last Chance to See", "Human beings, who are almost unique in having the ability to learn from the experience of others, are also remarkable for their apparent disinclination to do so."),
        SampleObject("Terry Gilliam and Terry Jones, Monty Python and the Holy Grail", "Tis but a scratch."),
        SampleObject("Terry Gilliam and Terry Jones, Monty Python and the Holy Grail", "Nobody expects the Spanish Inquisition!"),
        SampleObject("Terry Gilliam and Terry Jones, Monty Python and the Holy Grail", "Your mother was a hamster and your father smelt of elderberries."),
        SampleObject("Terry Gilliam and Terry Jones, Monty Python's Life of Brian", "He's not the Messiah, he's a very naughty boy!"),
        SampleObject("Philip K. Dick, Do Androids Dream of Electric Sheep?", "You will be required to do wrong no matter where you go. It is the basic condition of life, to be required to violate your own identity."),
        SampleObject("Orson Scott Card, Ender's Game", "In the moment when I truly understand my enemy, understand him well enough to defeat him, then in that very moment, I also love him."),
    ).map(SampleObject::toDoc).forEach(documentIndex::index)
    return documentIndex
}

```

One you have your index, you can query it:

```kotlin
val index = quotesIndex()
val results = index.search {
    query = BoolQuery(
        should = listOf(
            MatchQuery(SampleObject::description.name, "to be")
        )
    )
}
results.forEach { (id,score) ->
    println(id + " " + score + " " + index.documents[id]?.let { "${it.fields["description"]} (${it.fields["title"]})" })
}

```

Currently there are a handful of queries supported. `BoolQuery` is loosely styled after the Elasticsearch/Opensearch bool query. Same for `MatchQuery` and `TermQuery`. There's also a `MatchAll`.
