(ns spacephoenix.window
  (:require [clojure.string :as string]))

(defn focused []
  (.focused js/Window))

(defn maximize [window]
  (.maximize window))

(defn maximize-focused []
  (maximize (focused)))

(defn minimize [window]
  (.minimize window))

(defn minimize-focused []
  (minimize (focused)))

(defn focus [window]
  (.focus window))

(defn title [window]
  (try (.title window)
       (catch js/Object _
         (println "No Title for window"))))

(defn normal? [window]
  (= 1 (.isNormal window)))

(defn subrole [window]
  (.subrole window))

(defn standard? [window]
  (and (= (subrole window)
          "AXStandardWindow")
       (not (string/starts-with? (title window) "Float"))
       (not= (title window) "")))

(defn spaces [window]
  (.spaces window))

(defn all [& {:keys [only-normal?]
              :or {only-normal? true}}]
  (cond->> (.all js/Window)
    only-normal? (filter normal?)))

(defn move-to [window {:keys [x y h w]}]
  (.setTopLeft window (clj->js {:x x :y y}))
  (.setSize window (clj->js {:width w :height h}))
  window)

(defn close [window]
  (.close window))

(defn close-focused []
  (close (focused)))
