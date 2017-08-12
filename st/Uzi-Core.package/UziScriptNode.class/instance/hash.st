comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self name hash.
	self arguments do: [:arg | hash := hash * 19 + arg hash].
	hash := hash * 37 + self state hash.
	hash := hash * 37 + self tickingRate hash.
	hash := hash * 37 + self body hash.
	^ hash