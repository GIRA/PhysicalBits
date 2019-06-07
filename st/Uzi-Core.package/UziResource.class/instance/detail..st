accessing
detail: req
	| id out |
	id := req fields at: #id ifAbsent: nil.
	out := #().
	id ifNotNil: [
		out := self outputAt: id.
		self outputAt: id put: #()].
	^ (super detail: req)
		at: #isConnected put: self protocol isConnected;
		at: #portName put: self protocol portName;
		at: #output 
		put: (out collect: [:entry | Dictionary new 
			at: entry key 
			put: entry value; 
			yourself]);
		yourself 