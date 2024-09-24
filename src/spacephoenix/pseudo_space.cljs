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

(defn entry-app [{:keys [window]}]
  (app/title (window.core/app window)))

(defn minimize [{:keys [window]}]
  (window.core/minimize window))

(defn get-space-entry [space window]
  (some
   (fn [{comp-window :window :as entry}]
         (when (.isEqual window comp-window)
           entry))
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
                           (not (.isEqual window to-remove)))
                         entries)]
        (assoc acc space new-entries)))
    {}
    @pseudo-spaces)))

(defn set-status! [window status-type status]
  (let [entry-fn (fn [{comp-window :window :as entry}]
                   (if (.isEqual window comp-window)
                     (assoc entry status-type status)
                     entry))]
    (reset!
     pseudo-spaces
     (reduce-kv
      (fn [acc space entries]
        (assoc acc space (mapv entry-fn entries)))
      {}
      @pseudo-spaces))))

(defn minimize-focused []
  (let [window (window.core/focused)]
    (set-status! window :minimized? true)
    (window.core/minimize window)))

(def exclude-apps #{"Phoenix"})

(defn valid-entry? [{:keys [window] :as entry}]
  (and (not (contains? exclude-apps (entry-app entry)))
       (window.core/standard? window)))

;; Are these functions too low level for state? Or is this the
;; right place to have state management?
(defn add-entry! [space-number entry]
  (when (valid-entry? entry)
    (swap! pseudo-spaces update space-number (fnil conj []) entry)))

(defn activate [space]
  (when-not (= @active-space space)
    (let [to-activate (get @pseudo-spaces space)]
      (run!
       (fn [[space-number window-entries]]
         (when (not= space-number space)
           (run! minimize window-entries)))
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
    (message/alert (str "space: " @active-space))))

(defn clean-up! []
  (reset! pseudo-spaces
          (reduce-kv
           (fn [acc space entries]
             (assoc acc space (filterv valid-entry? entries)))
           {}
           @pseudo-spaces)))

(defn to-space [space-number]
  (let [window (window.core/focused)
        entry (or (get-entry window :space space-number)
                  (entry window))]
    (remove-window! window)
    (add-entry! space-number entry)
    (when-not (= space-number @active-space)
      (let [to-focus (window.core/neighbor window)]
        (window.core/minimize window)
        (when to-focus
          (window.core/focus to-focus))))))

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

(.on js/Event "windowDidOpen"
     (fn [window]
       (clean-up!)
       (add-entry! @active-space (entry window))))

(.on js/Event "windowDidClose" clean-up!)


(.on js/Event "appDidActivate"
     (fn [app]
       (clean-up!)
       (when-let [space (app-space (app/title app))]
         (activate space))))
