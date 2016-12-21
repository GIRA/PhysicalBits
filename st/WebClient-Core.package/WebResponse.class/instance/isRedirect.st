testing
isRedirect
	"Is this a redirect response?"

	^self code between: 300 and: 399
