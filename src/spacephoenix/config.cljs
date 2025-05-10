(ns spacephoenix.config
  (:require
   [spacephoenix.utils :refer [defconfig]]
   [clojure.string :as string]))

(def ^:private config (defconfig))

(defn architecture []
  (get config :architecture))

(defn auto-tile []
  (get config :auto-tile))

(defn browser []
  (get config :browser))

(defn launcher []
  (get config :launcher))

(defn mail []
  (get config :mail))

(defn terminal []
  (get config :terminal))

(defn padding []
  (get config :padding))

(defn space-initial []
  (get-in config [:space :initial]))

(defn space-assignment []
  (get-in config [:space :assignment]))

(defn indent [amount strings]
  (let [indent-string (string/join (repeat amount " "))]
    (mapv (partial str indent-string) strings)))

(defn show-internal [configuration]
  (let [separator " -> "]
    (reduce-kv
     (fn [acc setting-key setting-value]
       (let [setting-name (name setting-key)]
         (if (map? setting-value)
           (let [recursive-case (show-internal setting-value)
                 setting-length (count setting-name)
                 first-item (str setting-name separator (first recursive-case))
                 rest-items (indent (+ setting-length 10) (rest recursive-case))]
             (into (conj acc first-item) rest-items))
           (conj acc (str setting-name separator setting-value)))))
     []
     configuration)))

(defn show []
  (string/join "\n" (show-internal config)))
