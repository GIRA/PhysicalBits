class initialization
initialize
	"
	self initialize
	"
	buffer := OrderedCollection new.
	self removeActionsWithReceiver: self;
		when: #flushed
		send: #displayOnTranscript:
		to: self