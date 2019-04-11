(ns juglist.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

;;;;;; Consts and setup

(def TWITTER_DATA "cache/twitter-list.json")
(def GEOCODING_DATA "cache/geocodes.edn")

(clojure.java.io/make-parents TWITTER_DATA)
(clojure.java.io/make-parents GEOCODING_DATA)


;;;;;; Util fns

(defn cache-slurp [file]
  (try
    (slurp file)
    (catch java.io.FileNotFoundException e nil)))

(defn log [msg]
  (println msg))

(defn map* "inverted arg order for easier use with threading"
  [xs f]
  (map f xs))

(defn select-keys*
  [m paths]
  (into {} (map (fn [p]
                  [(last p) (get-in m p)]))
        paths))

(defn map-vals [m f]
  (into {} (for [[k v] m] [k (f v)])))

;;;;;; Dealing with Twitter

(defn fetch-twitter-list-data [bearer-token]
  (:body
   (client/get "https://api.twitter.com/1.1/lists/members.json"
               {:query-params {:owner_screen_name "brunoborges"
                               :slug "jugs"
                               :count 1000
                               :cursor -1}
                :headers {:authorization
                          (str "Bearer " bearer-token)}})))

(defn twitter-list-data []
  (->
   (if-let [cached-twitter-data (cache-slurp TWITTER_DATA)]
     (do (log "Using cached twitter data")
         cached-twitter-data)

     (do (log "Downloading list data from twitter")
         (if-let [bearer-token (System/getenv "TWITTER_BEARER_TOKEN")]
           (let [twitter-json (fetch-twitter-list-data bearer-token)]
             (spit TWITTER_DATA twitter-json)
             twitter-json)
           (throw (Exception. "You need to set TWITTER_BEARER_TOKEN")))))
   (json/read-str :key-fn keyword)))


;;;;;; Transforming and Geocoding

(defn twitter->jug [jug]
  (select-keys* jug [[:name]
                     [:screen_name]
                     [:description]
                     [:location]
                     [:entities :url :urls 0 :expanded_url]
                     [:profile_image_url]]))

(defn mapify-jugs
  "Transform from a seq into a map from screen_name to jug"
  [jugs]
  (reduce #(assoc %1 (-> %2 :screen_name clojure.string/lower-case keyword) %2) {} jugs))

(defn fix-nongeocodable-locations [jugs]
  (-> jugs
      ;; Remove ones that don't have a physical location
      ;;  (or I can't work it out)
      ;;  (or a JUG has 2 accounts in the same location)
      (dissoc :javahispano)
      (dissoc :ukjugs)
      (dissoc :virtualjug)
      (dissoc :eventosjespanol)
      (dissoc :jug_arg)
      (dissoc :jugsuldeminas)
      (dissoc :jmancunconf)
      
      ;; Change (or more likely, add)  a location
      (assoc-in [:moroccojug :location] "Rabat")
      (assoc-in [:jjug :location] "Tokyo")
      (assoc-in [:jugvadodara :location] "Vadodara")
      (assoc-in [:punejug :location] "Pune")
      (assoc-in [:ejug :location] "Linz")
      (assoc-in [:caririjug :location] "Cariri")
      (assoc-in [:izmirjava :location] "Izmir") ;;
      (assoc-in [:izmirjug :location] "Izmir")  ;; two of these?
      (assoc-in [:jugsaar :location] "Saarland")
      (assoc-in [:beirajug :location] "beira")
      (assoc-in [:phillyjug :location] "Philadelphia")
      (assoc-in [:illinoisjug :location] "Illinois")
      (assoc-in [:coimbrajug :location] "Coimbra")
      (assoc-in [:medellinjug :location] "Medellin")
      (assoc-in [:jugmumbai :location] "Mumbai")
      (assoc-in [:javaugbosnia :location] "Bosnia")
      (assoc-in [:nigeriajug :location] "Nigeria")
      (assoc-in [:cjava_peru :location] "Lima")
      (assoc-in [:denverjug :location] "Denver")
      (assoc-in [:maritimes_jug :location] "Cape Tormentine")
      (assoc-in [:algeriajug :location] "Algeria")
      (assoc-in [:doagjava :location] "Berlin")
      (assoc-in [:grjug :location] "Grand Rapids")
      (assoc-in [:jug_de :location] "Germany")
      (assoc-in [:cinjug :location] "Cincinnati")
      (assoc-in [:bakujug :location] "Baku")
      (assoc-in [:jugpy :location] "Paraguay")
      (assoc-in [:poitoujug :location] "Poitou-Charentes")
      (assoc-in [:clojug :location] "Colombia")
      (assoc-in [:campinasjug :location] "Campinas")
      (assoc-in [:mgjug :location] "Minas Gerais")
      (assoc-in [:jugru :location] "Russia")
      (assoc-in [:nejug :location] "New England")  
      (assoc-in [:java_ce :location] "Ceará")
      (assoc-in [:sertaojug :location] "Sertao")  
      (assoc-in [:jugsardegna :location] "Sardegna")
      (assoc-in [:javacro :location] "Zagreb")
      (assoc-in [:jkpgjug :location] "Jönköping")
      (assoc-in [:qjug :location] "Queensland")
      (assoc-in [:hjug :location] "Houston")
      (assoc-in [:dalajug :location] "Dalarna, Sweden")
      (assoc-in [:javaaltovale :location] "Itajai")
      (assoc-in [:jugcbe :location] "Coimbatore")
      (assoc-in [:caceresjug :location] "Caceres")
      (assoc-in [:soujava :location] "Rio de Janeiro")
      (assoc-in [:javabin :location] "Oslo")
  
      ;; Accounts covering more than one location
      (assoc :moroccojug2 (assoc (jugs :moroccojug) :location "Casablanca"))
      (assoc :doagjava2   (assoc (jugs :doagjava)   :location "Duisburg"))
      (assoc :doagjava3   (assoc (jugs :doagjava)   :location "Nürnberg"))
      (assoc :soujava2    (assoc (jugs :soujava)    :location "Sao Paulo"))
      (assoc :javabin2    (assoc (jugs :javabin)    :location "Bergen, Norway"))
      (assoc :javabin3    (assoc (jugs :javabin)    :location "Stavanger, Norway"))
      (assoc :javabin4    (assoc (jugs :javabin)    :location "Sørlandet, Norway"))
      (assoc :javabin5    (assoc (jugs :javabin)    :location "Trondheim, Norway"))
      (assoc :javabin6    (assoc (jugs :javabin)    :location "Vestfold, Norway"))
      (assoc :javabin7    (assoc (jugs :javabin)    :location "Sogn, Norway"))
      ))


((juxt :b :a) {:a 1 :b 200
               })

(def geocodes-cache (or
                     (clojure.edn/read-string (cache-slurp GEOCODING_DATA))
                     {}))

(defn ->latlon [location]
  (if-let [latlon (geocodes-cache location)]
    (do (log (str "Using cache for " location " :: " latlon))
        latlon)
    (do (log (str "Fetching geocode from Nominatim for " location))
        (->
         (str "https://nominatim.openstreetmap.org/?format=json&addressdetails=1&q="
              location
              "&format=json&limit=1")
         client/get
         :body
         (json/read-str :key-fn keyword)
         first
         (select-keys [:lat :lon])))))

(defn geocode [jug]
  (let [coords (->latlon (:location jug))]
    (if (= coords {})
      (println "Geocoding failed for"
               (:screen_name jug)
               (str " \"" (:location jug) "\""))
      coords)))

(defn cache-geocoding-results
  "Caches into the GEOCODING_DATA file like {loc {:lat x :lon y}}"
  [jugs]
  (let [geocode-cache (into {} (remove
                                #(= {} (second %))
                                (map #(vector (:location %) (:coords %))
                                     (vals jugs))))]
    
    (spit GEOCODING_DATA (prn-str geocode-cache))))

(defn add-lat-lons [jugs]
  (let [geocoded (map-vals jugs #(assoc % :coords (geocode %)))]
    (cache-geocoding-results geocoded)
    geocoded))


(defn log-and-remove-non-geocodables [jugs]
  (-> (remove :coords (vals jugs))
      (map* #(select-keys % [:screen_name :location]))
      clojure.pprint/pprint)
  (into {} (filter #(get-in % [1 :coords]) jugs)))




;;;;;; Writing out ClojureScript

(def thing {:foo "bar" :baz "QUUUUUUUUU" :banana "romo"})
(defn create-cljs [jug-data]
  (let [cljs-src (str (prn-str '(ns juglist.data))                      
                      (prn-str (list 'def 'jug-data jug-data)))]
    (spit "src-cljs/juglist/data.cljs" cljs-src)))

;;;;;; Entry Point

(defn -main "Entry point" [& args]

  (-> (twitter-list-data) :users
      (map* twitter->jug)
      (mapify-jugs)
      (fix-nongeocodable-locations)
      (add-lat-lons)
      (log-and-remove-non-geocodables)
      (create-cljs))
  (log "Done"))


