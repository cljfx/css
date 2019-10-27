(ns cljfx.css
  (:import [clojure.lang Named]
           [java.net URLStreamHandler URL URLConnection]
           [java.io ByteArrayInputStream])
  (:gen-class :extends java.net.spi.URLStreamHandlerProvider))

(set! *warn-on-reflection* true)

(def *registry (atom {}))

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

(defn- -createURLStreamHandler [this protocol]
  (when (= protocol "cljfx-css")
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
  "Globally register style map with associated keyword identifier"
  [id m]
  (swap! *registry assoc id m)
  id)

(defn url
  "Returns an url string that will load associated style's cascading style sheet"
  [id]
  (str "cljfx-css:?" (symbol id) "#" (hash (get @*registry id))))