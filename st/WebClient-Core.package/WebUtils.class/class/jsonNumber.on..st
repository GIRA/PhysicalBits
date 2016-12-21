json-encode
jsonNumber: aNumber on: stream
	"Encodes a number"

	| value |
	value := aNumber.
	value isInteger ifFalse:[value := aNumber asFloat].
	stream print: aNumber.
