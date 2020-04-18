encoding programs
writeGlobals: globals
	| count |
	self indent; write: ':globals #{'.
	count := globals size.
	globals withIndexDo: [:var :index |
		index > 1 ifTrue: [self indent; indent].
		var name
			ifNil: [self write: '(emit/constant {1})' 
				format: {var value = Float infinity ifTrue: ['##Inf'] ifFalse: [var value]}]
			ifNotNil: [self write: '(emit/variable "{1}" {2})'
				format: {var name. var value = Float infinity ifTrue: ['##Inf'] ifFalse: [var value]}].
		index = count ifFalse: [self writeLine: '']].
	self writeLine: '}'