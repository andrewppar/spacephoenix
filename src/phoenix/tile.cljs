(ns phoenix.tile
  (:require
   [phoenix.screen :as screen]
   [phoenix.space :as space]
   [phoenix.window :as window]))

(def events (atom nil))

(defn tile-configurations [height width]
  {1 [{:x 0 :y 0 :h height :w width}]
   2 [{:x 0 :y 0 :h height :w (/ width 2)}
      {:x (/ width 2) :y 0 :h height :w (/ width 2)}]
   3 [{:x 0 :y  0 :h (/ height 2) :w (/ width 2)}
      {:x (/ width 2) :y 0 :h (/ height 2) :w (/ width 2)}
      {:x 0 :y  (/ height 2) :h (/ height 2) :w width}]
   4 [{:x 0 :y 0 :h (/ height 2) :w (/ width 2)}
      {:x (/ width 2) :y 0 :h (/ height 2) :w (/ width 2)}
      {:x 0 :y (/ height 2) :h (/ height 2) :w (/ width 2)}
      {:x (/ width 2) :y (/ height 2) :h (/ height 2) :w (/ width 2)}]})

(defn tile []
  (let [windows (->> (space/current)
                     space/windows
                     (filter window/normal?))
        screen  (screen/current)
        height  (screen/height screen)
        width   (screen/width screen)
        window-count (count windows)
        window-map   (get
                      (tile-configurations height width)
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
    "windowDidFocus"
    "windowDidMove"
    "windowDidResize"
    "windowDidMinimize"
    "windowDidUnminimize"]))

(defn stop-auto-tile []
  (println @events)
  (mapv (fn [event] (.off js/Event event)) @events))
