utils
isUnaryExpression: expr receiver: rcvr messages: msgs
	(expr size = 2
		and: [expr first = rcvr
			or: [expr first inputValue = rcvr]])
		ifFalse: [^ false].
	expr second size ~= msgs size
		ifTrue: [^ false].
	expr second with: msgs do: [:msg :val |
		(self isUnaryMessage: msg value: val)
			ifFalse: [^ false]].
	^ true