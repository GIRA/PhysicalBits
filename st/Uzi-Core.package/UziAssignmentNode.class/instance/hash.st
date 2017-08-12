comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self left hash.
	hash := hash * 37 + self right hash.
	^ hash