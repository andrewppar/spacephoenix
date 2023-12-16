(ns spacephoenix.tile
  (:require
   [spacephoenix.app :as app]
   [spacephoenix.screen :as screen]
   [spacephoenix.space :as space]
   [spacephoenix.window :as window]
   [spacephoenix.utils :refer [defn-timed]]))

(def events (atom nil))

;;; TODO: refactor this
(defn tile-configurations [origin-x origin-y height width]
  {1 [{:x origin-x :y origin-y :h height :w width}]
   2 [{:x origin-x :y origin-y :h height :w (/ width 2)}
      {:x (+ origin-x (/ width 2)) :y origin-y :h height :w (/ width 2)}]
   3 [{:x origin-x :y  origin-y :h (/ height 2) :w (/ width 2)}
      {:x (+ origin-x (/ width 2)) :y origin-y :h (/ height 2) :w (/ width 2)}
      {:x origin-x :y  (+ origin-y (/ height 2)) :h (/ height 2) :w width}]
   4 [{:x origin-x :y origin-y :h (/ height 2) :w (/ width 2)}
      {:x (+ origin-x (/ width 2)) :y origin-y :h (/ height 2) :w (/ width 2)}
      {:x origin-x :y (+ origin-y (/ height 2)) :h (/ height 2) :w (/ width 2)}
      {:x (+ origin-x (/ width 2)) :y (+ origin-y (/ height 2)) :h (/ height 2) :w (/ width 2)}]})

(defn-timed tile []
  (let [screen (screen/current)
        windows (->> (app/all)
                     (reduce
                      (fn [acc app]
                        (let [windows (.windows app)]
                          (if (seq windows)
                            (apply conj acc windows)
                            acc)))
                      [])
                     (filter
                      (fn [window]
                        (and (= (window/screen window) screen)
                             (window/standard? window)))))
        {:keys [x y height width]} (screen/current-size-and-position)
        window-count (count windows)
        window-map   (get
                      (tile-configurations x y height width)
                      window-count)]
    (if window-map
      (mapv window/move-to windows window-map)
      (mapv window/maximize windows))))

(defn start-auto-tile []
  (mapv
   (fn [event-string]
     (swap! events conj
            (.on js/Event event-string tile)))
   ["spaceDidChange"
    "windowDidOpen"
    "windowDidClose"
    ;;"windowDidFocus"
    "windowDidMove"
    "windowDidResize"
    "windowDidMinimize"
    "windowDidUnminimize"]))

(defn stop-auto-tile []
  (mapv (fn [event] (.off js/Event event)) @events))
