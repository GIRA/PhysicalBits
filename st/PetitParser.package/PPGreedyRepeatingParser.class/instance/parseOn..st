parsing
parseOn: aStream
	| start element elements positions |
	start := aStream position.
	elements := OrderedCollection new.
	[ elements size < min ] whileTrue: [ 
		(element := parser parseOn: aStream) isPetitFailure ifTrue: [ 
			aStream position: start.
			^ element ].
		elements addLast: element ].
	positions := OrderedCollection with: aStream position.
	[ elements size < max and: [ (element := parser parseOn: aStream) isPetitFailure not ] ] whileTrue: [
		elements addLast: element.
		positions addLast: aStream position ].
	[ positions isEmpty ] whileFalse: [
		aStream position: positions last.
		element := limit parseOn: aStream.
		element isPetitFailure ifFalse: [
			aStream position: positions last.
			^ elements asArray ].
		elements isEmpty ifTrue: [
			aStream position: start.
			^ element ].
		elements removeLast.
		positions removeLast ].
	aStream position: start.
	^ PPFailure message: 'overflow' at: start