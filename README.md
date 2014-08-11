edn-scala
=========

A Scala wrapper around [edn-java](https://github.com/bpsm/edn-java); Provides some Scala sugar for
accessing [EDN](https://github.com/edn-format/edn) formatted data.

EDN files are popular as _Clojure_ configuration files -
you can read such a file into your Scala code as a
```scala.collection.immutable.Map``` like this:

```
    import com.themillhousegroup.edn.EDNParser
    import com.themillhousegroup.edn.ParseableSource._

    val theMap = EDNParser().asMap("/my-config-file.edn")
    ...
```



## Aims of the project
  - Reduce verbosity of accessing parsers, parseables, keys and values
  - Return Scala collection classes to allow idiomatic operations on data
  - Employ Scala ```Option```s rather than returning ```null``` or magic values


## Credits
Obviously, the [edn-java](https://github.com/bpsm/edn-java) library is the thing doing the heavy-lifting here.


## Getting Started
Bring in the library by adding the following to your ```build.sbt```. 

  - The release repository: 

```
   resolvers ++= Seq(
     "millhouse-releases" at "http://repository-themillhousegroup.forge.cloudbees.com/release"
   )
```
  - The dependency itself: 

```
   libraryDependencies ++= Seq(
     "com.themillhousegroup" %% "edn-scala" % "1.0"
   )

```

## Usage

#### Getting a ```Parseable```

A ```Parseable``` is the interface defined by *edn-java* as the basis of all operation. From *edn-scala*
you really don't need to know much about it because if you ```import com.themillhousegroup.edn.ParseableSource._```
you get implicits defined to automatically get a ```Parseable``` from either a ```scala.io.Source``` or a simple
```String``` representing a filename.

Once you have a ```Parseable```, there are three ways you can use this library; they will be presented in order of (decreasing) ease-of-use.

#### Treating the edn data as a ```Map[String, AnyRef]```
You've already seen an example of this usage pattern above.
Get an ```EDNParser``` instance by calling ```EDNParser()``` and then pass the ```Parseable``` to ```asMap()```.

This is by far the most straightforward way to start working with EDN data from Scala, because once you've got a ```Map```, you can
do all the idiomatic Scala things with it.

Remember that any maps _within_ the top-level map will also be of type ```Map[String, AnyRef]```.
You'll need to cast individual elements using ```asInstanceOf[T]``` to get type-safe access to them.

#### Treating the edn data as a ```Stream[(String, AnyRef)]```
If you're working with *very large* EDN structures, it might be more efficient to treat them as a stream of
```(String, AnyRef)``` tuples. To do this:

```EDNParser().asStream( parseable ) ```


#### The basic ```nextValue()``` mode
This is the "thinnest" wrapper around the Java API; it just gives you an ```Option[T]``` for the next value found in
the EDN data. You'll get a ```None``` if we've got to the end of the data, and a ```ClassCastException``` if the type coercion
didn't work.

Unless you've found a bug in the ```asMap``` or ```asStream``` functions (in which case, raise an [issue](https://github.com/themillhousegroup/edn-scala/issues)!), there is very little reason why you would want to
resort to using this access mode.
