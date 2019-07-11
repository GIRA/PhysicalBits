initialize-release
initialize
	super initialize.
	{
		UziProtocolEvents connect.
		UziProtocolEvents disconnect.
		UziProtocolEvents programUpdate.
		UziProtocolEvents reportingChange.
		UziProtocolEvents debuggerUpdate.
	}
	do: [:evt | self protocol when: evt send: #update to: self].
	Uzi logger when: #flushed send: #updateOutput: to: self.
	
	clients := Dictionary new.
	self initializeResources