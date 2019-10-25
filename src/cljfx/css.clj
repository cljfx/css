(ns cljfx.css
  (:require [cljfx.css.registry :as registry])
  (:import [clojure.lang IFn]))

(set! *warn-on-reflection* true)

(defn- hydrate [m x]
  (cond
    (map? x) (reduce-kv #(assoc %1 %2 (hydrate m %3)) x x)
    (vector? x) (recur m (get-in m x))
    (sequential? x) (map #(hydrate m %) x)
    :else x))

(defn- write-val [^StringBuilder acc x]
  (cond
    (keyword? x) (.append acc (name x))
    (sequential? x) (run! #(write-val acc %) x)
    :else (.append acc (str x))))

(defn- write [^StringBuilder acc path m]
  (doseq [[k v] m
          :when (string? k)
          :let [path (str path k)]]
    (.append acc path)
    (.append acc " {\n")
    (doseq [[sub-k sub-v] v
            :when (keyword? sub-k)]
      (.append acc "  ")
      (.append acc (name sub-k))
      (.append acc ": ")
      (write-val acc sub-v)
      (.append acc ";\n"))
    (.append acc "}\n")
    (write acc path v)))

(defrecord Style []
  IFn
  (invoke [this]
    (let [acc (StringBuilder.)]
      (write acc "" this)
      (.toString acc)))
  (invoke [this x]
    (hydrate this x)))

(defn make
  "Create Style from passed map

  Style is a both a map that can be used as a source of style information for desktop
  application, and a definition of a cascading style sheet

  Passed map recursively replaces all vectors in it in a value position with values from
  this map using `get-in` to reduce repetition in style definition, for example:
  ```
  (make {:padding {:big 10} \".dialog\" {:-fx-padding [:padding :big]}})
  => {:padding {:big 10} \".dialog\" {:-fx-padding 10}}
  ```

  Cascading style sheet is defined by recursively concatenating string keys starting from
  root to define selectors and then using keyword keys in associated maps to define rules:
  ```
  ((make {\".button\" {:-fx-text-fill \"#aaa\" \":hover\" {:-fx-text-fill \"#ccc\"}}}))
  => .button {
       -fx-text-fill: #aaa;
     }
     .button:hover {
       -fx-text-fill: #ccc;
     }
  ```

  Style has different invocation semantics:
  - when invoked with 0 args, returns it's cascading style sheet as string
  - when invoked with 1 arg, replaces all vector refs in passed data structure with values
  from this style map"
  [m]
  (map->Style (hydrate m m)))

(defn register
  "Globally register Style produced from passed map with associated keyword identifier"
  [id m]
  (let [css (make m)]
    (swap! registry/*registry assoc id css)
    css))

(defn url
  "Returns an url string that will load associated Style's cascading style sheet"
  [id]
  (str "cljfx-css:?" (symbol id) "#" (hash (get @registry/*registry id))))