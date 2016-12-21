handling
newRequest
	"Answer a new request.
	Subclasses should override this method to use a different request class."

	^(WebRequest new)
		server: self;
		yourself