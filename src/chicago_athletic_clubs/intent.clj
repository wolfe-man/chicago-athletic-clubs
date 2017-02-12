(ns chicago-athletic-clubs.intent
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clj-time.predicates :as p]
            [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [chicago-athletic-clubs.util :refer [help-request]]))


(defn local-parsed-dt [date formatter]
  (-> formatter
      (f/with-zone (t/time-zone-for-id "America/New_York"))
      (f/parse date)))


(defn get-day-of-week [yyyy-MM-dd]
  (let [date-time (local-parsed-dt yyyy-MM-dd (f/formatter "yyyy-MM-dd"))]
    (cond
      (p/sunday? date-time) "Sunday"
      (p/monday? date-time) "Monday"
      (p/tuesday? date-time) "Tuesday"
      (p/wednesday? date-time) "Wednesday"
      (p/thursday? date-time) "Thursday"
      (p/friday? date-time) "Friday"
      (p/saturday? date-time) "Saturday")))


(defn get-location-id [location]
  (let [wicker-regex (partial re-find #"(?i)wicker")]
    (cond
      (wicker-regex location) 8799)))


(defn get-cac-classes [class]
  (let [yoga-regex (partial re-find #"(?i)yoga")
        spin-regex (partial re-find #"(?i)spin")]
    (cond
      (yoga-regex class)
      #{"Aerial Yoga 60 Min" "Ashtanga Power Yoga 75 Min"
        "Ashtanga Power Yoga 90 Min" "Ashtanga Power Yoga 60 Min"
        "Feldenkrais 60 Min" "Foam Roller Recovery 30 Min"
        "Foam Roller Recovery 45 Min" "Gentle Qi Gong 60 Min"
        "Gentle Qi Gong 90 Min" "Gentle Tai Chi 90 Min" "Gentle Yoga 60 Min"
        "Gentle Yoga 90 Min" "Hatha Yoga 60 Min" "Hatha Yoga 75 Min"
        "Hatha Yoga 90 Min" "Hatha 60 Min" "Hot Room Yoga 75 Min"
        "Hot Room Yoga 90 Min" "Hot Room Yoga 60 Min"
        "Live Music Yoga 90 Min" "Meditation 30 Min" "Meditative Yoga 60 Min"
        "Meditative Yoga 90 Min" "One Hour Hatha Yoga" "One Hour Yoga"
        "Power Flow Yoga 60 Min" "Power Flow Yoga 75 Min" "Stretch 60 Min"
        "Stretch 45 Min" "Stretch 30 Min" "Stretch Yoga 60 Min"
        "Tai Chi 60 Min" "Gentle Tai Chi 90 Chi" "Vinyasa Flow Yoga 75 Min"
        "Vinyasa Flow Yoga 60 Min" "Vinyasa Flow Yoga 90 Min"
        "Yin Yoga 60 Min" "Yin Yoga 90 Min" "Yin Yoga 75 Min"
        "Yoga Beginner 60 Min" "Yoga Beginner 75 Min" "Yoga Beginner 90 Min"
        "Yoga Multi-Level 75 Min" "Yoga Multi-Level 90 Min"
        "Yoga Multi-Level 60 Min" "Yoga Elements 60 Min"
        "Yoga Elements 90 Min" "Yoga Sculpt 60 Min"
        "Functional Balance 45 Min" "Yoga 90 Min"}
      (spin-regex class)
      #{"Live DJ Spin 50 Min" "Quick Spin" "Spin 50 Min" "Spin 75 Min"
        "BURN" "SPRINT" "SPRINT 30 min"})))


(defn get-cac-schedule [location-id start-date]
  (let [end-date (-> start-date
                     (local-parsed-dt (f/formatter "yyyy-MM-dd"))
                     (t/plus (t/days 1))
                     ((partial f/unparse (f/formatter "yyyy-MM-dd"))))]
    (-> (str "https://chicago-abc.appspot.com/events.json"
             "?clubNumber=" location-id "&"
             "eventDateRange=" start-date "," end-date)
        slurp
        (json/read-str :key-fn keyword)
        :events)))


(defn get-cac-data [class date location]
  (let [location-id (get-location-id location)
        classes (get-cac-classes class)
        schedule (get-cac-schedule location-id date)]
    {:day (get-day-of-week date)
     :classes classes
     :schedule schedule}))


(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*$" s)
    (read-string s)))


(defn invalid-date? [yyyy-MM-dd]
  (and yyyy-MM-dd
       (or (= "" yyyy-MM-dd)
           (let [[year month day] (-> yyyy-MM-dd
                                      (clojure.string/split  #"-")
                                      ((partial map parse-number)))]
             (or (not= year (t/year (l/local-now)))
                 (not= month (t/month (l/local-now)))
                 (not (pos? day))
                 (> day 31))))))


(defn parse-classes [{:keys [eventName eventTimestamp]}]
  (let [hour (-> eventTimestamp
                 (local-parsed-dt (f/formatter "yyyy-MM-dd HH:mm:ss.SSSSSS"))
                 t/hour)
        minute (-> eventTimestamp
                   (local-parsed-dt (f/formatter "yyyy-MM-dd HH:mm:ss.SSSSSS"))
                   t/minute)]
    (if (zero? minute)
      (format "%s at %s" eventName hour)
      (format "%s at %s %s" eventName hour minute))))


(defn get-output [day filtered-classes location]
  (let [parsed-classes (map parse-classes filtered-classes)
        class-output (cond
                       (= 1 (count parsed-classes))
                       parsed-classes
                       (= 2 (count parsed-classes))
                       (interpose " and " parsed-classes)
                       (< 2 (count parsed-classes))
                       (let [with-commas (->> parsed-classes
                                              butlast
                                              (interpose ", ")
                                              (apply str))
                             last-class (last parsed-classes)]
                         (apply str with-commas " and " last-class)))]
    (apply str "This " day " in " location " there is " class-output)))


(defn intent-request [{:keys [intent]}]
  (let [class (get-in intent [:slots :Class :value])
        date (get-in intent [:slots :Date :value])
        location (get-in intent [:slots :Location :value])]
    (if (invalid-date? date)
      (help-request)
      (let [{:keys [day classes schedule]} (get-cac-data class date location)
            filtered-classes (filter (fn [{:keys [eventName]}]
                                       (contains? classes eventName)) schedule)
            output (get-output day filtered-classes location)]
        {:title "Chicago Athletic Clubs"
         :output output
         :reprompt-text ""
         :should-end-session true}))))
