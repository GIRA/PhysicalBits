connection
requestConnection
	| connRequest majorVersion minorVersion n1 n2 |
	[connRequest := self class msgOutConnectionRequest.
	majorVersion := self class majorVersion.
	minorVersion := self class minorVersion.
	self send: { connRequest . majorVersion . minorVersion }.
	UziTranscript log: 'Requesting connection...'; flush.
	500 milliSeconds asDelay wait.
	n1 := serial next.
	n2 := (majorVersion + minorVersion + n1) \\ 256.
	self send: { n2 }.
	500 milliSeconds asDelay wait.
	n2 = serial next
		ifTrue: [UziTranscript log: 'Connection accepted!'; flush]
		ifFalse: [self privateDisconnect.
			UziTranscript log: 'Connection rejected'; flush]]
		on: ConnectionTimedOut
		do: [self privateDisconnect.
			UziTranscript log: 'Connection timeout'; flush]