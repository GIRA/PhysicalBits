comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self selector hash.
	hash := hash * 37 + self primitive hash.
	self arguments do: [:arg | hash := hash * 19 + arg hash].
	^ hash