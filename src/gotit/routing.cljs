(ns ^:figwheel-always gotit.routing
    (:require [goog.events :as events]
              [goog.history.EventType :as EventType]
              [secretary.core :as secretary :refer-macros [defroute]]
              [gotit.common :as common]
              )
    (:import goog.History))

(enable-console-print!)

;;
;; basic hash routing to configure some game options
;;



(secretary/set-config! :prefix "#")


(defn dispatching [t l p f v]
  (let [target (js.parseInt t)
        limit (js.parseInt l)
        players (js.parseInt p)
        first-player (js.parseInt f)
        viewer v]

    (prn "hi")

    (when (and (common/check-target t) (common/check-limit l) (common/check-players p) (common/check-first f))
      (swap! (:game common/Gotit) assoc-in [:settings :target] target)
      (swap! (:game common/Gotit) assoc-in [:settings :limit] limit)
      (swap! (:game common/Gotit) assoc-in [:settings :players] players)

      (let [fp (if (and (= 2 players) (zero? first-player)) :a :b)]
        (swap! (:game common/Gotit) assoc-in [:play-state :player] fp)
        (common/switch-view viewer))
      (prn "game-state " @(:game common/Gotit)))
    )
  )

(defroute full-island
  "/island/:target/:limit/:players/:first-player" {:as params}
  (dispatching (:target params)
               (:limit params)
               (:players params)
               (:first-player params)
               :island))

(defroute
  "/island/:target/:limit/:players" {:as params}
  (dispatching (:target params)
               (:limit params)
               (:players params)
               0
               :island))

(defroute
  "/island/:target/:limit" {:as params}
  (dispatching (:target params)
               (:limit params)
               (:players (:settings @(:game common/Gotit)))
               0
               :island))

(defroute
  "/island/:target" {:as params}
  (dispatching (:target params)
               (:limit (:settings @(:game common/Gotit)))
               (:players (:settings @(:game common/Gotit)))
               0
               :island))

(defroute
  "/island" {:as params}
  (dispatching (:target (:settings @(:game common/Gotit)))
               (:limit (:settings @(:game common/Gotit)))
               (:players (:settings @(:game common/Gotit)))
               0
               :island))


(defroute full-number
  "/number/:target/:limit/:players/:first-player" {:as params}
  (dispatching (:target params)
               (:limit params)
               (:players params)
               (:first-player params)
               :number))

(defroute
  "/number/:target/:limit/:players" {:as params}
  (dispatching (:target params)
               (:limit params)
               (:players params)
               0
               :number))
(defroute
  "/number/:target/:limit" {:as params}
  (dispatching (:target params)
               (:limit params)
               (:players (:settings @(:game common/Gotit)))
               0
               :number))

(defroute
  "/number/:target" {:as params}
  (dispatching (:target params)
               (:limit (:settings @(:game common/Gotit)))
               (:players (:settings @(:game common/Gotit)))
               0
               :number))

(defroute
  "/number" {:as params}
  (dispatching (:target params)
               (:limit (:settings @(:game common/Gotit)))
               (:players (:settings @(:game common/Gotit)))
               0
               :number))

(defroute
  "/:target/:limit/:players" {:as params}
  (dispatching (:target params)
               (:limit params)
               (:players params)
               0
               :number))

(defroute
  "/:target/:limit" {:as params}
  (dispatching (:target params)
               (:limit params)
               (:players (:settings @(:game common/Gotit)))
               0
               :number))

(defroute
  "/:target" {:as params}
  (dispatching (:target params)
               (:limit (:settings @(:game common/Gotit)))
               (:players (:settings @(:game common/Gotit)))
               0
               :number))

(defroute
  "" {:as params}
  (dispatching (:target params)
               (:limit (:settings @(:game common/Gotit)))
               (:players (:settings @(:game common/Gotit)))
               0
               :number))

(defn params->url
  "convert parameters to a url"
  [viewer target limit players first-player]
  (let [pmap {:target target :limit limit :players players :first-player first-player}]
    (if (= viewer :number)
      (full-number pmap)
      (full-island pmap)
      ))
  )

(defn save-settings
  "save settings in the url"
  []
  (let [game @(:game common/Gotit)
        settings (:settings game)
        player (:player (:play-state game))]
    (.replaceState js/history nil
                   (:title settings)
                   (params->url (:viewer settings) (:target settings)
                                (:limit settings) (:players settings)
                                (if (= 1 (:players settings)) ({:a 0 :b 1} player) 0)))))

;; history configuration.
;;
;; The invisible element "dummy" is needed to make goog.History reloadable by
;; figwheel. Without it we see
;; Failed to execute 'write' on 'Document':
;; It isn't possible to write into a document from an
;; asynchronously-loaded external script unless it is explicitly
;;
;; Note that this history handling must happen after route definitions for it
;; to kick in on initial page load.
;;
(let [h (History. false false "dummy")]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))
