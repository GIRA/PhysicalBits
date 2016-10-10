parsing
parseComments: anArray on: aStream
	| start comments |
	comments := anArray.
	[ [ aStream atEnd not and: [ SeparatorPredicate value: aStream uncheckedPeek ] ]
		whileTrue: [ aStream next ].
	 aStream atEnd not and: [ aStream uncheckedPeek = $" ] ] whileTrue: [
		aStream next.
		start := aStream position.
		aStream upTo: $".
		comments := comments copyWith: (start to: aStream position) ].
	^ comments