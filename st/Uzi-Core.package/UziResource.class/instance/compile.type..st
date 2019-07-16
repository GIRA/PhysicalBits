private
compile: src type: type
	^ type = 'text' 
		ifTrue: [self controller compileString: src]
		ifFalse: [self controller compileJSON: src]