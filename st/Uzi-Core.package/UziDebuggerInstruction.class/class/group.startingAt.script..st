instance creation
group: instructions startingAt: startPC script: script
	"The grouping of instructions just makes sure that each
	group ends with a statement"
	| group groups pc |
	groups := OrderedCollection new.
	pc := startPC.
	group := OrderedCollection new.
	groups add: group -> pc.
	instructions do: [:instr |
		pc := pc + 1.
		group add: instr.
		instr isStatement ifTrue: [
			group := OrderedCollection new.
			groups add: group -> pc]].
	^ groups
		select: [:kvp | kvp key notEmpty]
		thenCollect: [:kvp | self on: kvp key startingAt: kvp value script: script]