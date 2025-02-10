(ns spacephoenix.tile
  (:require
   [spacephoenix.app :as app]
   [spacephoenix.config :as cfg]
   [spacephoenix.screen :as screen]
   [spacephoenix.window.core :as window]))

(def events (atom nil))

(defn ->location [x y height width]
  {:x x :y y :h height :w width})


(defn shrink
  [location dimension &
   {:keys [factor amount] :or {amount 0}}]
  (let [factor-constant (if factor (/ (get location dimension) factor) 0)
        new-dimension (+ factor-constant amount)]
    (update location dimension - new-dimension)))

(defn move
  [{width :w height :h :as location} axis direction &
   {:keys [factor amount] :or {amount 0}}]
  (let [move-factor (if factor
                      (case axis
                        :x (/ width factor)
                        :y (/ height factor))
                      0)
        move-amount (+ move-factor amount)
        update-fn (fn [axis-loc] (direction axis-loc move-amount))]
    (update location axis update-fn)))

(defn pad [location direction amount]
  (case direction
    :top (-> location
             (shrink :h :amount amount)
             (move :y + :amount amount))
    :bottom (-> location
                (shrink :h :amount amount))
    :left (-> location
              (shrink :w :amount amount)
              (move :x + :amount amount))
    :right (-> location
               (shrink :w :amount amount))))

(defn config-pad [location]
  (let [config (cfg/padding)]
    (reduce
     (fn [location* direction]
       (if-let [amount (get config direction)]
         (pad location* direction amount)
         location*))
     location
     [:top :bottom :left :right])))

(defn tile-configurations [origin-x origin-y height width]
  (let [location (->location origin-x origin-y height width)
        quarter-size (-> location (shrink :w :factor 2) (shrink :h :factor 2))
        half-height (-> location (shrink :h :factor 2))
        half-width (-> location (shrink :w :factor 2))]
    {1 [(config-pad location)]
     2 [(config-pad half-width)
        ;;or :amount (* width 2))
        (-> half-width (move :x + :factor 1) config-pad)]
     3 [(config-pad quarter-size)
        (-> quarter-size (move :x + :factor 1) config-pad)
        (-> half-height (move :y + :factor 1) config-pad)]
     4 [(config-pad quarter-size)
        (-> quarter-size (move :x + :factor 1) config-pad)
        (-> quarter-size (move :y + :factor 1) config-pad)
        (-> quarter-size (move :x + :factor 1) (move :y + :factor 1) config-pad)]}))

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
                      (tile-configurations x y height width) window-count)]
    (if window-map
      (mapv window/move-to windows window-map)
      (mapv window/maximize windows))))

(defn dont-care-window? [window]
  (contains? app/dont-care-apps (app/title (window/app window))))

(defn tile-on-window-activity [closing? window]
  (when (and (not (dont-care-window? window))
             (or closing? (window/normal? window)))
    (tile)))

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
     (let [closing? (= event-string "windowDidClose")]
       (swap! events conj
              (.on js/Event event-string
                   (partial tile-on-window-activity closing?)))))
   ["windowDidOpen" "windowDidClose"]))

(defn stop-auto-tile []
  (mapv (fn [event] (.off js/Event event)) @events))
