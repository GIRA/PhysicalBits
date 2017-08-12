comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self alias hash.
	hash := hash * 37 + self path hash.
	^ hash