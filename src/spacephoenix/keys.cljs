(ns spacephoenix.keys)

(def bound-keys (atom {}))

(defn unbind [key]
  (when-let [key-handler (get-in @bound-keys [key :handler])]
    (.off js/Key key-handler)
    (swap! bound-keys dissoc key)))

(defn bind
  [key callback &
   {:keys [key-type modifiers] :or {key-type :default}}]
  (let [key-string (name key)
        handler (.on js/Key key-string (clj->js modifiers) callback)]
    (unbind key)
    (swap! bound-keys assoc key {:handler handler
                                 :modifiers modifiers
                                 :key-type key-type})))

(defn unbind-all []
  (mapv (fn [key] (unbind key)) (keys @bound-keys)))

(defn unbind-keys-of-type [type]
  (reduce-kv
   (fn [_ key {:keys [key-type]}]
     (when (= key-type type)
       (unbind key)))
   nil
   @bound-keys))
