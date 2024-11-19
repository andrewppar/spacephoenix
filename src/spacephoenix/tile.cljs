(ns spacephoenix.tile
  (:require
   [spacephoenix.app :as app]
   [spacephoenix.screen :as screen]
   [spacephoenix.window.core :as window]))

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

(defn tile []
  (let [windows (->> (app/all)
                     (reduce
                      (fn [acc app]
                        (let [windows (.windows app)]
                          (if (seq windows)
                            (apply conj acc windows)
                            acc)))
                      [])
                     (filter
                      (fn [window]
                        (and (window/normal? window)
                             (not (= (window/title window) ""))
                             (not (window/minimized? window)))))
                     (sort-by window/id))
        {:keys [x y height width]} (screen/current-size-and-position)
        window-count (count windows)
        window-map   (get
                      (tile-configurations x y height width)
                      window-count)]
    (if window-map
      (mapv window/move-to windows window-map)
      (mapv window/maximize windows))))

(defn start-auto-tile []
  (run!
   (fn [event-string]
     (swap! events conj
            (.on js/Event event-string tile)))
   ["appDidActivate"
    "windowDidMinimize"
    "windowDidUnminimize"])
  (run!
   (fn [event-string]
     (swap! events conj
            (.on js/Event event-string
                 (fn [window]
                   (when (and (not (contains?
                                    app/dont-care-apps
                                    (app/title (window/app window))))
                              (window/normal? window))
                     (tile))))))
   ["windowDidOpen" "windowDidClose"]))

(defn stop-auto-tile []
  (mapv (fn [event] (.off js/Event event)) @events))
