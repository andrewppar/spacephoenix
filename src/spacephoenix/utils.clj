(ns spacephoenix.utils
  (:require [clojure.edn :as edn]))

(defmacro defn-timed [name args log-spec & body]
  `(defn ~name ~args
     (let [start# (.now js/Date)
           result# (do ~@body)]
       #_(println "HELLO!!!!")
       (println (str ~(get log-spec :name) " time: " (- (.now js/Date) start#)))
       result#)))

(defmacro defconfig
  "Fetches configuration from resources.
  The configuration is expected to be keyed on your hostname, but if
  that key doesn't exist the default is used."
  []
  (let [hostname (System/getenv "HOST")
        configs (edn/read-string (slurp "./resources/config.edn"))]
    (if (contains? (set (keys configs)) hostname)
      (assoc (get configs hostname) :hostname hostname)
      (get configs "default"))))
