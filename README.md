# Jughead

A Clojure ArchieML parser using Instaparse

## Usage

Jughead is available on
[Clojars](https://clojars.org/org.clojars.minikomi/jughead). To use it include
in your project.clj dependencies:

```clojure
[org.clojars.minikomi/jughead "0.1.1"]
```

And include the parse function from jughead.core:

```clojure
(ns jugheadtest.core
  (:require [jughead.core :refer [parse]]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(defn -main []
  (pprint
    (parse 
      "[yokozuna]

      name: Kakuryū Rikisaburō
      promoted: 2014
      ring-entering-style: Unryū

      name: Harumafuji Kōhei
      promoted: 2012
      ring-entering-style: Shiranui

      name: Hakuhō Shō
      promoted: 2007
      ring-entering-style: Shiranui
      "
      )))
```

The above when `lein run` will print out the resulting map:

```clojure
{:yokozuna
 [{:ring-entering-style "Unryū",
   :promoted "2014",
   :name "Kakuryū Rikisaburō"}
  {:ring-entering-style "Shiranui",
   :promoted "2012",
   :name "Harumafuji Kōhei"}
  {:ring-entering-style "Shiranui",
   :promoted "2007",
   :name "Hakuhō Shō"}]}
```

## License

Copyright © 2015 Adam Moore

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
