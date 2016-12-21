testing
isInformational
	"Is this a 1xx response?"

	^self code between: 100 and: 199
