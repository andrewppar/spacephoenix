(ns spacephoenix.window.core
  (:require
   [clojure.string :as string]
   [spacephoenix.utils :refer [defn-timed]]))

(defn title [window]
  (try (.title window)
       (catch js/Object _
         (println "No Title for window"))))

(defn screen [window]
  (.screen window))

(defn app [window]
  (.app window))

(defn standard? [window]
  (and (not (string/starts-with? (title window) "Float"))
       (not= (title window) "")))

(defn normal? [window]
  (= 1 (.isNormal window)))

(defn minimized? [window]
  (.isMinimised window))

(defn visible? [window]
  (.isVisible window))

(defn all [& {:keys [only-normal?]
              :or {only-normal? true}}]
  (.all js/Window))

(defn focused []
  (.focused js/Window))

(defn neighbors [window]
  (into []
        (mapcat
         (fn [direction]
           (.neighbours window direction))
         ["north" "south" "east" "west"])))

(defn visible-neighbor [window]
  (some
   (fn [direction]
     (some
      (fn [neighbor-window]
        (when (visible? neighbor-window)
          neighbor-window))
      (js->clj (.neighbours window direction))))
   ["east" "west" "north" "south"]))

(defn focus [window]
  (.focus window))

(defn maximize [window]
  (.maximize window))

(defn maximize-focused []
  (maximize (focused)))

(defn minimize [window]
  (.minimize window))

(defn minimize-focused []
  (minimize (focused)))

(defn unminimize [window]
  (.unminimize window))

(defn top-left [window]
  (.topLeft window))

(defn move-to [window {:keys [x y h w]}]
  (.setTopLeft window (clj->js {:x x :y y}))
  (.setSize window (clj->js {:width w :height h}))
  window)

(defn close [window]
  (.close window))

(defn close-focused []
  (close (focused)))
