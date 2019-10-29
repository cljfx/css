(ns e01-app-styling
  (:require [cljfx.css :as css]
            [clojure.pprint :as pprint]
            [cljfx.api :as fx]))

(def style
  (css/register ::style
    (let [base-color "#222"
          palette {:app.palette/text base-color
                   :app.palette/help (str base-color "8")
                   :app.palette/border (str base-color "4")
                   :app.palette/shadow (str base-color "3")
                   :app.palette/focus (str base-color "8")
                   :app.palette/control "#fff"
                   :app.palette/control-hover "#f4f4f4"
                   :app.palette/background "#eee"}
          layout {:app.layout/spacing 10
                  :app.layout/scroll-bar-size 9
                  :app.layout/padding 20
                  :app.layout/corner 5
                  :app.layout/label-padding "2px 4px"}
          text (fn [size weight]
                 {:-fx-text-fill (:app.palette/text palette)
                  :-fx-wrap-text true
                  :-fx-font-weight weight
                  :-fx-font-size size})
          control-shadow (format "dropshadow(gaussian, %s, 5, 0, 0, 1)"
                                 (:app.palette/shadow palette))
          inner-shadow (format "innershadow(gaussian, %s, 5, 0, 0, 2)"
                               (:app.palette/shadow palette))
          hover-shadow (format "dropshadow(gaussian, %s, 7, 0, 0, 2)"
                               (:app.palette/shadow palette))
          armed-shadow (format "dropshadow(gaussian, %s, 3, 0, 0, 1)"
                               (:app.palette/shadow palette))
          border {:-fx-border-color (:app.palette/border palette)
                  :-fx-background-color (:app.palette/control palette)
                  :-fx-border-radius (:app.layout/corner layout)
                  :-fx-background-radius (:app.layout/corner layout)}
          button (merge
                   (text 13 :normal)
                   border
                   {:-fx-padding (:app.layout/label-padding layout)
                    :-fx-effect control-shadow
                    ":focused" {:-fx-border-color (:app.palette/focus palette)}
                    ":hover" {:-fx-effect hover-shadow
                              :-fx-background-color (:app.palette/control-hover palette)}
                    ":armed" {:-fx-effect armed-shadow}})]
      (merge
        palette
        layout
        {".app-" {"label" (text 13 :normal)
                  "header" (text 20 :bold)
                  "sub-header" (text 16 :bold)
                  "code" (merge
                           (text 13 :normal)
                           {:-fx-font-family "monospace"
                            :-fx-padding (:app.layout/padding layout)})
                  "container" {:-fx-spacing (:app.layout/spacing layout)}
                  "root" {:-fx-padding (:app.layout/padding layout)
                          :-fx-background-color (:app.palette/background palette)}
                  "button-" {"primary" button
                             "secondary" button}
                  "check-box" {:-fx-text-fill (:app.palette/text palette)
                               :-fx-label-padding (format "0 0 0 %spx"
                                                          (:app.layout/spacing layout))
                               ":focused > .box" {:-fx-border-color (:app.palette/focus palette)}
                               ":hover > . box" {:-fx-effect hover-shadow
                                                 :-fx-background-color (:app.palette/control-hover palette)}
                               ":armed > .box" {:-fx-effect armed-shadow}
                               "> .box" (merge
                                          border
                                          {:-fx-effect control-shadow
                                           :-fx-padding "3px 2px"
                                           "> .mark" {:-fx-padding "5px 6px"
                                                      :-fx-shape "'M7.629,14.566c0.125,0.125,0.291,0.188,0.456,0.188c0.164,0,0.329-0.062,0.456-0.188l8.219-8.221c0.252-0.252,0.252-0.659,0-0.911c-0.252-0.252-0.659-0.252-0.911,0l-7.764,7.763L4.152,9.267c-0.252-0.251-0.66-0.251-0.911,0c-0.252,0.252-0.252,0.66,0,0.911L7.629,14.566z'"}})
                               ":selected > .box > .mark" {:-fx-background-color (:app.palette/text palette)}}
                  "text-field" (merge
                                 (text 13 :normal)
                                 border
                                 {:-fx-highlight-fill (:app.palette/text palette)
                                  :-fx-padding (:app.layout/label-padding layout)
                                  :-fx-prompt-text-fill (:app.palette/help palette)
                                  :-fx-highlight-text-fill (:app.palette/background palette)
                                  :-fx-effect inner-shadow
                                  ":focused" {:-fx-border-color (:app.palette/focus palette)}})}
         ".scroll-pane" (merge
                          border
                          {:-fx-effect inner-shadow
                           :-fx-focus-traversable true
                           ":focused" {:-fx-border-color (:app.palette/focus palette)
                                       :-fx-background-insets 0}
                           "> .viewport" {:-fx-background-color (:app.palette/control palette)}
                           "> .corner" {:-fx-background-color :transparent}})
         ".scroll-bar" {:-fx-background-color :transparent
                        "> .thumb" {:-fx-background-color (:app.palette/focus palette)
                                    :-fx-background-radius (:app.layout/scroll-bar-size layout)
                                    :-fx-background-insets 1
                                    ":pressed" {:-fx-background-color (:app.palette/text palette)}}
                        ":horizontal" {"> .increment-button > .increment-arrow" {:-fx-pref-height (:app.layout/scroll-bar-size layout)}
                                       "> .decrement-button > .decrement-arrow" {:-fx-pref-height (:app.layout/scroll-bar-size layout)}}
                        ":vertical" {"> .increment-button > .increment-arrow" {:-fx-pref-width (:app.layout/scroll-bar-size layout)}
                                     "> .decrement-button > .decrement-arrow" {:-fx-pref-width (:app.layout/scroll-bar-size layout)}}
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
  (add-watch #'style :render (fn [_ _ _ _] (swap! *state assoc :style style)))
  ;; ... and remove it when you are done
  (remove-watch #'style :render))

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