assertions
assertBlock: node
	^ self assert: node isBlock 
		description: 'Block expected'
		node: node