as yet unclassified
concat: anotherBlock
	^ (self class 
		arguments: arguments , anotherBlock arguments
		statements: statements , anotherBlock statements)
			token: token "I really don't know what token should I use"