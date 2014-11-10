edn-scala
=========

[![Build Status](https://travis-ci.org/themillhousegroup/edn-scala.svg?branch=master)](https://travis-ci.org/themillhousegroup/edn-scala)

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
  - Streamline conversion into Scala case classes
  - One-line reading into a [Typesafe `Config`](https://github.com/typesafehub/config) instance
  - Return Scala collection classes to allow idiomatic operations on data

## Credits
Obviously, the [edn-java](https://github.com/bpsm/edn-java) library is the thing doing the heavy-lifting here.


## Getting Started

#### Including the dependency
Bring in the library by adding the following to your ```build.sbt```. 

  - The release repository: 

```
   resolvers ++= Seq(
     "Millhouse Bintray"  at "http://dl.bintray.com/themillhousegroup/maven"
   )
```
  - The dependency itself: 

```
   libraryDependencies ++= Seq(
     "com.themillhousegroup" %% "edn-scala" % "4.0.0"
   )

```

#### A note on Scala versions
Versions of the library up to __3.1.0__ were cross-built for Scala 2.10.4 and 2.11.2, but from __4.x.x__ onwards, only Scala > 2.11.2 is supported. If you intend to use the
read-into-case-class functionality, `scala-reflect` seems to be much more reliable in Scala 2.11, so please use __4.0.0__. 

## Usage

#### Treating the edn data as a [Typesafe `Config`](https://github.com/typesafehub/config) object

Where `myfile.edn` is a file in the root of the resource path:

```
  import com.themillhousegroup.edn.EDNConfigFactory

  val cfg = EDNConfigFactory.load("myfile")

  val foo = cfg.getString("foo") // etc
```

The [Ficus](https://github.com/ceedubs/ficus) library is recommended for idiomatic Scala access to Typesafe Config!

#### Getting a ```Parseable```

A ```Parseable``` is the interface defined by *edn-java* as the basis of all operation. From *edn-scala*
you really don't need to know much about it because if you ```import com.themillhousegroup.edn.ParseableSource._```
you get implicits defined to automatically get a ```Parseable``` from either a ```scala.io.Source``` or a simple
```String``` representing a filename.

Once you have a ```Parseable```, there are two ways to use it:

#### Reading the edn data into a case class

A very handy (and type-safe) way to read configuration settings;
Invoke `readInto[T]` - it'll give you back a `Try[T]` with the results:

```
    import com.themillhousegroup.edn.EDNParser

    val maybeCC:Try[MyCaseClass] = EDNParser().readInto[MyCaseClass](theParseable)

    maybeCC.map { cc =>
        println(s"It worked, my case class is $cc")
    }
```


#### Treating the edn data as a ```Map[String, AnyRef]```
You've already seen an example of this usage pattern above.
Get an ```EDNParser``` instance by calling ```EDNParser()``` and then pass the ```Parseable``` to ```asMap()```.

Remember that any maps _within_ the top-level map will also be of type ```Map[String, AnyRef]```.
You'll need to cast individual elements using ```asInstanceOf[T]``` to get type-safe access to them.
