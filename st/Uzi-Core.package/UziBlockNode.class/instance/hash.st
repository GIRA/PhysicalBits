comparing
hash
	| hash |
	hash := 67.
	self statements do: [:stmt | hash := hash * 37 + stmt hash].
	^ hash