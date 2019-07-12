private
compile: src type: type silent: silentFlag
	^ type = 'text' 
		ifTrue: [self protocol compileString: src silent: silentFlag]
		ifFalse: [self protocol compileJSON: src silent: silentFlag]