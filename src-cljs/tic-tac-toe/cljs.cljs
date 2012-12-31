(ns tic-tac-toe.core
	(:use [jayq.core :only [$ css inner find data text current-target on delegate click]]
	 	  [jayq.util :only [log clj->js]])
	(:require[goog.events :as events]))

;; logging 
(defn console-log [my-var]
 (.log js/console (pr-str my-var)))

;; DOM Elements

(def $turn-prompt ($ :#TurnPrompt))
(def $status-text ($ :#StatusText))
(def $game-table  ($ :#GameTable))
(def $button ($ :button))
(def $td ($ :td))

;; Vars
(def turns (array))
(def finished false)
(def winner false)
(def active-player false)

;; App 
(defn set-history 
	"We just take the existence of the history HTML5 api for granted here."
	[url]
	(.pushState (aget js/window "history") nil nil url))

(defn curr-target [evt]
	(.-currentTarget evt))
;;
(defn start-game []
	(.modal $turn-prompt (clj->js {:show :true})))

(defn cache-board [table]
	(let [rows {}]
		(into rows 
			(for [x (range 3) 
			      y (range 3)] 
			      [(+(str x)(str y)) (.find table (apply str (+ "[data-x=" x "][data-y=" y "]")))]))))

;; Rules	
(defn check-x [x player]
	;(== (== (.data (contains? rows (apply str (+ "0" x))) "player") player) 
	;	(== (.data (contains? rows (apply str (+ "1" x))) "player") player) 
	;	(== (.data (contains? rows (apply str (+ "2" x))) "player") player))
	)

(defn check-y [y player]
	;(== (== (.data (contains? rows (apply str (+ "0" y))) "player") player) 
	;	(== (.data (contains? rows (apply str (+ "1" y))) "player") player) 
	;	(== (.data (contains? rows (apply str (+ "2" y))) "player") player))
	)

(defn check-all-diags [player])


(defn check-diags [x y player]
	"If the sum of x and y are odd, its not on a diagonal."
	[x y player]
	(if (== (rem (+ x y) 2) 1) false)
	(check-all-diags player))

(defn set-game-over [x y player])

(defn set-winner [player]
	(set! winner player)
	(set! finished true)
	(.text $status-text (apply str(+ player "Won")))
	(.css $game-table "background" "#FAFAFA"))

(defn play [entering-player turns]
	(let [turn-copy turns
		  player (if (== entering-player "x") "o" "x")
		  turn (.split (get turns 0) ",")
		  x (apply str (get turn 0))
		  y (apply str (get turn 1))
		  row-key (+ (str y) (str y))
		  rows (cache-board $game-table)
		  winner ()]
		  (.text (row-key rows) player)
		  ))

(defn check-draw [turns])

(defn set-turn [aplayer]
	(.text $status-text (apply str (+ aplayer "'s Turn")))
	(set! active-player aplayer))

(defn route []
	(let [path (.substring (.-pathname (aget js/window "location")) 5)
		  turn (.slice path -1)
		  turns (.split (.slice path 1 -2) "-")]
		  ;(console-log turns)
		  (if (empty? turn) (start-game))
		  (set-turn turn)
		  (if-not (empty? turns)
		  	(do (play turn turns)
		  		;(check-draw turns)
		  		)
			)))

(defn construct-path []
	(apply str(+ "/game/" (.join turns "-") "/" active-player)))

(defn change-turn []
	(if (== active-player "x") (set-turn "o") (set-turn "x")))

(defn select-turn 
	"select turn starts the game when the a player selects x or o from modal window."
	[e]	
	(let [elem ($ (curr-target e)) 
		  turn (.data elem "turn")
		  url ( apply str (+ "/game/" turn))]
		(.modal $turn-prompt (clj->js "hide"))
		(set-turn turn)
		(set-history url)))

(defn take-turn [e]
	(let [cell ($ (curr-target e)) 
		  x (.data cell "x")
		  y (.data cell "y")]
		  (if (= finished true) 
		  	(js/alert "The game is over. Stop playing!"))
		  ;;(if-not(empty? ) (.data ((str apply(+ x y)) rows) "player") (js/alert "Stop that. Someone else played there!"))
		  (.push turns (+ x "," y))
		  (change-turn)
		  (set-history (construct-path))
		  (route)))

(defn init []
	(.modal $turn-prompt (clj->js {:show :false}))
	(cache-board $game-table)
	;;(.-addEventListener "popstate" (route))
	(.click $button (fn [e] (select-turn e)))
	(.click $td (fn [e] (take-turn e))))

(set! (.-onload js/window) init)