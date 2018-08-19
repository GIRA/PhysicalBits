accessing
sourceCode
	^ node ifNil: '' 
		ifNotNil: [node token 
			ifNil: '' 
			ifNotNil: [:token | token inputValue]]