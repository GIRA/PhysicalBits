testing
isSuccess
	"Is this a successful response?"

	^self code between: 200 and: 299
