(ns middleware.utils.async
  #?(:clj (:require [clojure.core.async])
     :cljs (:require-macros [middleware.utils.async])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; NOTE(Richo): The following code comes from Alexander Kiel's library async-error
; (https://github.com/alexanderkiel/async-error).
; I decided to include the source code here instead of as a dependency because I
; wanted to change it slightly in order to handle throwing any object in cljs.

;; ---- Helpers Taken from Prismatic Schema -----------------------------------

#?(:clj
   (defn cljs-env?
     "Take the &env from a macro, and tell whether we are expanding into cljs."
     [env]
     (boolean (:ns env))))

#?(:clj
   (defmacro if-cljs
     "Return then if we are generating cljs code and else for Clojure code.
      https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ"
     [then else]
     (if (cljs-env? &env) then else)))

;; ---- Helpers ---------------------------------------------------------------

(defn throw-err [e]
  (when (instance? #?(:clj Throwable :cljs js/Error) e) (throw e))
  e)

;; ---- Public API ------------------------------------------------------------

#?(:clj
   (defmacro <?
     "Like <! but throws errors."
     [ch]
     `(if-cljs
        (throw-err (cljs.core.async/<! ~ch))
        (throw-err (clojure.core.async/<! ~ch)))))

#?(:clj
   (defn <??
     "Like <!! but throws errors."
     [ch]
     (throw-err (clojure.core.async/<!! ch))))

#?(:clj
   (defmacro go-try
     "Like go but catches the first thrown error and returns it."
     [& body]
     `(if-cljs
        (cljs.core.async/go
         (try
           ~@body
           (catch :default e#
             (if (instance? js/Error e#)
               e#
               (js/Error. "ERROR" (cljs.core/js-obj "cause" e#))))))
        (clojure.core.async/go
         (try
           ~@body
           (catch Throwable t# t#))))))
