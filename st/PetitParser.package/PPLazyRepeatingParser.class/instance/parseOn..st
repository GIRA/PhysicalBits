parsing
parseOn: aStream
	| start element elements |
	start := aStream position.
	elements := OrderedCollection new.
	[ elements size < min ] whileTrue: [
		(element := parser parseOn: aStream) isPetitFailure ifTrue: [
			aStream position: start.
			^ element ].
		elements addLast: element ].
	[ self matchesLimitOn: aStream ] whileFalse: [
		elements size < max ifFalse: [
			aStream position: start.
			^ PPFailure message: 'overflow' at: start ].
		element := parser parseOn: aStream.
		element isPetitFailure ifTrue: [
			aStream position: start.
			^ element ].
		elements addLast: element ].
	^ elements asArray