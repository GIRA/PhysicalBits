accessing
detail: req
	| id out |
	id := req fields at: #id ifAbsent: nil.
	out := #().
	id ifNotNil: [out := (self outputAt: id) dequeueAll].
	^ (super detail: req)
		at: #isConnected put: self controller isConnected;
		at: #portName put: self controller portName;
		at: #tasks put: (self controller taskWatchers 
			collect: [:w | Dictionary new
				at: #scriptName put: w scriptName;
				at: #isRunning put: w isRunning;
				at: #isError put: w isError;
				yourself	]);
		at: #output put: (out 
			collect: [:entry | Dictionary new 
				at: #type put: entry key; 
				at: #text put: entry value; 
				yourself]);
		yourself 