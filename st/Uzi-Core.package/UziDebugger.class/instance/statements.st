as yet unclassified
statements
	| result |
	result := Dictionary new.
	program scripts do: [:script || group |
		result at: script put: OrderedCollection new.
		group := OrderedCollection new.
		(result at: script) add: group.
		script instructions do: [:instr |
			group add: instr.
			instr isStatement ifTrue: [
				group := OrderedCollection new.
				(result at: script) add: group]]].
	^ result