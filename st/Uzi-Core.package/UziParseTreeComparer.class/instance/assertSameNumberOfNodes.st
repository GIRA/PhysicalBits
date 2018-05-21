private
assertSameNumberOfNodes 
	| expected |
	expected := nodes first size.
	nodes allButFirst do: [:each |
		self assert: expected
			equals: each size
			description: 'Parse trees should have the same number of nodes']
	