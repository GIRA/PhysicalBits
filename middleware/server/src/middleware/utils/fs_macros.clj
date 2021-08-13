(ns middleware.utils.fs-macros
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

;; HACK(Richo): I wrote this macro because I'm lazy and I didn't want to refactor the
;; compiler to work with asynchronous code.
;; The problem I have is that the linker needs access to the file system in order to
;; read the imported libraries. However, when running in the browser I don't have direct
;; access to the file system. Instead I need to go through an http request. That should
;; be straightforward but the async nature of javascript makes it a little annoying.
;; And for this to work correctly, I'll need to change the entire compiler code to make
;; it asynchronous as well, which I don't really feel like doing right now.
;; So as a *temporary* solution I wrote this macro that reads all the libraries at compile
;; time and leaves them stored in the code. Since clojurescript macros run in the context
;; of the JVM, I can access the filesystem without problems. This way the compiler can keep
;; its sync implementation.
;; I know, it's an ugly hack and I will fix it soon, but for now, it seems to work.
(defmacro read-file* [path root]
  (let [data (into {} (map (fn [f] [(str/replace (.getPath f) "\\" "/") (slurp f)])
                           (filter (memfn isFile)
                                   (file-seq (io/file root)))))]
    `(get ~data ~path nil)))
