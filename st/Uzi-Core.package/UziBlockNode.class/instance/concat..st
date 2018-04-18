as yet unclassified
concat: anotherBlock
	^ (self class 
		statements: statements , anotherBlock statements)
			token: token "I really don't know what token should I use"