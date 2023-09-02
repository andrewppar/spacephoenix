(ns phoenix.menu
  (:require [clojure.string :as string]
            [phoenix.keys :as keys]
            [phoenix.message :as message]
            [phoenix.timer :as timer]))

(declare down-layer)
(declare up-layer)

(defn unbind-all-menu-keys []
  (keys/unbind-keys-of-type ::menu))

(defn bind-layer [menu bread-crumbs alert root?]
  (let [new-crumbs (conj bread-crumbs :items)
        timer (timer/make
               5 (fn []
                   (message/notify "Unbinding SpacePhoenix Keys...")
                   (unbind-all-menu-keys)))]
    (when-not root?
      (keys/bind :g [:ctrl]
                 (fn [] (up-layer menu bread-crumbs timer alert))
                 :key-type ::menu))
    (when-let [new-items (get-in menu new-crumbs)]
      (reduce-kv
       (fn [_ key {:keys [items action] :as menu-item}]
         (let [modifiers (get menu-item :modifiers [])]
           (if items
             (let [menu-fn (fn []
                             (message/close-alert alert)
                             (down-layer menu new-crumbs key timer))]
               (keys/bind key modifiers menu-fn :key-type ::menu))
             (keys/bind key modifiers (fn []
                                        (message/close-alert alert)
                                        (timer/off timer)
                                        (unbind-all-menu-keys)
                                        (action))
                        :key-type ::menu))))
       nil
       new-items))))

(defn display-new-bindings [menu bread-crumbs root?]
  (message/alert
   (reduce-kv
    (fn [acc key {:keys [title modifiers]}]
      (let [key-name   (name key)
            key-string (if (= modifiers [])
                         key-name
                         (string/join "+" (conj
                                           (mapv name modifiers)
                                           key-name)))]
        (if title
          (if (= key :nil)
            (str acc title "\n")
            (str acc key-string ": " title "\n"))
          acc)))
    (if-not root? "ctrl+g: Back\n" "")
    (get-in menu (conj bread-crumbs :items)))
   :duration 4))

(defn enter [menu]
  (let [bread-crumbs []
        alert (display-new-bindings menu bread-crumbs true)]
    (bind-layer menu bread-crumbs alert true)))

(defn up-layer [menu bread-crumbs timer alert]
  (message/close-alert alert)
  (unbind-all-menu-keys)
  (timer/off timer)
  (let [new-crumbs (pop (pop bread-crumbs))]
    (if (= [] new-crumbs)
      (enter menu)
      (let [key (peek (pop bread-crumbs))]
        (down-layer menu new-crumbs key timer)))))

(defn down-layer [menu bread-crumbs key timer]
  (timer/off timer)
  (let [new-crumbs   (conj bread-crumbs key)
        alert (display-new-bindings menu new-crumbs true)]
    (unbind-all-menu-keys)
    (bind-layer menu new-crumbs alert false)))
