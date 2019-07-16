initialize-release
initialize
	super initialize.
	{
		UziEvents connect.
		UziEvents disconnect.
		UziEvents programUpdate.
		UziEvents reportingUpdate.
		UziEvents debuggerUpdate.
	}
	do: [:evt | self controller when: evt send: #update to: self].
	Uzi logger when: #flushed send: #updateOutput: to: self.
	
	clients := Dictionary new.
	self initializeResources