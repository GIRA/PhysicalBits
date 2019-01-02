initialize-release
initialize
	super initialize.
	{
		UziProtocolEvents connect.
		UziProtocolEvents disconnect.
		UziProtocolEvents runProgram.
		UziProtocolEvents reportingChange.
		UziProtocolEvents debuggerUpdate.
	}
	do: [:evt | self protocol when: evt send: #update to: self].
	
	clients := Dictionary new.
	self initializeResources