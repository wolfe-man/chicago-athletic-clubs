(ns chicago-athletic-clubs.util)


(defn help-request []
  {:title "Help"
   :output (str "I know the flavor of the day at good berry's for every date "
                "in the month.  Just say, alexa, ask good berry's fan what is "
                "the flavor of the day. Do you want to ask me for a "
                "date this month?")
   :reprompt-text (str "I know the flavor of the day at good berry's for "
                       "every date in the month.  Just say, alexa, ask "
                       "good berry's fan what is the flavor of the day. "
                       "Do you want to ask me for a date this month?")
   :should-end-session false})


(defn launch-request []
  {:title "Welcome"
   :output (str "Welcome to Goodberry's Flavor of the Day. "
                "Please ask me for today's flavor of the day by saying, "
                "Alexa, ask good berry's fan what is the flavor of the day "
                "today or on Friday.")
   :reprompt-text (str "Please ask me for today's flavor of the day by "
                       "saying, Alexa, ask good berry's fan what is the flavor "
                       "of the day today or on Friday.")
   :should-end-session false})
