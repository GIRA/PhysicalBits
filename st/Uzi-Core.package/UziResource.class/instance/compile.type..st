private
compile: src type: type
	^ type = 'text' 
		ifTrue: [self protocol compileString: src]
		ifFalse: [self protocol compileJSON: src]