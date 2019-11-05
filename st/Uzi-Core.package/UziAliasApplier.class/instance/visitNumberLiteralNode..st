visiting
visitNumberLiteralNode: node
	^ (node class value: node value) token: node token