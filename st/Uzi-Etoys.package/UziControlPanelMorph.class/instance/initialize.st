initialize-release
initialize
	super initialize.
	rebuildFlag := false.
	protocol := UziProtocol new.
	{ 
		UziProtocolEvents connect.
		UziProtocolEvents disconnect.
		UziProtocolEvents runProgram.
	} 
	do: [:evt | protocol when: evt send: #update to: self].
	
	pinsReporting := Set new.
	globalsReporting := Set new.
	
	pinTexts := Dictionary new.
	globalTexts := Dictionary new.
	scriptTexts := Dictionary new.
	portName := self class lastPortName.
	self color: Color white;
		borderWidth: 2;
		borderColor: Color red;
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #topToBottom;		
		buildSubmorphs