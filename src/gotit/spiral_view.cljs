(ns ^:figwheel-always gotit.spiral-view
    (:require [rum.core :as rum]
              [generic.game :as game]
              [generic.util :as util]
              [generic.history :as hist]
              [generic.components :as comp]
              [generic.viewer :refer [IViewer]]
              [gotit.common :as common]
              [cljsjs.jquery :as jq]
              [cljsjs.bootstrap :as bs]
              [events.svg :as esg]))

;;;
;; ui config
;;;
(def view {:vw 620
           :vh 620
           :pad-x 80
           :pad-y 80})

(def messages {:yours "Your turn"
               :als   "My turn"
               :as-turn "Blue's turn"
               :bs-turn "Red's turn"
               :you-win "You win!"
               :al-win "Computer wins!"
               :a-win "Blue won!"
               :b-win "Red won!"
               :draw  "It's a draw!"
               })

(def colours {:a "rgb(0, 153, 255)"
              :b "rgb(238, 68, 102)"
              :none "rgb(220,255,220)"
              :draw "rgb(74, 157, 97)"
              })

(def message-colours {:yours :b
                      :als   :a
                      :as-turn :a
                      :bs-turn :b
                      :you-win :b
                      :al-win :a
                      :a-win :a
                      :b-win :b
                      :draw :draw
                      })

(def computer-think-time 2000)

;;;;;;;; Game art ;;;;;;;;

(defn pad-click [event pad-index]
  (game/player-move common/Gotit pad-index)
  )

(defn reached?
  "look in history to discover whether a play-state has been reached"
  [history play-state]
  ((set (map #(dissoc % :feedback) (:undo history))) (dissoc play-state :feedback)))

(rum/defc pads-reached-by < rum/reactive [view pads player]
  [:g
   (map
    #(let [p (esg/xy->viewport view %)]
       ;; render flag
       [:text.numb {:x (- (first p) 15)
                    :y (+  (second p) 10)
                    :font-family "FontAwesome"
                    :font-size "30"
                    :fill (player colours)
                    } "\uf041"]) ; map-marker
    (keep-indexed (fn [index point]
                    (when (reached? (rum/react hist/history) (common/PlayState.
                                        player ""  index))
                      point))
                  pads))])

(defn show-player [view pads]
  (let [play-state (:play-state @(:game common/Gotit))
        p (esg/xy->viewport view (get pads (:state play-state)))]
    [:text.numb {:x (- (first p) 10)
            :y (+  (second p) 12)
            :font-family "FontAwesome"
            :font-size "36"
            :stroke "white"
            :stroke-width 1
            :fill ((:player play-state) colours)
                 }
     ;"\uf21d"
     "\uf041"
     ] ;street-view
    ))

(defn show-target [view pads]
  (let [settings (:settings @(:game common/Gotit))
        target (:target settings)
        p (esg/xy->viewport view (pads target))]
    [:text.numb {:x (- (first p) 11.5)
            :y (+  (second p) 9)
            :font-family "FontAwesome"
            :font-size "30"
            :stroke "white"
            :stroke-width 1
            :fill "black"
            } "\uf00d"] ;x marks the spot
    ))

(defn show-numbers [view pads]
  (let [game @(:game common/Gotit)
        target (:target (:settings game))
        state (:state (:play-state game))]
    (map
     #(do
        (let [[dex p] %
              [left top] (esg/xy->viewport view p)]
          ;; render number
          [:text.numb {:x (+ 4 (if (< dex 10) (- left 6) (- left 13)))
                  :y (+ 10 (+ top 7))
                  :stroke "white"
                  :stroke-width 0.1
                  :font-size 18
                  :style {:font-weight 800}
                  :fill "black"
                  } dex]))
     (keep-indexed (fn [index point]
                     (when (or (<= index state) (= index target)) [index point])) pads)))  )

(rum/defc viewer-macro < rum/reactive []
  [:svg {:view-box (str "0 0 " (:vw view) " " (:vh view))
         :height "100%"
         :width "100%"
         :id "svg-container"
;;         :on-mouse-down esg/handle-start-line
;;         :on-mouse-move esg/handle-move-line
;;         :on-mouse-out esg/handle-out
;;         :on-mouse-up esg/handle-end-line
;;         :on-touch-start esg/handle-start-line
;;         :on-touch-move esg/handle-move-line
;;         :on-touch-end esg/handle-end-line
         }
   [:g {:transform "translate(-20, -20)"}
    (let [game (rum/react (:game common/Gotit))
          play-state (:play-state game)
          state (:state play-state)
          settings (:settings game)
          target (:target settings)
          limit (:limit settings)
          reached (:reached play-state)
          pad-count (inc target)
          pads (vec (comp/pad-spiral pad-count))]

      [:g
       ;; render sand banks
       (comp/render-pad-path view pad-count
                             0
                             target
                             {:stroke "#3366bb"
                              :stroke-width 40
                              :stroke-dasharray "15 20  5 10"
                              :stroke-linecap "round"}
                             )
       (comp/render-pad-path view pad-count
                             0
                             (min target (+ (:limit settings)
                                     state))
                             {:stroke "#0088ff"
                              :stroke-width 30
                              :stroke-linecap "round"}
                             )

       ;; render path so far
       (comp/render-pad-path view pad-count
                             0
                             state
                             {:stroke "#cc7700"
                              :stroke-width 20}
                             )

       ;; all islands
       (map-indexed #(comp/pad view %2 {:fill (cond
                                                (<= %1 state) "rgba(255, 160, 0, 0.8)"
                                                (< %1 (+ state limit 1)) "#ffcc00"
                                                :else "rgba(255, 160, 0, 0.6)")
                                        :stroke "none"
                                        :style {:pointer-events (if (and (> %1 state) (< %1 (+ state limit 1))) "auto" "none")}
                                        :n %1} (fn [event] (pad-click event %1))) pads)

       ;; Target Cross
       (show-target view pads)

       ;; islands reached by blue
       ;(pads-reached-by view pads :b)

       ;; islands reached by red
       ;(pads-reached-by view pads :a)

       ;; Current position of player
       (show-player view pads)

       ;; Number islands
       ;(show-numbers view pads)

       ])]
   ])


(rum/defc help < rum/reactive []
  [:div {:style {:padding "20px"}}
   [:.alert.alert-info
    "On your turn you can build up to "
    [:b (:limit (:settings (rum/react (:game common/Gotit)))) " bridges"]
    " over the shallows by "
    [:b " tapping the yellow island you want to reach."]
    " Be the first to reach the treasure marked with a cross. "]])

(defrecord Spiral-view []
  IViewer
  (get-message [this status]
    (status messages))

  (get-fill [this status]
    ((status message-colours) colours))

  (game-viewer [this config] (viewer-macro))

  (help-viewer [this]
    (help))

  )
