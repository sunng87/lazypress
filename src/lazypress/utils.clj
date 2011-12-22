(ns lazypress.utils
  (:use [clojure.string :only [join]])
  (:use [clojure.data.json :only [json-str]]))

(def ^{:private true} base62-seed "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
(defn base62 [n]
  (loop [x n r (list)]
    (if (< x 62)
      (join "" (conj r (nth base62-seed x)))
      (recur (long (/ x 62)) (conj r (nth base62-seed (mod x 62)))))))

(defn json-response [d c]
  {:headers
   {"Content-Type" (if (nil? c)
                     "application/json" "text/javascript")}
   :body (if (nil? c)
           (json-str d)
           (str c "(" (json-str d) ");"))})

