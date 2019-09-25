private
compare: tree1 with: tree2
	[UziParseTreeComparer assert: tree1 equals: tree2. ^ true]
			on: AssertionFailure
			do: [:ex | ^ false]