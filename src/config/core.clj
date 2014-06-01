;; Functions for loading and resolving Java-friendly property files in which property values may cross-refer.
;; Currently property references are restricted to the form ${property-name}, which is at least Spring friendly.

(ns config.core
  (:require
    [clojure.java.io :refer [reader]]))

(def max-nesting-depth (atom 10))

(defn- load-raw-properties [path]
  (with-open [^java.io.Reader reader (reader path)]
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [(keyword k) v])))))

(defn- get-defined-property-value [prop props]
  (or
    (get props prop)
    (throw (RuntimeException. (str "No such property " prop)))))

(def prop-pattern #"\$\{([^\\}]+)\}")

(defn- resolve-property-value [raw-value raw-properties index]
  (if (> index @max-nesting-depth) (throw (RuntimeException. "Properties nested too deeply, perhaps indicating a circular reference")))

  ; Currently fails to retain property resolutions made when resolving nested properties, so may end up resolving the same property more
  ; than once.
  (let [matcher (re-matcher prop-pattern raw-value)
        stringbuffer (StringBuffer.)]
    (while (.find matcher)
      (let [nested-prop (.group matcher 1)
            nested-property-value (resolve-property-value (get-defined-property-value (keyword nested-prop) raw-properties) raw-properties (inc index))]
        (.appendReplacement matcher stringbuffer (clojure.string/replace nested-property-value "$" "\\$"))))
    (.appendTail matcher stringbuffer)
    (.toString stringbuffer)))

(defn resolve-properties [raw-properties]
  "Given a property map, returns a map with identical entries but cross-referring properties resolved."
  (reduce
    (fn [so-far entry]
      (let [prop (key entry)]
        (conj so-far {prop (resolve-property-value (get-defined-property-value prop raw-properties) raw-properties 0)})))
    {}
    raw-properties))

(defn load-and-resolve-properties [path]
  "Given a path to a Java-style properties file, loads properties from the file and returns a map of them in which
  cross-referring properties have been resolved."
  (resolve-properties (load-raw-properties path)))
