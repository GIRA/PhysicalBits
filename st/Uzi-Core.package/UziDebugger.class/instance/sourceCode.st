accessing
sourceCode
	"Ugh!"
	^ self currentInstruction 
		ifNil: ''
		ifNotNil: [:i | i node 
			ifNil: ''
			ifNotNil: [:n | n token
				ifNil: ''
				ifNotNil: [:t | t collection]]]