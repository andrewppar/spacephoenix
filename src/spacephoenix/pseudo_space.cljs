(ns spacephoenix.pseudo-space
  (:require
   [clojure.string :as string]
   [spacephoenix.app :as app]
   [spacephoenix.message :as message]
   [spacephoenix.window.core :as window.core]
   [spacephoenix.timer :as timer]))

(def initial-spaces [1 2 3])

(def pseudo-spaces (atom
                    (reduce
                     (fn [acc space] (assoc acc space []))
                     {}
                     initial-spaces)))

(def active-space (atom 1))

(def default-assignment
  {"WezTerm" 1
   "Firefox" 2
   "Webex" 3})



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

(defn get-space [window]
  (some
   (fn [space]
     (some
      (fn [{comp-window :window}]
        (when (window.core/equal? window comp-window)
          space))
      (get @pseudo-spaces space)))
   (spaces)))

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
        ;; should/could we use set stautus here?
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

(defmacro with-event-suspend [event-name & body]
  `(do
     (.off js/Event ~event-name)
     (do ~@body)
     (.on js/Event ~event-name)))

(defn window-to-space [space-number window]
  (with-event-suspend window-minimize-event
    (let [entry (or (get-entry window :space space-number)
                    (entry window))]
      (remove-window! window)
      (add-entry! space-number entry)
      (when-not (= space-number @active-space)
        (let [to-focus (window.core/visible-neighbor window)]
          (window.core/minimize window)
          (when to-focus
            (window.core/focus to-focus)))))))

(defn to-space [space-number]
  (window-to-space space-number (window.core/focused)))

(defn make []
  (let [new-space (inc (apply max (spaces)))]
    (swap! pseudo-spaces assoc new-space [])
    (message/alert (str "space " new-space " created"))))

(defn serialize-entry [{:keys [window app]}]
  (let [title (window.core/title window)
        formatted (if (>= (count title) 20)
                    (str (subs title 0 17) "...")
                    title)]
    (str app " - " formatted)))

(defn ^:private serialize-entries [entries]
  (let [titles (mapv serialize-entry entries)]
    (reduce
     (fn [acc* title]
       (str acc* "\n     " title))
     (first titles)
     (rest titles))))

(defn space-list []
  (let [assigned (reduce
                  (fn [acc entries] (into acc (map :app entries)))
                  #{"Phoenix"}
                  (vals @pseudo-spaces))
        unassigned (->> (app/all)
                        (filter (comp seq app/windows))
                        (map app/title)
                        (filter (complement (partial contains? assigned)))
                        set)]
    (message/alert
     (str
      (reduce-kv
       (fn [acc space entries]
         (let [active? (= space @active-space)]
           (str acc (if active? "*" " ")
                space ": "
                (serialize-entries entries)
                "\n")))
       "spaces\n=====\n"
       @pseudo-spaces)
      (when (seq unassigned)
        (str
         "Unassigned: \n"
         (string/join "\n" unassigned)))))))

(def window-close-event
 (.on js/Event "windowDidClose" clean-up!))

(def app-activate-event
  (.on js/Event "appDidLaunch"
       (fn [app]
         (when-let [space (app-space (app/title app))]
           (activate-app! app space)
           (activate space)))))

;;; generalize this!!
(def app-activate-space-management
  (.on js/Event "appDidLaunch"
       (fn [app]
         (timer/make
          1
          (fn []
            (let [app-name (app/title app)]
              (when-let [space (get default-assignment app-name)]
                (run! (partial window-to-space space) (app/windows app)))))))))

(def window-focus-event
  (.on js/Event "windowDidFocus"
       (fn [window]
         (let [space (or (get-space window) @active-space)]
           (set-status! window :minimized? false)
           (activate space)))))
