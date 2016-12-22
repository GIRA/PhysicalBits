building messages
send: rcvr messages: msgs
	^ msgs 
		ifNil: [rcvr]
		ifNotNil: [| result |
			result := nil.
			msgs do: [:msg |
				result := UziMessageSendNode
					receiver: (result ifNil: [rcvr])
					selector: msg key
					arguments: msg value].
			result]
		