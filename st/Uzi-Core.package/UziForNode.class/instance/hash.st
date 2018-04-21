comparing
hash
	| hash |
	hash := 67.
	hash := hash * 37 + self counter hash.
	hash := hash * 37 + self start hash.
	hash := hash * 37 + self stop hash.
	hash := hash * 37 + self step hash.
	hash := hash * 37 + self body hash.
	^ hash