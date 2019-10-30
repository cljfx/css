(ns cljfx.css
  "Charmingly Simple Styling library allows using clojure data structures to define
  application styles.

  It adds JVM-wide support for `cljfx-css` URL protocol that can then be used to retrieve
  registered style maps as CSS"
  (:import [clojure.lang Named]
           [java.net URLStreamHandler URL URLConnection]
           [java.io ByteArrayInputStream])
  (:gen-class :extends java.net.spi.URLStreamHandlerProvider))

(set! *warn-on-reflection* true)

(def ^:private *registry (atom {}))

(defn- write-val [^StringBuilder acc x]
  (cond
    (instance? Named x) (.append acc (name x))
    (vector? x) (transduce (interpose " ") (completing #(write-val acc %2)) nil x)
    (sequential? x) (run! #(write-val acc %) x)
    :else (.append acc x)))

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


(defn- -createURLStreamHandler [this protocol]
  (when (= protocol "cljfx-css")
    (proxy [URLStreamHandler] []
      (openConnection [^URL url]
        (proxy [URLConnection] [url]
          (getInputStream []
            (let [style (get @*registry (keyword (.getQuery url)))
                  acc (StringBuilder.)]
              (write acc "" style)
              (ByteArrayInputStream. (.getBytes (.toString acc) "UTF-8")))))))))

(defn register
  "Globally register style map describing CSS with associated keyword identifier

  Returns a map with additional key `:cljfx.css/url` containing URL string pointing to CSS
  derived from style map

  CSS is created by recursively concatenating string keys starting from root to define
  selectors, while using keyword keys in value maps to define rules, for example:
  ```
  {\".button\" {:-fx-text-fill \"#ccc\"
              \":hover\" {:-fx-text-fill \"#aaa\"}}}
  ;; corresponds to this css:
  .button {
    -fx-text-fill: #ccc;
  }
  .button:hover {
    -fx-text-fill: #aaa;
  }
  ```"
  [id m]
  (let [css (assoc m ::url (str "cljfx-css:?" (symbol id) "#" (hash m)))]
    (swap! *registry assoc id css)
    css))
