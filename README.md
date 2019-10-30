# cljfx / css

WIP, do not use yet

[![Cljdoc documentation](https://cljdoc.org/badge/cljfx/css)](https://cljdoc.org/jump/release/cljfx/css) 
[![Clojars Project](https://img.shields.io/clojars/v/cljfx/css.svg)](https://clojars.org/cljfx/css)

Charmingly Simple Styling for [cljfx](https://github.com/cljfx/cljfx)

## Rationale

JavaFX is designed to use CSS files for styling. CSS has it's own set of problems such as 
selectors unexpectedly overriding each other and having unclear priority. Because of that, 
inline styles are more predictable and, with cljfx, where styles can be described as maps, 
also more composable.

Unfortunately, CSS is unavoidable, because controls don't provide access to their internal 
nodes, and they can be targeted only with CSS selectors. What's worse, JavaFX does not 
allow loading CSS from strings or some other data structures, instead expecting an URL 
pointing to a CSS file. In addition to that, CSS is not always enough for styling JavaFX 
application: not every Node is styleable (for example, Shapes aren't). All this leads to
slow iteration cycle on styling and also to duplication of styling information in CSS and 
code.

Charmingly Simple Styling is a library and a set of recommendations that solve these 
problems. Library provides a way to configure application style using clojure data 
structures and then construct special URLs to load CSS for styling JavaFX nodes that is 
derived from the same data structures. Recommendations help setup cljfx application in 
a way that allows you to rapidly iterate on styling in a live app and keep some sanity in 
the world of CSS.

## Installation and requirements

Latest version on Clojars:

[![cljfx/css](https://clojars.org/cljfx/css/latest-version.svg)](https://clojars.org/cljfx/css)

Charmingly Simple Styling does not depend on cljfx itself, so it can be used in any JavaFX
application built with Clojure.

## Library overview

You want to create style description, both usable from code and loadable as CSS from URL. 
To achieve that, Charmingly Simple Styling extends JVM URLs with custom protocol — 
`cljfx-css` — that loads CSS from globally-registered style maps. CSS is generated by 
recursively concatenating all string keys in a style map to construct selectors, at the
same time using keyword keys for associated selectors to construct rules.  

Let's see how it looks with this walk-through:
```clj
(ns my-app.style
  (:require [cljfx.css :as css]))

(def style
  (css/register ::style
    (let [padding 10
          text-color "#111111"]
      ;; you can put style settings that you need to access from code at keyword keys in a
      ;; style map and access them directly in an app
      {::padding padding
       ::text-color text-color
       ;; string key ".root" defines `.root` selector with these rules: `-fx-padding: 10;`
       ".root" {:-fx-padding padding}
       ".label" {:-fx-text-fill text-color
                 :-fx-wrap-text true}
       ".button" {:-fx-text-fill text-color
                  ;; vector values are space-separated
                  :-fx-padding ["4px" "8px"]
                  ;; nested string key defines new selector: `.button:hover`
                  ":hover" {:-fx-text-fill :black}}})))

;; `css/register` registers this style map globally so it can be loaded by URL, and puts
;; URL string in a style map at `:cljfx.css/url` key.
style
=> {:my-app.style/padding 10,
    :my-app.style/text-color "#111111",
    ".root" {:-fx-padding 10},
    ".label" {:-fx-text-fill "#111111", :-fx-wrap-text true},
    ".button" {:-fx-text-fill "#111111",
               :-fx-padding ["4px" "8px"],
               ":hover" {:-fx-text-fill :black}},
    ;; URL has stringified version of keyword in query part of URL, and a hash of a style 
    ;; map in a fragment part. Query part is used to lookup style map in a global 
    ;; registry, and fragment is used to indicate that style is changed when it's 
    ;; redefined to trigger CSS reload in JavaFX
    :cljfx.css/url "cljfx-css:?my-app.style/style#-1561130535"}

;; let's see how loaded CSS looks like:
(println (slurp (::css/url style)))
;; prints:
;; .root {
;;   -fx-padding: 10;
;; }
;; .label {
;;   -fx-text-fill: #111111;
;;   -fx-wrap-text: true;
;; }
;; .button {
;;   -fx-text-fill: #111111;
;;   -fx-padding: 4px 8px;
;; }
;; .button:hover {
;;   -fx-text-fill: black;
;; }

;; Later, in app description:
{:fx/type :stage
 :showing true 
 :scene {:fx/type :scene
         :stylesheets [(::css/url style)]
         :root ...}}
```

That's it: you define styles, register them and feed constructed URL to JavaFX. 

## Recommendations

### Don't rely on priority rules

CSS has confusing priority rules, which, when relied upon, usually results in CSS files 
becoming append only with more and more overrides. In Charmingly Simple Styling, on the 
other hand, style maps are unordered, which means resulted CSS selectors are emitted in 
undefined order. That's made intentionally to promote a more reasonable approach: create 
different CSS classes for different purposes and then switch between them.

For example, instead of this:
```clojure
;; BAD!

;; style map:
{".notification" {:-fx-background-color :black
                  "> .label" {:-fx-text-fill :gray}}
 ".danger > .label" {:-fx-text-fill :red}}

;; component:
(defn notification [{:keys [text variant]
                     :or {variant "info"}}]
  {:fx/type :v-box
   :style-class ["notification" variant]
   :children [{:fx/type :label 
               :text text}]})
```
You should use this:
```clojure
;; GOOD!

;; style map:
{".notification" {:-fx-background-color :black
                  "-label" {"-info" {:-fx-text-fill :gray}
                            "-danger" {:-fx-text-fill :red}}}}

;; component:
(defn notification [{:keys [text variant]
                     :or {variant "info"}}]
  {:fx/type :v-box
   :style-class "notification"
   :children [{:fx/type :label
               :style-class (str "notification-label-" variant)
               :text text}]})
```

### Watch for changes while iterating on styles

Usually styles are static during the application runtime, but when you develop application
styling, it's very important to see your changes immediately. To achieve that with 
Charmingly Simple Styling, you need to take 2 steps:
- put registered style into application state, so re-registered style can be picked up on
  next render;
- watch for changes in registered style and update it in app state.

When putting style in an app state, it might be useful to also put it into component 
environment with `fx/ext-set-env`, so you can access it easily. See 
[`ext-set-env`/`ext-get-env` section](https://github.com/cljfx/cljfx#extending-cljfx) in 
cljfx's manual. 

When you keep style `def`ed in a Var, you can just add a watch to that var that updates
style in an app state to achieve instant reload. 
See [example](examples/e01_instant_restyling.clj) — it contains a style definition and 
a rich comment that you can use to start and stop watching for changes in style to 
instantly reapply styles in an app.  

There are also 2 resources I found very important when developing application styling:
- Official JavaFX [CSS reference](https://openjfx.io/javadoc/12/javafx.graphics/javafx/scene/doc-files/cssref.html) —
  to see what you can style with CSS
- [modena.css](https://gist.github.com/maxd/63691840fc372f22f470) — default CSS used by 
  JavaFX, helpful when documentation is not enough

### Be careful with indirect children CSS selector

TODO
https://wiki.openjdk.java.net/display/OpenJFX/Performance+Tips+and+Tricks
CSS:
- Avoid selectors that have to match against the entire set of parents
- Use stylesheets not setStyles
- Use pseudo-class state, not multiple style classes, for state-based styles (FX 8)

### Prefer custom classes instead of JavaFX ones

TODO

## License

TODO
