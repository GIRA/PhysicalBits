comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self scriptName hash.
	self arguments do: [:arg | hash := hash * 19 + arg hash].
	^ hash