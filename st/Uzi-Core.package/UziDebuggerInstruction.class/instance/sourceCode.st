accessing
sourceCode
	"Ugh!"
	^ innerInstructions anyOne
		ifNil: ''
		ifNotNil: [:i | i node 
			ifNil: ''
			ifNotNil: [:n | n token
				ifNil: ''
				ifNotNil: [:t | t collection]]]