(ns spacephoenix.window
  (:require
   [clojure.string :as string]
   [spacephoenix.utils :refer [defn-timed]]))

(defn title [window]
  (try (.title window)
       (catch js/Object _
         (println "No Title for window"))))

(defn screen [window]
  (.screen window))

(defn normal? [window]
  (and
   (= 1 (.isNormal window))
   (not (string/starts-with? (title window) "Float"))
   (not= (title window) "")))

(defn-timed all [& {:keys [only-normal?]
                    :or {only-normal? true}}]
  (.all js/Window))

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

(defn spaces [window]
  (.spaces window))

(defn move-to [window {:keys [x y h w]}]
  (.setTopLeft window (clj->js {:x x :y y}))
  (.setSize window (clj->js {:width w :height h}))
  window)

(defn close [window]
  (.close window))

(defn close-focused []
  (close (focused)))
