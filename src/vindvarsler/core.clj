(ns vindvarsler.core
  (:require [clojure.xml :as xml])
  (:require [clojure.core.cache :as cache]))


(def forecast-url "http://www.yr.no/sted/Norge/S%C3%B8r-Tr%C3%B8ndelag/Trondheim/Trondheim/varsel.xml")

;; Cache with 10 minutes time-to-live
(def my-cache (atom (cache/ttl-cache-factory {} :ttl 600000)))

(defn get-weatherdata
  [url]
  (do
    (if (cache/has? @my-cache :data)
      (swap! my-cache #(cache/hit % :data))
      (swap! my-cache #(cache/miss % :data (xml/parse url))))
    (cache/lookup @my-cache :data)))

(defn get-windspeed
  [time-element]
  (->> time-element
       :content
       (filter #(= (:tag %) :windSpeed))
       first
       :attrs
       :mps
       Double/parseDouble))

(defn get-windspeeds
  [weatherdata]
  (->> weatherdata
       :content
       (filter #(= (:tag %) :forecast))
       first
       :content
       (filter #(= (:tag %) :tabular))
       first
       :content
       (map get-windspeed)))

(defn strong-breeze-or-more?
  [windspeed]
  (> windspeed 10.8))

(defn strong-wind-in-forecast?
  [windspeeds]
  (reduce
   (fn [acc x] (and acc x))
   (map strong-breeze-or-more? windspeeds)))

(defn gale?
  []
  (strong-wind-in-forecast?
   (get-windspeeds
    (get-weatherdata
     forecast-url))))
