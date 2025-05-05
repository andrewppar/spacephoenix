(ns spacephoenix.config
  (:require
   [clojure.string :as string]))

(def ^:private config
  {:architecture :aarch
   :auto-tile true
   :browser "Zen Browser"
   :launcher "quicksilver"
   :terminal "wezterm"
   :mail "Mail"
   :space
   {:initial [1 2 3]
    :assignment
    {"WezTerm" 1
     "Zen" 1
     "Webex" 2}}
   :padding
   {:left 5
    :right 5
    :top 5
    :bottom 5}})

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

(defn show []
  (let [named-keys (->> config keys (map (juxt name identity)) (into {}))
        max-setting (apply max (map (comp count first) named-keys))]
    (reduce
     (fn [acc [setting-name setting]]
       (let [value (get config setting)
             setting-string (str setting-name
                                 (-> max-setting
                                     (- (count setting-name))
                                     (repeat " ")
                                     string/join))]
         (str acc setting-string ": " value "\n")))
     ""
     (sort-by first named-keys))))
