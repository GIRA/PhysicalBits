accessing
summary: req
	^ (super summary: req)
		at: #index put: self debugger index;
		at: #pc put: self debugger pc;
		at: #fp put: self debugger fp;
		at: #stackFrames put: (self debugger stackFrames
			collect: [:sf | Dictionary new
				at: #scriptName put: sf scriptName;
				at: #pc put: sf pc;
				at: #fp put: sf fp;
				at: #annotatedStack put: sf annotatedStack lines;
				at: #arguments put: (Array streamContents: [:stream |
					(sf arguments collect: [:var |
						stream nextPut: (Dictionary new
							at: #name put: var displayName;
							at: #value put: (sf localAt: var);
							yourself)])]);
				at: #locals put: (Array streamContents: [:stream |
					(sf locals keysAndValuesDo: [:key :value |
						stream nextPut: (Dictionary new
							at: #name put: key displayName;
							at: #value put: value;
							yourself)])]);
				yourself]);
		yourself