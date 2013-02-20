(ns tic-tac-toe.core
	(:use [jayq.core :only [$ css inner find data text current-target on click html append]]
        [jayq.util :only [log clj->js]])
  (:require [clojure.string :as string]))

;;------------------
;; Logging
;;------------------

(defn console-log [my-var]
 (.log js/console (pr-str my-var)))

;;------------------
;; Vars 
;;------------------

(def $turn-prompt ($ :#TurnPrompt))
(def $status-text ($ :#StatusText))
(def $game-table  ($ :#GameTable))
(def $button ($ :button))
(def $td ($ :td))
(def $body ($ :body))

(defn new-game-state
  "Returns a map in an atom which holds the entire state of the game.
   Using an atom explicitly says we want side effects for this peice of data."
  []
   (atom {:turns []
          :finished false
          :winner false
          :draw false
          :active-player false}))

(def game-state (new-game-state))

;;------------------
;; Helpers
;;------------------

(defn set-history 
	"We just take the existence of the history HTML5 api for granted here.
   A more rigorous implementation would check for its "
	[url]
	(.pushState (aget js/window "history") nil nil url))

(defn curr-target 
	"Helper function. Better syntax for getting the clicked element."
	[evt]
	(.-currentTarget evt))

(defn start-game 
	"Displays x/o modal prompt at game start."
	[]
	(.modal $turn-prompt (clj->js {:show :true})))

(defn make-board 
	"Creates a map representation of the current table state.
   Ostensibly helps us avoid traversing the DOM consisently for changes.
   Returns a map of the form (row-column dom-object). ex. (00 <HTML object>)"
	[table]
  (into {} 
    (for [x (range 3) 
          y (range 3)] 
      [(str x y) (.find table (str "[data-x=" x "][data-y=" y "]"))])))

(defn update-game
  "Updates the current game state. If we are swaping the turns vector we use update-in then
   conj the value onto our vector. If we are swaping, we assoc. 
   More about swap! here: http://clojuredocs.org/clojure_core/clojure.core/swap!"
  [key value]
  (if (= key :turns) (swap! game-state update-in [:turns] conj value)
      (swap! game-state assoc-in [key] value)))

;; ----------------------
;; Rules
;; ----------------------

(defn player-data 
  "Get current player from html table."
	[selector]
	(.data ($ selector) "player"))

(defn check-axis
  "Higher order function. Checks for matching moves by player along x and y axis."
	[axis? axis player rows]
  (let [axis-fn (if (= axis? :x)
                  #(str %1 %2)
                  #(str %2 %1))]
    (every? identity (map #(== player (player-data ((axis-fn axis %) rows)))
                          ["0" "1" "2"]))))

(defn check-all-diags 
	"Same as above. Checks for matching moves along each diagonal."
	[player rows]
  (letfn [(check [row-key]
            (== player (player-data (row-key rows))))
          (all-same [row-keys]
            (every? identity (map check row-keys)))]
    (or (all-same ["00" "11" "22"])
        (all-same ["02" "11" "20"]))))

(defn check-diags 
	"If the sum of x and y are odd, we're not on a diagonal."
	[x y player rows]	
	(if (not= (rem (+ x y) 2) 1))
	(check-all-diags player rows))

(defn game-over 
	"Conditions for game end."
	[x y player rows]
	(or
    (check-axis :y y player rows) 
		(check-axis :x x player rows) 
		(check-diags x y player rows)))

(defn set-winner 
	"Conditions for winning game. Uses set! which is a bit gnarly I admit."
	[player]
  (update-game :winner player)
  (update-game :finished true)
	(.text $status-text (+ (str player) " Won in " (@game-state :turn-count) " moves."))
	(.css $game-table "background" "#FAFAFA"))

(defn play 
	"Sets text for current move in corresponding cell. 
	 Uses jQuery data API to store moves in rows map. Checks for wins."
	[entering-player turns]
	(let [turn-copy turns
        player (if (== entering-player "x") "o" "x")
        turn (.split (last turns) ",")
        x (apply str (get turn 0))
        y (apply str (get turn 1))
        row-key (+ (str x) (str y))
        rows (make-board $game-table)
        winner ()]
    (.text ($ (row-key rows)) player)
    (.data ($ (row-key rows)) (clj->js {:player player}))
    (if (game-over x y player rows)
      (set-winner player))))

(defn check-draw 
	"If turns are greater than or equal to 9 and no winner, draw is declared."
	[turns]
	(if (and (>= (count (@game-state :turns)) 9) (== false (@game-state :winner)))
		(do
      (update-game :finished true)
      (update-game :draw true)
			(.text $status-text "Draw!")
			(.css  $game-table "background" "#FAFAFA"))))

(defn set-turn 
	"Sets current turn."
	[aplayer]
	(.text $status-text (apply str (+ aplayer "'s Turn")))
  (update-game :active-player aplayer))

(defn route 
	"Builds turn array based on path."
	[]
	(let [path (.substring (.-pathname (aget js/window "location")) 5)
        turn (.slice path -1)
        turns (.split (.slice path 1 -2) "-")]
    (if (empty? turn) (start-game))
    (set-turn turn)
    (if-not (empty? turns)
      (do (play turn turns)
          (check-draw turns)))))

(defn construct-path 
	"Create the location path."
	[]
	(apply str(+ "/game/" (string/join "-" (@game-state :turns)) "/" (@game-state :active-player))))

(defn change-turn []
	(if (== (@game-state :active-player) "x") (set-turn "o") (set-turn "x")))

(defn select-turn 
	"Starts the game when selecting a player from modal window."
	[e]	
	(let [elem ($ (curr-target e)) 
        turn (.data elem "turn")
        url (str (+ "/game/" turn))]
		(.modal $turn-prompt (clj->js "hide"))
		(set-turn turn)
		(set-history url)))

(defn take-turn 
	"Create the current turn. Check if cells are occupied."
	[e]
	(let [cell ($ (curr-target e)) 
        x (.data cell "x")
        y (.data cell "y")
        row-key (+ (str y) (str y))]
    (if-not (== (.data cell) nil )
      (do
        (swap! game-state update-in [:turn-count] (fnil inc 0))
        (update-game :turns (+ x "," y))
        (change-turn)
        (set-history (construct-path))
        (route)))))

(defn init 
	"Initialize the app"
	[]
	(.modal $turn-prompt (clj->js {:show :false}))
	(.addEventListener js/window "popstate" (route))
	(.click $button (fn [e] (select-turn e)))
	(.click ($ (.find $game-table "td"))(fn [e] (take-turn e))))

(set! (.-onload js/window) init)