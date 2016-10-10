as yet unclassified
testPushPop
	| stack |
	stack := UziStack new.
	stack push: 1.
	stack push: 2.
	stack push: 3.
	self assert: {stack pop. stack pop. stack pop} = {3. 2. 1}