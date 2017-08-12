comparing
hash
	| hash |
	hash := 67.
	self locals do: [:local | hash := hash * 19 +  local hash].
	self statements do: [:stmt | hash := hash * 37 + stmt hash].
	^ hash