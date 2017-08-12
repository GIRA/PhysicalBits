comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self condition hash.
	hash := hash * 37 + self trueBranch hash.
	hash := hash * 37 + self falseBranch hash.
	^ hash