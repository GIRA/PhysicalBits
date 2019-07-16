initialize-release
initialize
	super initialize.
	{
		UziEvents connect.
		UziEvents disconnect.
		UziEvents programUpdate.
		UziEvents reportingChange.
		UziEvents debuggerUpdate.
	}
	do: [:evt | self protocol when: evt send: #update to: self].
	Uzi logger when: #flushed send: #updateOutput: to: self.
	
	clients := Dictionary new.
	self initializeResources