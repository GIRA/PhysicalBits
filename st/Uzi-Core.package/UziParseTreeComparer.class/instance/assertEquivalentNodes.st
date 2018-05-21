private
assertEquivalentNodes
	nodesDict keys do: [:node | self visit: node]