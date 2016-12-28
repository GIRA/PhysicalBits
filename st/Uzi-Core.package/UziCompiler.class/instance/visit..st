visiting-utils
visit: anUziNode
	^ script 
		ifNil: [anUziNode accept: self]
		ifNotNil: [script withCurrentNode: anUziNode do: [anUziNode accept: self]]