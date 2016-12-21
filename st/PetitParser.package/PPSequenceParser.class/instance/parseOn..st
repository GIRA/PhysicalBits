parsing
parseOn: aStream
	"This is optimized code that avoids unnecessary block activations, do not change."
	
	| start elements element |
	start := aStream position.
	elements := Array new: parsers size.
	1 to: parsers size do: [ :index |
		element := (parsers at: index) 
			parseOn: aStream.
		element isPetitFailure ifTrue: [
			aStream position: start.
			^ element ].
		elements at: index put: element ].
	^ elements