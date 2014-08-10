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
  - Obviously, the [edn-java](https://github.com/bpsm/edn-java) library is the thing doing the heavy-lifting here.

This library is currently a work-in-progress, as is (apparently) *edn-java* so don't expect much until a public artifact location is listed here.


