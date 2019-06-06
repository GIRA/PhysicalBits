accessing
detail: req
	| id out |
	id := req fields at: #id ifAbsent: nil.
	id ifNotNil: [| output |
		output := self outputAt: id.
		out := output contents copyReplaceAll: { Character cr } with: String crlf.
		output reset].
	^ (super detail: req)
		at: #isConnected put: self protocol isConnected;
		at: #portName put: self protocol portName;
		at: #output put: out;
		yourself 