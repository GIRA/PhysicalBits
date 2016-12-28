building
withCurrentNode: node do: aBlock
	| old |
	old := currentNode.
	currentNode := node.
	aBlock ensure: [currentNode := old]