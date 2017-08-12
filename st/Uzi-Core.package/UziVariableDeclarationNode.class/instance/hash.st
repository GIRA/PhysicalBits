comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self name hash.
	hash := hash * 37 + self value hash.
	^ hash