comparing
hash
	| hash |
	hash := 67.
	self imports do: [:each | hash := hash * 19 + each hash].
	self globals do: [:each | hash := hash * 23 + each hash].
	self scripts do: [:each | hash := hash * 29 + each hash].
	^ hash