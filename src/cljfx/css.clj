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

(defrecord Css []
  IFn
  (invoke [this]
    (let [acc (StringBuilder.)]
      (write acc "" this)
      (.toString acc)))
  (invoke [this x]
    (hydrate this x)))

(defn make [m]
  (map->Css (hydrate m m)))

(defn url [id]
  (str "cljfx-css:?" (symbol id) "#" (hash (id @registry/*registry))))

(defn register [id css]
  (let [css (cond-> css (not (instance? Css css)) make)]
    (swap! registry/*registry assoc id css)
    css))