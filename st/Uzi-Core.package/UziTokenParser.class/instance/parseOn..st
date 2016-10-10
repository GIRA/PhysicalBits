parsing
parseOn: aStream
	| start comments token |
	start := aStream position.
	comments := self
		parseComments: #()
		on: aStream.
	token := super parseOn: aStream.
	token isPetitFailure ifTrue: [
		aStream position: start.
		^ token ].
	comments := self
		parseComments: comments
		on: aStream.
	^ token comments: comments