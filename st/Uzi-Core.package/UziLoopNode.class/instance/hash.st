comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self pre hash.
	hash := hash * 37 + self condition hash.
	hash := hash * 37 + self post hash.
	hash := hash * 37 + self negated hash.
	^ hash