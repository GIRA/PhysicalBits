private
compile: src type: type
	^ type = 'text' 
		ifTrue: [protocol compileString: src]
		ifFalse: [protocol compileJSON: src]