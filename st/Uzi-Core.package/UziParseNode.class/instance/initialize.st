initialize-release
initialize
	super initialize.
	id := UUID new asString.
	self children do: [:each | each ifNotNil: [each parent: self]]