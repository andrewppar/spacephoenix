(ns spacephoenix.screen
  (:require
   [spacephoenix.window.core :as window]
   [spacephoenix.app :as app]
   [spacephoenix.config :as cfg]))

(defn current []
  (.main js/Screen))

(defn id [screen]
  (.hash screen))

(defn equal? [screen-one screen-two]
  (case (cfg/architecture)
    :aarch (.isEqual screen-one screen-two)
    :x86 (= (id screen-one) (id screen-two))))

(defn windows
  [screen & {:keys [visible? native?] :or {visible? true}}]
  ;; for now it seems like native and non-native are comparable
  ;; this was not always the case
  (if native?
    (if visible?
      (.windows screen (clj->js {:visible true}))
      (.windows screen))
    (->> (app/all)
         (reduce (fn [acc app] (into acc (.windows app))) [])
         (filter
          (fn [window]
            (and (window/normal? window)
                 (not (= (window/title window) ""))
                 (not (window/minimized? window))
                 (equal? screen (window/screen window))))))))

(defn height [screen]
  (.-height (.flippedVisibleFrame screen)))

(defn width [screen]
  (.-width (.flippedVisibleFrame screen)))

(defn x [screen]
  (.-x (.flippedVisibleFrame screen)))

(defn y [screen]
  (.-y (.flippedVisibleFrame screen)))

(defn current-size []
  (let [current-screen (current)]
    {:height (height current-screen)
     :width (width current-screen)}))

(defn size-and-position [screen]
  {:height (height screen)
   :width (width screen)
   :x      (x screen)
   :y      (y screen)})

(defn current-size-and-position []
  (size-and-position (current)))

(defn move-window [window screen]
  (window/move-to window (size-and-position screen)))

(defn previous [screen]
  (.previous screen))

(defn all []
  (.all js/Screen))
