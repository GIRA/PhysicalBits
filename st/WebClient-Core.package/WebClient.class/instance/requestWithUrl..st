initialize
requestWithUrl: urlString
	"Create and return a new WebRequest initialized with the given url."

	^self  newRequest initializeFromUrl: urlString