messages
build: aNode messages: anArray 
	^ (anArray isNil or: [ anArray isEmpty ]) 
		ifTrue: [ aNode ]
		ifFalse: [
			anArray 
				inject: aNode
				into: [ :rec :msg | 
					msg isNil 
						ifTrue: [ rec ]
						ifFalse: [UziMessageSendNode 
								receiver: rec
								selector: (msg first inject: '' into: [:subTotal :next | subTotal, next inputValue]) asSymbol
								arguments: msg second]]]