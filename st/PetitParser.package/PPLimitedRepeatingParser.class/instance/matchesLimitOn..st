private
matchesLimitOn: aStream
	| element position |
	position := aStream position.
	element := limit parseOn: aStream.
	aStream position: position.
	^ element isPetitFailure not