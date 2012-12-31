(ns tic-tac-toe.views.game
  (:require [tic-tac-toe.views.common :as common])
  (:use [noir.core :only [defpage defpartial]]))

(defn row [y]
  [:tr
    (for [x (range 0 3)]
      [:td {:data-x x :data-y y :style "text-align:center;"} "&nbsp;"])])

(defn print-board [times]
  (for [t (range 0 times)]
    (row t)))

(defpartial board [& content]
	[:div.container
           		[:div#Game {:style "margin-top: 100px; text-align:center;"}
           			[:a {:href "/game" :title "New Game" :style "float:right;"} "New Game"]
           			[:h1#StatusText]
           			[:div.row
           				[:div.span4.offset4
           					[:table#GameTable.table.table-bordered
                     (print-board 3) ]]]]]
           	[:div#TurnPrompt.modal.padded.hide
           		[:div.modal-header "Whose turn is it?"]
           		[:div.row.padded
           			[:div.span12
           				[:button.btn.btn-primary.btn-large {:data-turn "x"} "X's turn"]]]
           		[:div.row.padded
           			[:div.spin12
           				[:button.btn.btn-primary.btn-large {:data-turn "o"} "O's turn"]]]])

(defpage "/game" []
         (common/layout
         	[:div.navbar.navbar-inverse.navbar-fixed-top
           	  [:div.navbar-inner
           	  	[:div.container
           	  		[:div.row
           	  			[:h1.span12 "Tic Tac Toe"]]]]]
           	  			(board)))