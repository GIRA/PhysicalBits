accessing
at: key do: aBlock ifTrue: conditionBlock
	self at: key
		put: (RESTResource new
			onPOST: aBlock;
			yourself).	
	conditions at: key put: conditionBlock