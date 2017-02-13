(ns chicago-athletic-clubs.util)


(defn help-request []
  {:title "Help"
   :output (str "I know the schedule for Chicago Athletic Clubs this week."
                " Just say, alexa, ask the gym when yoga is tomorrow. "
                "Do you want to ask me for the schedule?")
   :reprompt-text (str "I know the schedule for Chicago Athletic Clubs "
                       "this week. Just say, alexa, ask the gym "
                       "when yoga is tomorrow. Do you want to ask "
                       "me for the schedule?")
   :should-end-session false})


(defn launch-request []
  {:title "Welcome"
   :output (str "Welcome to Chicago Athletic Clubs. "
                "Please ask me for today's gym schedule by saying, "
                "Alexa, ask the gym when yoga is tomorrow.")
   :reprompt-text (str "Please ask me for today's gym schedule by saying, "
                       "Alexa, ask the gym when yoga is tomorrow.")
   :should-end-session false})
