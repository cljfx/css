(ns compile
  (:require [clojure.java.io :as io]))

(defn -main []
  (run! io/delete-file (reverse (rest (file-seq (io/file "classes")))))
  (.mkdir (io/file "classes"))
  (compile 'cljfx.css)
  (println "Compiled" 'cljfx.css))
