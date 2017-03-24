building messages
send: rcvr messages: msgs
	^ msgs 
		ifNil: [rcvr]
		ifNotNil: [| result |
			result := nil.
			msgs do: [:msg || receiver selector args |
				receiver := result ifNil: [rcvr].
				selector := msg key.
				args := msg value.
				result := (self 
						specialMessageSend: receiver
						selector: selector
						arguments: args)
					ifNil: [UziMessageSendNode
						receiver: receiver
						selector: selector
						arguments: args
						primitive: (self primitiveForSelector: msg key)]].
			result]
		