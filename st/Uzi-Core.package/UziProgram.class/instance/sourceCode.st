accessing
sourceCode
	^ node ifNil: '' ifNotNil: [node token inputValue]