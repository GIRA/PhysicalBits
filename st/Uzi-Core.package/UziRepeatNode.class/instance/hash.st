comparing
hash
	| hash |
	hash := 67.
	hash := hash * 19 + self times hash.
	hash := hash * 19 + self body hash.
	^ hash