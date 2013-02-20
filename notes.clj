;; This buffer is for notes you don't want to save, and for Lisp evaluation.
;; If you want to create a file, visit that file with C-x C-f,
;; then enter the text in that file's own buffer.

;; Create an Atom
(def game-state (atom {:turns [] :winner false :game-over false}))

;; Conj new value, but only one.
(swap! game-state conj [:turns :first])

;; Add a new key arbitrarily and increment it once using fnil.
(swap! game-state update-in [:turn-count] (fnil inc 0))

;; Add a winner key.
(swap! game-state assoc-in [:winner] "Bob")

;; add a new val, but his doesn't work
(swap! game-state update-in [:turns] conj :third)
