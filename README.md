# cljfx / css

WIP, do not use yet

[![Cljdoc documentation](https://cljdoc.org/badge/cljfx/css)](https://cljdoc.org/jump/release/cljfx/css) 
[![Clojars Project](https://img.shields.io/clojars/v/cljfx/css.svg)](https://clojars.org/cljfx/css)

Charmingly Simple Styling for [cljfx](https://github.com/cljfx/cljfx)

## Rationale

JavaFX is designed to use CSS files for styling. CSS has it's own set of problems such as 
selectors unexpectedly overriding each other and having unclear priority. Because of that, 
using inline styles is much more predictable, and with cljfx, where styles can be 
described as maps, also composable.

Unfortunately, CSS is unavoidable, because controls don't provide access to their internal 
nodes, and they can be targeted only with CSS selectors. What's worse, JavaFX does not 
allow loading CSS from strings or some other data structures, instead expecting an URL 
pointing to a CSS file. This leads to slow iteration cycle on styling and also to 
duplication of styling information in CSS and code.

Charmingly Simple Styling is a solution to these problems: it provides a way to configure 
application style using clojure data structures, while also allowing to construct special 
URLs that contain CSS information for styling JavaFX nodes, derived from the same data 
structures.

## Installation and requirements

Latest version on Clojars:

[![cljfx/css](https://clojars.org/cljfx/css/latest-version.svg)](https://clojars.org/cljfx/css)

## Overview

### Creating style

Let's start with an example:
```clj
 
```