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

(def ^:private url-protocol "cljfx-css")

(defn- -createURLStreamHandler [this protocol]
  (when (= protocol url-protocol)
    (proxy [URLStreamHandler] []
      (openConnection [^URL url]
        (proxy [URLConnection] [url]
          (connect [])
          (getInputStream []
            (let [style (get @*registry (keyword (.getQuery url)))
                  acc (StringBuilder.)]
              (write acc "" style)
              (ByteArrayInputStream. (.getBytes (.toString acc) "UTF-8")))))))))

(defn register
  "Globally register style map describing CSS with associated keyword identifier

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
  (swap! *registry assoc id m)
  m)

(defn- make-url [id m]
  (str url-protocol ":?" (symbol id) "#" (hash m)))

(defn url
  "Returns an URL string that will load registered style's CSS"
  [id]
  (make-url id (get @*registry id)))

(defn watch
  "Add a watch function for id that will receive new URL string and style map whenever
  a new style map for this id is registered

  `key` is a watch identifier, using it with different `f` or `id` will overwrite existing
  watch"
  [key id f]
  (add-watch *registry key
             (fn [_ _ old new]
               (let [m (get new id)]
                 (when-not (= m (get old id))
                   (f (make-url id m) m)))))
  (let [m (get @*registry id)]
    (f (make-url id m) m)))

(defn unwatch
  "Remove a watcher defined by this `key`"
  [key]
  (remove-watch *registry key))