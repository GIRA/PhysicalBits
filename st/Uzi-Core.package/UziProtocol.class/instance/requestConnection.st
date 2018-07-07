connection
requestConnection
	| connRequest majorVersion minorVersion n1 n2 |
	connRequest := self class msgOutConnectionRequest.
	majorVersion := self class majorVersion.
	minorVersion := self class minorVersion.
	self send: { connRequest . majorVersion . minorVersion }.
	n1 := serial next.
	n2 := (majorVersion + minorVersion + n1) \\ 256.
	self send: { n2 }.
	n2 = serial next
		ifFalse: [self privateDisconnect]	