initialize
newResponse
	"Create a new response entitiy.
	Subclasses can override this method to provide a different response class"

	^(WebResponse new)
		request: self;
		yourself