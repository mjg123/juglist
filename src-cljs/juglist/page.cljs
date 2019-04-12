(ns juglist.page
  (:require [juglist.data :as d]
            [hiccups.runtime :as hiccupsrt]
            [cljsjs.leaflet])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))

(enable-console-print!)

(defonce twitter-default-icon "http://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png")
(defonce jug-default-icon "https://pbs.twimg.com/profile_images/1936731219/2873c9d_normal.png")

(defonce jug-map (.map js/L "map" (clj->js {:center [30 0] :zoom 3})))
(defonce tile-layer (-> js/L
                        (.tileLayer "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png")
                        (.addTo jug-map)))
(defonce marker-layer (-> js/L
                          .layerGroup
                          (.addTo jug-map)))

(.clearLayers marker-layer);; prevents marker mania while hot reloading

(when (.-geolocation js/navigator)
  (-> js/navigator
      (.-geolocation)
      (.getCurrentPosition (fn [posn]
                             (let [lat (-> posn .-coords .-latitude)
                                   lon (-> posn .-coords .-longitude)]
                               (-> jug-map
                                   (.setView (clj->js [lat lon]) 7)))))))
(defn ensure-https [url]
  (if (clojure.string/starts-with? url "http://")
    (do
      (println "Found http url: " url)
      (str "https://" (subs url 7)))
    (do
      (println "nice - it was already https: " url)
      url)))

(defn create-icon [iconUrl]
  (let [iconUrl (if (= iconUrl twitter-default-icon)
                  jug-default-icon
                  iconUrl)]
    (-> js/L
        (.icon (clj->js {:iconUrl (ensure-https iconUrl)
                         :iconSize [48 48]
                         :iconAnchor [24 48]
                         :popupAnchor [0 -48]
                         :className "jug-icon"})))))

(defn create-description [{:keys [name description screen_name expanded_url]}]
  (html [:div.popup
         [:h2 name]
         [:div.desc description]
         [:div
          [:label "Twitter "]
          [:a {:href (str "https://twitter.com/" screen_name) :target "_blank"} "@" screen_name]]
         (when expanded_url
           [:div
            [:label "Website "]
            [:a {:href expanded_url :target "_blank"} expanded_url]])]))

(defn add-marker [jug]
  (-> js/L
      (.marker (clj->js ((juxt :lat :lon) (:coords jug)))
               (clj->js {:icon (create-icon (:profile_image_url jug))}))
      (.addTo jug-map)
      (.bindPopup (create-description jug))))


(dorun
 (map add-marker (vals d/jug-data)))

