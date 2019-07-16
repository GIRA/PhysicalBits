private
compile: src type: type silent: silentFlag
	^ type = 'text' 
		ifTrue: [self controller compileString: src silent: silentFlag]
		ifFalse: [self controller compileJSON: src silent: silentFlag]