printing
printOn: stream
	super printOn: stream.
	self token ifNotNil: [:tkn |
		stream cr; tab.
		stream 
			nextPutAll: '... ';
			nextPutAll: tkn inputValue;
			nextPutAll: ' ...']