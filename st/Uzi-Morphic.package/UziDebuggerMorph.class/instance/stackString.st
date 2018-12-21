accessing
stackString
	^ String streamContents: [:stream || stackFrames |
		stackFrames := debugger stackFrames.
		1 to: stackFrames size do: [:i || stackFrame prev |
			i > 1 ifTrue: [stream nextPutAll: '----'; cr].
			stackFrame := stackFrames at: i.
			prev := stackFrames at: i + 1 ifAbsent: nil.
			stackFrame temporaries
				do: [:value | stream print: value; cr].
			stackFrame locals
				keysAndValuesDo: [:local :value | stream 
					nextPutAll: local displayName;
					nextPutAll: ': ';
					print: value;
					cr].
			stream 
				nextPutAll: 'PC: ';
				print: (prev ifNil: [0] ifNotNil: [prev pc]);
				cr.
			stream
				nextPutAll: 'FP: ';
				print: (prev ifNil: [0] ifNotNil: [stackFrame fp]);
				cr]]