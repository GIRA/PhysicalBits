comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self value hash.
	hash := hash * 37 + self scale hash.
	^ hash