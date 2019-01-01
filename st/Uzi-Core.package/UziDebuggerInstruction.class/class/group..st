instance creation
group: instructions
	"The grouping of instructions just makes sure that each
	group ends with a statement"
	| group groups |
	groups := OrderedCollection new.
	group := OrderedCollection new.
	groups add: group.
	instructions do: [:instr |
		group add: instr.
		instr isStatement ifTrue: [
			group := OrderedCollection new.
			groups add: group]].
	^ groups
		select: [:each | each notEmpty]
		thenCollect: [:each | self on: each]