(ns e01-instant-restyling
  (:require [cljfx.css :as css]
            [clojure.pprint :as pprint]
            [cljfx.api :as fx]))

(def style
  (css/register ::style
    (let [base-color "#222"
          style {:app.style/text-color base-color
                 :app.style/help-color (str base-color "8")
                 :app.style/border-color (str base-color "4")
                 :app.style/shadow-color (str base-color "3")
                 :app.style/focus-color (str base-color "8")
                 :app.style/control-color "#fff"
                 :app.style/control-hover-color "#f4f4f4"
                 :app.style/background-color "#eee"
                 :app.style/spacing 10
                 :app.style/scroll-bar-size 9
                 :app.style/padding 20
                 :app.style/corner-size 5
                 :app.style/label-padding "2px 4px"}
          text (fn [size weight]
                 {:-fx-text-fill (:app.style/text-color style)
                  :-fx-wrap-text true
                  :-fx-font-weight weight
                  :-fx-font-size size})
          control-shadow (format "dropshadow(gaussian, %s, 5, 0, 0, 1)"
                                 (:app.style/shadow-color style))
          inner-shadow (format "innershadow(gaussian, %s, 5, 0, 0, 2)"
                               (:app.style/shadow-color style))
          hover-shadow (format "dropshadow(gaussian, %s, 7, 0, 0, 2)"
                               (:app.style/shadow-color style))
          armed-shadow (format "dropshadow(gaussian, %s, 3, 0, 0, 1)"
                               (:app.style/shadow-color style))
          border {:-fx-border-color (:app.style/border-color style)
                  :-fx-background-color (:app.style/control-color style)
                  :-fx-border-radius (:app.style/corner-size style)
                  :-fx-background-radius (:app.style/corner-size style)}
          button (merge
                   (text 13 :normal)
                   border
                   {:-fx-padding (:app.style/label-padding style)
                    :-fx-effect control-shadow
                    ":focused" {:-fx-border-color (:app.style/focus-color style)}
                    ":hover" {:-fx-effect hover-shadow
                              :-fx-background-color (:app.style/control-hover-color style)}
                    ":armed" {:-fx-effect armed-shadow}})]
      (merge
        style
        {".app-" {"label" (text 13 :normal)
                  "header" (text 20 :bold)
                  "sub-header" (text 16 :bold)
                  "code" (merge
                           (text 13 :normal)
                           {:-fx-font-family "monospace"
                            :-fx-padding (:app.style/spacing style)})
                  "container" {:-fx-spacing (:app.style/spacing style)}
                  "root" {:-fx-padding (:app.style/padding style)
                          :-fx-background-color (:app.style/background-color style)}
                  "button-" {"primary" button
                             "secondary" button}
                  "check-box" {:-fx-text-fill (:app.style/text-color style)
                               :-fx-label-padding (format "0 0 0 %spx"
                                                          (:app.style/spacing style))
                               ":focused > .box" {:-fx-border-color (:app.style/focus-color style)}
                               ":hover > . box" {:-fx-effect hover-shadow
                                                 :-fx-background-color (:app.style/control-hover-color style)}
                               ":armed > .box" {:-fx-effect armed-shadow}
                               "> .box" (merge
                                          border
                                          {:-fx-effect control-shadow
                                           :-fx-padding "3px 2px"
                                           "> .mark" {:-fx-padding "5px 6px"
                                                      :-fx-shape "'M7.629,14.566c0.125,0.125,0.291,0.188,0.456,0.188c0.164,0,0.329-0.062,0.456-0.188l8.219-8.221c0.252-0.252,0.252-0.659,0-0.911c-0.252-0.252-0.659-0.252-0.911,0l-7.764,7.763L4.152,9.267c-0.252-0.251-0.66-0.251-0.911,0c-0.252,0.252-0.252,0.66,0,0.911L7.629,14.566z'"}})
                               ":selected > .box > .mark" {:-fx-background-color (:app.style/text-color style)}}
                  "text-field" (merge
                                 (text 13 :normal)
                                 border
                                 {:-fx-highlight-fill (:app.style/text-color style)
                                  :-fx-padding (:app.style/label-padding style)
                                  :-fx-prompt-text-fill (:app.style/help-color style)
                                  :-fx-highlight-text-fill (:app.style/background-color style)
                                  :-fx-effect inner-shadow
                                  ":focused" {:-fx-border-color (:app.style/focus-color style)}})}
         ".scroll-pane" (merge
                          border
                          {:-fx-effect inner-shadow
                           :-fx-focus-traversable true
                           ":focused" {:-fx-border-color (:app.style/focus-color style)
                                       :-fx-background-insets 0}
                           "> .viewport" {:-fx-background-color (:app.style/control-color style)}
                           "> .corner" {:-fx-background-color :transparent}})
         ".scroll-bar" {:-fx-background-color :transparent
                        "> .thumb" {:-fx-background-color (:app.style/focus-color style)
                                    :-fx-background-radius (:app.style/scroll-bar-size style)
                                    :-fx-background-insets 1
                                    ":pressed" {:-fx-background-color (:app.style/text-color style)}}
                        ":horizontal" {"> .increment-button > .increment-arrow" {:-fx-pref-height (:app.style/scroll-bar-size style)}
                                       "> .decrement-button > .decrement-arrow" {:-fx-pref-height (:app.style/scroll-bar-size style)}}
                        ":vertical" {"> .increment-button > .increment-arrow" {:-fx-pref-width (:app.style/scroll-bar-size style)}
                                     "> .decrement-button > .decrement-arrow" {:-fx-pref-width (:app.style/scroll-bar-size style)}}
                        "> .decrement-button" {:-fx-padding 0
                                               "> .decrement-arrow" {:-fx-shape nil
                                                                     :-fx-padding 0}}
                        "> .increment-button" {:-fx-padding 0
                                               "> .increment-arrow" {:-fx-shape nil
                                                                     :-fx-padding 0}}}}))))

(def *state
  (atom {:check-box true
         :style style}))

(comment
  ;; to iterate during development on style, add a watch to var that updates style in app
  ;; state...
  (add-watch #'style :refresh-app (fn [_ _ _ _] (swap! *state assoc :style style)))
  ;; ... and remove it when you are done
  (remove-watch #'style :refresh-app))

(def renderer
  (fx/create-renderer
    :middleware
    (fx/wrap-map-desc
      (fn [{:keys [check-box style]}]
        {:fx/type :stage
         :showing true
         :width 900
         :height 600
         :scene {:fx/type :scene
                 :stylesheets [(::css/url style)]
                 :root {:fx/type :v-box
                        :style-class ["app-root" "app-container"]
                        :children [{:fx/type :label
                                    :style-class "app-header"
                                    :text "Header"}
                                   {:fx/type :label
                                    :style-class "app-label"
                                    :text "label with some text"}
                                   {:fx/type :h-box
                                    :style-class "app-container"
                                    :children [{:fx/type :button
                                                :style-class "app-button-primary"
                                                :text "First Button"}
                                               {:fx/type :button
                                                :style-class "app-button-secondary"
                                                :text "Second Button"}]}
                                   {:fx/type :text-field
                                    :style-class "app-text-field"
                                    :prompt-text "type here something"}
                                   {:fx/type :label
                                    :style-class "app-sub-header"
                                    :text "css"}
                                   {:fx/type :check-box
                                    :style-class "app-check-box"
                                    :text "As text"
                                    :selected check-box
                                    :on-selected-changed #(swap! *state assoc :check-box %)}
                                   {:fx/type :scroll-pane
                                    :content {:fx/type :label
                                              :style-class "app-code"
                                              :text (with-out-str
                                                      (if check-box
                                                        (println (slurp (::css/url style)))
                                                        (pprint/pprint style)))}}]}}}))))

(fx/mount-renderer *state renderer)
