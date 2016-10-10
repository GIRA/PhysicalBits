printing
printOn: aStream
	| title |
	title := self class name.
	aStream
		nextPutAll: (title first isVowel ifTrue: ['an '] ifFalse: ['a ']);
		nextPutAll: title;
		nextPut: $(;
		nextPutAll: self inputValue;
		nextPut: $)