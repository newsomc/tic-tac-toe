(ns tic-tac-toe.core
	(:use [jayq.core :only [$ css inner find data text current-target on click html]]
	 	  [jayq.util :only [log clj->js]])
	(:require[goog.events :as events]))

;; Logging 
(defn console-log [my-var]
 (.log js/console (pr-str my-var)))

;; DOM Elements
(def $turn-prompt ($ :#TurnPrompt))
(def $status-text ($ :#StatusText))
(def $game-table  ($ :#GameTable))
(def $button ($ :button))
(def $td ($ :td))
(def $body ($ :body))

;; Vars
(def turns (array))
(def finished false)
(def winner false)
(def draw false)
(def active-player false)

;; App 
(defn set-history 
	"We just take the existence of the history HTML5 api for granted here."
	[url]
	(.pushState (aget js/window "history") nil nil url))

(defn curr-target 
	"Helper function. Better syntax for getting the clicked element."
	[evt]
	(.-currentTarget evt))
;;
(defn start-game 
	"Shows x,y modal prompt at game start."
	[]
	(.modal $turn-prompt (clj->js {:show :true})))

(defn cache-board 
	"Create a map of the current table state."
	[table]
	(let [rows {}]
		(into rows 
			(for [x (range 3) 
			      y (range 3)] 
			      [(+(str x)(str y)) (.find table (apply str (+ "[data-x=" x "][data-y=" y "]")))]))))

;; Rules	
(defn check-x 
	"Check for matching moves on the x-axis."
	[x player rows]
	(and (== (.data ($ ((+ (str x) "0") rows)) "player") player)
		 (== (.data ($ ((+ (str x) "1") rows)) "player") player) 
		 (== (.data ($ ((+ (str x) "2") rows)) "player") player)))

(defn check-y 
	"Check for matching moves on the y-axis."
	[y player rows]
	(and (== (.data ($ ((+ "0" (str y)) rows)) "player") player)
		 (== (.data ($ ((+ "1" (str y)) rows)) "player") player) 
		 (== (.data ($ ((+ "2" (str y)) rows)) "player") player)))

(defn check-all-diags 
	"Check for matching moves along each diagonal."
	[player rows]
	(or (and (== (.data ($ ("00" rows)) "player") player) 
			 (== (.data ($ ("11" rows)) "player") player) 
			 (== (.data ($ ("22" rows)) "player") player)) 

	    (and (== (.data ($ ("02" rows)) "player") player) 
			 (== (.data ($ ("11" rows)) "player") player) 
			 (== (.data ($ ("20" rows)) "player") player))))

(defn check-diags 
	"If the sum of x and y are odd, we're not on a diagonal."
	[x y player rows]	
	(if (not= (rem (+ x y) 2) 1))
	(check-all-diags player rows))

(defn game-over 
	"Conditions for game end."
	[x y player rows]
	(or (check-y y player rows) 
		(check-x x player rows) 
		(check-diags x y player rows)))

(defn set-winner 
	"Conditions for winning game. Uses set! which is a bit gnarly I admit."
	[player]
	(set! winner player)
	(set! finished true)
	(.text $status-text (+ (str player) " Won"))
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
		  rows (cache-board $game-table)
		  winner ()]
		  (.text ($ (row-key rows)) player)
		  (.data ($ (row-key rows)) (clj->js {:player player}))
		  (console-log (.data ($ (row-key rows)) "player"))
		  (if (game-over x y player rows)
		  	(set-winner player))))

(defn check-draw 
	"If turns are greater than or equal to 9 and no winner, draw is declared."
	[turns]
	(if (and (>= (alength turns) 9) (= false winner))
		(do
			(set! finished true)
			(set! draw true)
			(.text $status-text "Draw!")
			(.css $game-table "background" "#FAFAFA"))))

(defn set-turn 
	"Sets current turn."
	[aplayer]
	(.text $status-text (apply str (+ aplayer "'s Turn")))
	(set! active-player aplayer))

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
	""
	[]
	(apply str(+ "/game/" (.join turns "-") "/" active-player)))

(defn change-turn []
	(if (== active-player "x") (set-turn "o") (set-turn "x")))

(defn select-turn 
	"Starts the game when selecting a player from modal window."
	[e]	
	(let [elem ($ (curr-target e)) 
		  turn (.data elem "turn")
		  url ( apply str (+ "/game/" turn))]
		(.modal $turn-prompt (clj->js "hide"))
		(set-turn turn)
		(set-history url)))

(defn take-turn 
	"Create the current turn. Check if cells are occupied. 
	TODO: change checking for td text to player data with jQuery."
	[e]
	(let [cell ($ (curr-target e)) 
		  x (.data cell "x")
		  y (.data cell "y")
		  empty-cell "&nbsp;"
		  row-key (+ (str y) (str y))
		  rows (cache-board $game-table)]
		  (if (== finished true) 
		  	(js/alert "The game is over. Stop playing!")
		  	(if-not (== (.html cell) empty-cell) 
		  		(js/alert "Stop that. Someone else played there!")
		  		(do 
		  			(.push turns (+ x "," y))		  
		  			(change-turn)
		  			(set-history (construct-path))
		  			(route))))))

(defn init 
	"Initialize the app!"
	[]
	(.modal $turn-prompt (clj->js {:show :false}))
	(cache-board $game-table)
	(.addEventListener js/window "popstate" (route))
	(.click $button (fn [e] (select-turn e)))
	(.click ($ (.find $game-table "td"))(fn [e] (take-turn e))))

(set! (.-onload js/window) init)