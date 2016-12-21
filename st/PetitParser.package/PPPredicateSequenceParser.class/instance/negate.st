operators
negate
	"Answer a parser that is the negation of the receiving predicate parser."
	
	^ self class 
		on: negated message: negatedMessage
		negated: predicate message: predicateMessage
		size: size