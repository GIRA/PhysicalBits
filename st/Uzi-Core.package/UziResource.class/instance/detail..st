accessing
detail: req
	| id out |
	id := req fields at: #id ifAbsent: nil.
	out := #().
	id ifNotNil: [out := (self outputAt: id) dequeueAll].
	^ (super detail: req)
		at: #isConnected put: self protocol isConnected;
		at: #portName put: self protocol portName;
		at: #output 
		put: (out collect: [:entry | Dictionary new 
			at: #type put: entry key; 
			at: #text put: entry value; 
			yourself]);
		yourself 