(ns cljfx.css.registry
  (:import [java.net URLStreamHandler URL URLConnection]
           [java.io ByteArrayInputStream]))

(def *registry (atom {}))

(gen-class
  :name cljfx.css.registry.StreamHandlerProvider
  :extends java.net.spi.URLStreamHandlerProvider)

(defn -createURLStreamHandler [this protocol]
  (if (= protocol "cljfx-css")
    (proxy [URLStreamHandler] []
      (openConnection [^URL url]
        (proxy [URLConnection] [url]
          (connect [])
          (getInputStream []
            (let [^String css ((get @*registry (keyword (.getQuery url))))]
              (ByteArrayInputStream. (.getBytes css "UTF-8")))))))))