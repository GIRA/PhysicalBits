building messages
send: rcvr messages: msgs token: token
	^ msgs 
		ifNil: [rcvr]
		ifNotNil: [| result |
			result := nil.
			msgs do: [:msg || receiver selector args msgToken |
				receiver := result ifNil: [rcvr].
				selector := msg first.
				args := msg second.
				msgToken := msg third.
				result := (self 
						specialMessageSend: receiver
						selector: selector
						arguments: args)
					ifNil: [UziMessageSendNode
						receiver: receiver
						selector: selector
						arguments: args
						primitive: (self primitiveForSelector: selector)].				
				result token: (token class 
					on: token collection
					start: token start
					stop: msgToken stop
					value: token parsedValue)].
			result]
		