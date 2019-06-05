class initialization
initialize
	"
	self initialize
	"
	buffer := '' writeStream.
	self when: #flushed
		evaluate: [:msg | WorldState addDeferredUIMessage: [Transcript show: msg]]