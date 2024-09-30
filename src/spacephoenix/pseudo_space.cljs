(ns spacephoenix.pseudo-space
  (:require
   [spacephoenix.window.core :as window.core]
   [spacephoenix.message :as message]
   [spacephoenix.app :as app]))

(def initial-spaces [1 2 3])
(def pseudo-spaces (atom
                    (reduce
                     (fn [acc space] (assoc acc space []))
                     {}
                     initial-spaces)))
(def active-space (atom 1))

(defn spaces []
  (keys @pseudo-spaces))

(defn entry [window]
  {:window window
   :app (app/title (window.core/app window))
   :minimized? (window.core/minimized? window)})

(defn entry-app [{:keys [app]}]
  app)

(defn minimize [{:keys [window]}]
  (window.core/minimize window))

(defn get-space-entry [space window]
  (some
   (fn [{comp-window :window :as entry}]
     (when (window.core/equal? window comp-window) entry))
   (get @pseudo-spaces space)))

(defn get-entry [window & {:keys [space]}]
  (or (when space (get-space-entry space window))
      (some
       (fn [space]
         (get-space-entry space window))
       (spaces))))

(defn ^:private space-found-for-app [space app-name]
  (some
   (fn [{:keys [app]}] (when (= app app-name) space))
   (get @pseudo-spaces space)))


(defn app-space [app-name]
  (or (space-found-for-app @active-space app-name)
      (some
       (fn [space]
         (space-found-for-app space app-name))
       (spaces))))

(defn remove-window! [to-remove]
  (reset!
   pseudo-spaces
   (reduce-kv
    (fn [acc space entries]
      (let [new-entries (filterv
                         (fn [{:keys [window]}]
                           (not (window.core/equal? window to-remove)))
                         entries)]
        (assoc acc space new-entries)))
    {}
    @pseudo-spaces)))

(defn set-status! [window status-type status]
  (let [entry-fn (fn [{comp-window :window :as entry}]
                   (if (window.core/equal? window comp-window)
                     (assoc entry status-type status)
                     entry))]
    (reset!
     pseudo-spaces
     (reduce-kv
      (fn [acc space entries]
        (assoc acc space (mapv entry-fn entries)))
      {}
      @pseudo-spaces))))

(defn window-spaces [window]
  (reduce-kv
   (fn [acc space entries]
     (let [in-space? (some
                      (fn [{comp-window :window}]
                        (window.core/equal? comp-window window))
                      entries)]
       (if in-space?
         (conj acc space)
         acc)))
   #{}
   @pseudo-spaces))

(defn activate-app! [app space]
  (let [app-title (app/title app)
        entries (mapv
                 (fn [{:keys [app] :as entry}]
                   (if (= app app-title)
                     (assoc entry :minimized? false)
                     entry))
                 (get @pseudo-spaces space))]
    (swap! pseudo-spaces assoc space entries)))

(def exclude-apps #{"Phoenix"})

(defn valid-entry? [{:keys [window] :as entry}]
  (and (not (contains? exclude-apps (entry-app entry)))
       (window.core/standard? window)))

;; Are these functions too low level for state? Or is this the
;; right place to have state management?
(defn add-entry! [space-number entry]
  (when (valid-entry? entry)
    (swap! pseudo-spaces update space-number (fnil conj []) entry)))

(def window-minimize-event
  (.on js/Event "windowDidMinimise"
       (fn [window]
         (let [new-entries (mapv
                            (fn [{comp-window :window :as entry}]
                              (if (window.core/equal? comp-window window)
                                (assoc entry :minimized? true)
                                entry))
                            (get @pseudo-spaces @active-space))]
           (swap! pseudo-spaces assoc @active-space new-entries)))))

(def window-unminimize-event
  (.on js/Event "windowDidUnminimise"
       (fn [window]
         (let [new-entries (mapv
                            (fn [{comp-window :window :as entry}]
                              (if (window.core/equal? comp-window window)
                                (assoc entry :minimized? false)
                                entry))
                            (get @pseudo-spaces @active-space))]
           (swap! pseudo-spaces assoc @active-space new-entries)))))

(defn minimize-focused []
  (let [window (window.core/focused)
        neighbor (window.core/visible-neighbor window)]
    (window.core/minimize window)
    (when neighbor
      (window.core/focus neighbor))))

(defn activate [space]
  (when-not (= @active-space space)
    (.off js/Event window-minimize-event)
    (.off js/Event window-unminimize-event)
    (let [to-activate (get @pseudo-spaces space)]
      (run!
       (fn [[space-number window-entries]]
         (when (not= space-number space)
           (run!
            (fn [{:keys [window]}] (window.core/minimize window))
            window-entries)))
       @pseudo-spaces)
      (run! (fn [{:keys [minimized? window]}]
              ;; maybe factor this out
              (if minimized?
                (window.core/minimize window)
                (window.core/unminimize window)))
            to-activate))
    (reset! active-space space)
    (when-not (window.core/focused)
      (some
       (fn [{:keys [window]}]
         (when (window.core/visible? window)
           (window.core/focus window)))
       (get @pseudo-spaces @active-space)))
    (.on js/Event window-minimize-event)
    (.on js/Event window-unminimize-event)
    (message/alert (str "space: " @active-space))))

(defn clean-up! []
  (reset! pseudo-spaces
          (reduce-kv
           (fn [acc space entries]
             (assoc acc space (filterv valid-entry? entries)))
           {}
           @pseudo-spaces)))

(defn delete! [space]
  (swap! pseudo-spaces dissoc space))

(defn to-space [space-number]
  (.off js/Event window-minimize-event)
  (let [window (window.core/focused)
        entry (or (get-entry window :space space-number)
                  (entry window))]
    (remove-window! window)
    (add-entry! space-number entry)
    (when-not (= space-number @active-space)
      (let [to-focus (window.core/visible-neighbor window)]
        (window.core/minimize window)
        (when to-focus
          (window.core/focus to-focus))))
    (.on js/Event window-minimize-event)))

(defn make []
  (let [new-space (inc (apply max (spaces)))]
    (swap! pseudo-spaces assoc new-space [])
    (message/alert (str "space " new-space " created"))))

(defn ^:private serialize-entries [entries]
  (let [titles (set (map entry-app entries))]
    (reduce
     (fn [acc* title]
       (str acc* "\n     " title))
     (first titles)
     (rest titles))))

(defn space-list []
  (message/alert
   (reduce-kv
    (fn [acc space entries]
      (let [active? (= space @active-space)]
        (str acc (if active? "*" " ")
             space ": "
             (serialize-entries entries)
             "\n")))
    "spaces\n=====\n"
    @pseudo-spaces)))

(def window-close-event
 (.on js/Event "windowDidClose" clean-up!))

(def app-activate-event
  (.on js/Event "appDidActivate"
       (fn [app]
         (when-let [space (app-space (app/title app))]
           (activate-app! app space)
           (activate space)))))
