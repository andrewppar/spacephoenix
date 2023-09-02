(ns phoenix.app)

(defn launch [app-name & {:keys [focus?] :or {focus? true}}]
  (.launch js/App app-name (clj->js {:focus focus?})))

(defn quit-focused-app [& {:keys [force?] :or {force? false}}]
  (.terminate (.focused js/App) (clj->js {:force force?})))

(defn focus [app-name]
  (.focus (.get js/App app-name)))
