(ns spacephoenix.message
  (:require
   [clojure.string :as string]
   [spacephoenix.screen :as screen]))

(def alerts (atom {}))

(defn notify
  [^String message]
  (.notify js/Phoenix message))

(defn alert
  [message &
   {:keys [type duration title x-coord y-coord]
    :or {duration 2
         title "spacephoenix"}}]
  (let [modal (js/Modal.)
        lines (string/split message #"\n")
        max-line (apply max (map count lines))
        max-height (count lines)
        {:keys [height width]} (screen/current-size)
        x (or x-coord  (- (/ width  2) (* 10 max-line)))
        y (or y-coord  (+ (/ height 2) (* 20 max-height)))]
    (set! (.-origin modal) #js {:x x :y y })
    (set! (.-text modal) (string/join " " message))
    (set! (.-duration modal) duration)
    (.show modal)
    (swap! alerts assoc modal {:type (or type :default) :title title})
    modal))

(defn close-alert [alert]
  (.close alert)
  (swap! alerts dissoc alert))

(defn close-all-alerts [& {:keys [type]}]
  (reduce-kv
   (fn [acc alert {alert-type :type}]
     (if (or (not type) (= alert-type type))
       (do
         (close-alert alert)
         (conj acc alert))
       acc))
   []
   @alerts))
